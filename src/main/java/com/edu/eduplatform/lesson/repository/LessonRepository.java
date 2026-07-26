package com.edu.eduplatform.lesson.repository;

import com.edu.eduplatform.lesson.domain.Lesson;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByOrderNoAsc(Long courseId);
}
