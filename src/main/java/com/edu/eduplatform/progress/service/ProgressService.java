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
import com.edu.eduplatform.progress.dto.DashboardSummaryResponse;
import com.edu.eduplatform.progress.dto.SkillAreaProgressResponse;
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
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

        long completedCount = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .count();
        if (completedCount < MIN_HISTORY_LESSONS) {
            throw new InsufficientHistoryException();
        }

        Map<LessonType, SkillAreaCounts> counts = computeSkillAreaCounts(memberId, member);
        if (counts.isEmpty()) {
            throw new InsufficientHistoryException();
        }

        double lowestCoverage = counts.values().stream()
                .mapToDouble(SkillAreaCounts::coverage)
                .min()
                .orElseThrow();

        Set<LessonType> focusAreas = EnumSet.noneOf(LessonType.class);
        counts.forEach((type, c) -> {
            if (c.coverage() == lowestCoverage) {
                focusAreas.add(type);
            }
        });

        return focusAreas;
    }

    /**
     * 영역별(어휘/읽기/쓰기/듣기/말하기) 완료 레슨 수 대비 회원 레벨에 존재하는 전체 레슨 수를 반환한다.
     * 마이페이지 대시보드의 "영역별 학습 현황" 막대그래프에 쓰인다.
     */
    public List<SkillAreaProgressResponse> getSkillAreaProgress(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);
        Map<LessonType, SkillAreaCounts> counts = computeSkillAreaCounts(memberId, member);

        return Arrays.stream(LessonType.values())
                .filter(counts::containsKey)
                .map(type -> {
                    SkillAreaCounts c = counts.get(type);
                    return new SkillAreaProgressResponse(type, (int) c.completed(), (int) c.available());
                })
                .toList();
    }

    /** 마이페이지 대시보드 상단 요약 통계(완료 레슨 수/학습 중인 코스 수/전체 진도율). */
    public DashboardSummaryResponse getDashboardSummary(Long memberId) {
        List<CourseProgressResponse> courseProgress = getCourseProgress(memberId);
        int totalCompleted = courseProgress.stream().mapToInt(CourseProgressResponse::completedLessons).sum();
        int totalLessons = courseProgress.stream().mapToInt(CourseProgressResponse::totalLessons).sum();
        int overallPercentage = totalLessons == 0 ? 0 : (int) Math.round(totalCompleted * 100.0 / totalLessons);

        return new DashboardSummaryResponse(totalCompleted, courseProgress.size(), overallPercentage);
    }

    /** 영역별 완료/가능 레슨 수. {@link #recommendFocusAreas}와 {@link #getSkillAreaProgress}가 공유한다. */
    private Map<LessonType, SkillAreaCounts> computeSkillAreaCounts(Long memberId, MemberResponse member) {
        List<Long> completedLessonIds = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .map(LearningProgress::getLessonId)
                .toList();
        Map<LessonType, Long> completedCountByType = lessonRepository.findAllById(completedLessonIds).stream()
                .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.counting()));

        Map<LessonType, Long> availableCountByType = courseRepository.search(member.memberType(), member.level(), null).stream()
                .flatMap(course -> lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId()).stream())
                .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.counting()));

        Map<LessonType, SkillAreaCounts> result = new EnumMap<>(LessonType.class);
        availableCountByType.forEach((type, available) ->
                result.put(type, new SkillAreaCounts(completedCountByType.getOrDefault(type, 0L), available)));
        return result;
    }

    private record SkillAreaCounts(long completed, long available) {
        double coverage() {
            return available == 0 ? 0.0 : completed / (double) available;
        }
    }
}
