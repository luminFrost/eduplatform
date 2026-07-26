package com.edu.eduplatform.progress.domain;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원의 레슨별 학습 진행 상황.
 * memberId, lessonId 로 참조한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long lessonId;

    @Column(nullable = false)
    private boolean completed;

    private LocalDateTime completedAt;

    @Builder
    public LearningProgress(Long memberId, Long lessonId) {
        this.memberId = memberId;
        this.lessonId = lessonId;
        this.completed = false;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }
}
