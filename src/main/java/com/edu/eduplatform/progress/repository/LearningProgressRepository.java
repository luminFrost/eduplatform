package com.edu.eduplatform.progress.repository;

import com.edu.eduplatform.progress.domain.LearningProgress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    List<LearningProgress> findByMemberId(Long memberId);

    Optional<LearningProgress> findByMemberIdAndLessonId(Long memberId, Long lessonId);

    /** 완료한 지 오래된 순서로 복습 대상을 찾는다. */
    List<LearningProgress> findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(
            Long memberId, LocalDateTime cutoff);
}
