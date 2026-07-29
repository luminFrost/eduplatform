package com.edu.eduplatform.progress.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.dto.CourseProgressResponse;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    public boolean isCompleted(Long memberId, Long lessonId) {
        return learningProgressRepository.findByMemberIdAndLessonId(memberId, lessonId)
                .map(LearningProgress::isCompleted)
                .orElse(false);
    }

    @Transactional
    public void complete(Long memberId, Long lessonId) {
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
}
