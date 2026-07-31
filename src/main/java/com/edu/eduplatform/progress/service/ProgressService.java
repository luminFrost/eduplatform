package com.edu.eduplatform.progress.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.dto.CourseProgressResponse;
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    /** 이보다 적게 완료한 상태에서는 영역별 커버리지 편차가 우연에 가까워 추천 근거로 삼지 않는다. */
    private static final int MIN_HISTORY_LESSONS = 3;

    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final MemberService memberService;

    public boolean isCompleted(Long memberId, Long lessonId) {
        return learningProgressRepository.findByMemberIdAndLessonId(memberId, lessonId)
                .map(LearningProgress::isCompleted)
                .orElse(false);
    }

    @Transactional
    public void complete(Long memberId, Long lessonId) {
        memberService.getMember(memberId);
        if (!lessonRepository.existsById(lessonId)) {
            throw new LessonNotFoundException(lessonId);
        }

        LearningProgress progress = learningProgressRepository.findByMemberIdAndLessonId(memberId, lessonId)
                .orElseGet(() -> LearningProgress.builder()
                        .memberId(memberId)
                        .lessonId(lessonId)
                        .build());

        if (!progress.isCompleted()) {
            progress.complete();
        }

        learningProgressRepository.save(progress);
    }

    public List<CourseProgressResponse> getCourseProgress(Long memberId) {
        List<LearningProgress> memberProgress = learningProgressRepository.findByMemberId(memberId);
        if (memberProgress.isEmpty()) {
            return List.of();
        }

        List<Long> touchedLessonIds = memberProgress.stream().map(LearningProgress::getLessonId).toList();
        Map<Long, Lesson> lessonsById = lessonRepository.findAllById(touchedLessonIds).stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        Map<Long, Long> completedCountByCourse = memberProgress.stream()
                .filter(LearningProgress::isCompleted)
                .map(progress -> lessonsById.get(progress.getLessonId()))
                .filter(lesson -> lesson != null)
                .collect(Collectors.groupingBy(Lesson::getCourseId, Collectors.counting()));

        List<Long> touchedCourseIds = lessonsById.values().stream()
                .map(Lesson::getCourseId)
                .distinct()
                .toList();

        Map<Long, Course> coursesById = courseRepository.findAllById(touchedCourseIds).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        return touchedCourseIds.stream()
                .map(courseId -> {
                    Course course = coursesById.get(courseId);
                    int totalLessons = lessonRepository.findByCourseIdOrderByOrderNoAsc(courseId).size();
                    int completedLessons = completedCountByCourse.getOrDefault(courseId, 0L).intValue();

                    return new CourseProgressResponse(
                            course.getId(),
                            course.getTitle(),
                            course.getEmoji(),
                            totalLessons,
                            completedLessons
                    );
                })
                .sorted(Comparator.comparing(CourseProgressResponse::courseId))
                .toList();
    }

    /**
     * 학습 이력을 근거로 비중을 둘 영역을 추천한다.
     * LearningProgress는 완료 여부만 기록하고(정답률·오답 데이터 없음, 퀴즈 미도입) 완료된 순간에만 생성되므로
     * "완료율"은 항상 100%라 신호가 되지 않는다. 대신 "회원 레벨에 존재하는 레슨 대비 얼마나 진행했는지(커버리지)"를
     * 약점의 대리 지표로 쓴다 — 커버리지가 가장 낮은 영역이 상대적으로 손대지 않은, 즉 더 필요한 영역이라고 본다.
     * 커버리지가 가장 낮은 영역(들)을 반환한다 — 여러 영역이 동률이면 모두 포함한다.
     */
    public Set<LessonType> recommendFocusAreas(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);

        List<LearningProgress> memberProgress = learningProgressRepository.findByMemberId(memberId);
        long completedCount = memberProgress.stream().filter(LearningProgress::isCompleted).count();
        if (completedCount < MIN_HISTORY_LESSONS) {
            throw new InsufficientHistoryException();
        }

        List<Long> completedLessonIds = memberProgress.stream()
                .filter(LearningProgress::isCompleted)
                .map(LearningProgress::getLessonId)
                .toList();
        Map<LessonType, Long> completedCountByType = lessonRepository.findAllById(completedLessonIds).stream()
                .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.counting()));

        Map<LessonType, Long> availableCountByType = courseRepository.search(member.memberType(), member.level(), null).stream()
                .flatMap(course -> lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId()).stream())
                .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.counting()));

        if (availableCountByType.isEmpty()) {
            throw new InsufficientHistoryException();
        }

        double lowestCoverage = availableCountByType.entrySet().stream()
                .mapToDouble(entry -> completedCountByType.getOrDefault(entry.getKey(), 0L) / (double) entry.getValue())
                .min()
                .orElseThrow();

        Set<LessonType> focusAreas = EnumSet.noneOf(LessonType.class);
        availableCountByType.forEach((type, availableCount) -> {
            double coverage = completedCountByType.getOrDefault(type, 0L) / (double) availableCount;
            if (coverage == lowestCoverage) {
                focusAreas.add(type);
            }
        });

        return focusAreas;
    }
}
