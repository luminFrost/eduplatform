package com.edu.eduplatform.progress.repository;

import com.edu.eduplatform.progress.domain.LearningProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    List<LearningProgress> findByMemberId(Long memberId);

    Optional<LearningProgress> findByMemberIdAndLessonId(Long memberId, Long lessonId);
}
