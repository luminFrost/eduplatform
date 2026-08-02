package com.edu.eduplatform.lesson.repository;

import com.edu.eduplatform.lesson.domain.Lesson;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByOrderNoAsc(Long courseId);

    /** 코스 여러 개의 레슨을 한 번에 조회한다 — 코스 목록을 순회하며 코스마다 따로 조회하는 N+1을 피할 때 쓴다. */
    List<Lesson> findByCourseIdIn(Collection<Long> courseIds);

    /** 공식 코스(개인 코스 제외)의 레슨 내용에서 키워드를 검색한다. Course와 매핑된 연관관계가 없어 명시적 ON 조인을 쓴다. */
    @Query("select l from Lesson l join Course c on c.id = l.courseId "
            + "where c.ownerId is null and lower(cast(l.content as string)) like lower(concat('%', :keyword, '%')) "
            + "order by l.courseId, l.orderNo")
    List<Lesson> searchByContent(@Param("keyword") String keyword);
}
