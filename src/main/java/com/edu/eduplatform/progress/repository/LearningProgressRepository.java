package com.edu.eduplatform.progress.repository;

import com.edu.eduplatform.progress.domain.LearningProgress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    List<LearningProgress> findByMemberId(Long memberId);

    Optional<LearningProgress> findByMemberIdAndLessonId(Long memberId, Long lessonId);

    /** 완료한 지 오래된 순서로 복습 대상을 찾는다. */
    List<LearningProgress> findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(
            Long memberId, LocalDateTime cutoff);

    /** 이 코스의 레슨을 하나라도 완료한 서로 다른 회원 수 — "학습자 수" 표시에 쓴다. */
    @Query("select count(distinct lp.memberId) from LearningProgress lp "
            + "where lp.lessonId in (select l.id from Lesson l where l.courseId = :courseId)")
    long countDistinctMembersByCourseId(@Param("courseId") Long courseId);
}
