package com.edu.eduplatform.progress.repository;

import com.edu.eduplatform.progress.domain.LearningProgress;
import java.time.LocalDateTime;
import java.util.Collection;
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

    /** 코스 목록 화면에서 카드마다 따로 집계 쿼리를 날리지 않도록 courseId 여러 개를 한 번에 집계한다. */
    @Query("select l.courseId as courseId, count(distinct lp.memberId) as learnerCount "
            + "from LearningProgress lp join Lesson l on l.id = lp.lessonId "
            + "where l.courseId in :courseIds group by l.courseId")
    List<CourseLearnerCountProjection> countDistinctMembersByCourseIdIn(@Param("courseIds") Collection<Long> courseIds);

    interface CourseLearnerCountProjection {
        Long getCourseId();

        Long getLearnerCount();
    }
}
