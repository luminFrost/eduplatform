package com.edu.eduplatform.lesson.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse;
import com.edu.eduplatform.lesson.dto.LessonSummaryResponse;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    public List<LessonSummaryResponse> listByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderNoAsc(courseId).stream()
                .map(LessonSummaryResponse::from)
                .toList();
    }

    public LessonDetailResponse getDetail(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new LessonNotFoundException(id));
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(lesson.getCourseId()));
        List<Lesson> siblings = lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId());

        int index = IntStream.range(0, siblings.size())
                .filter(i -> siblings.get(i).getId().equals(lesson.getId()))
                .findFirst()
                .orElseThrow();
        Lesson prev = index > 0 ? siblings.get(index - 1) : null;
        Lesson next = index < siblings.size() - 1 ? siblings.get(index + 1) : null;

        return new LessonDetailResponse(
                lesson.getId(),
                course.getId(),
                course.getTitle(),
                lesson.getTitle(),
                lesson.getOrderNo(),
                lesson.getContent(),
                siblings.size(),
                prev != null ? prev.getId() : null,
                prev != null ? prev.getTitle() : null,
                next != null ? next.getId() : null,
                next != null ? next.getTitle() : null
        );
    }
}
