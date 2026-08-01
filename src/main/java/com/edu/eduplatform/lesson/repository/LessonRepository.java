package com.edu.eduplatform.lesson.repository;

import com.edu.eduplatform.lesson.domain.Lesson;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByOrderNoAsc(Long courseId);

    /** 코스 여러 개의 레슨을 한 번에 조회한다 — 코스 목록을 순회하며 코스마다 따로 조회하는 N+1을 피할 때 쓴다. */
    List<Lesson> findByCourseIdIn(Collection<Long> courseIds);
}
