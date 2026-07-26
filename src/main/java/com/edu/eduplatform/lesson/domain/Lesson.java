package com.edu.eduplatform.lesson.domain;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스에 속한 개별 학습 단위(레슨).
 * courseId 로 코스를 참조한다. (연관관계는 추후 @ManyToOne 으로 전환 가능)
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int orderNo;

    @Lob
    private String content;

    @Builder
    public Lesson(Long courseId, String title, int orderNo, String content) {
        this.courseId = courseId;
        this.title = title;
        this.orderNo = orderNo;
        this.content = content;
    }
}
