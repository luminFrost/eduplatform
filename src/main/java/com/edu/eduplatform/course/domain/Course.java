package com.edu.eduplatform.course.domain;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학습 코스. 대상(초등/성인)과 난이도(입문~고급)를 가진다.
 * 공식 코스(ownerId == null)와 개인 코스(ownerId == 소유 회원, PRODUCT.md 3-2)를 같은 구조로 표현한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    /** 코스 목록·상세에서 보여주는 대표 이모지 (예: "🦊"). 실제 삽화 도입 전까지 시각 요소로 사용. */
    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnglishLevel level;

    /** 개인 코스 소유 회원. null이면 공식 코스. */
    private Long ownerId;

    /** 개인 코스가 비중을 두는 영역(복수 가능). 공식 코스는 비움. */
    @ElementCollection
    @CollectionTable(name = "course_focus_area", joinColumns = @JoinColumn(name = "course_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type")
    private Set<LessonType> focusAreas = new HashSet<>();

    /** 개인 코스 기준 판단 방식. 공식 코스는 null. */
    @Enumerated(EnumType.STRING)
    private CourseCriteriaSource criteriaSource;

    @Builder
    public Course(
            String title,
            String description,
            String emoji,
            MemberType targetType,
            EnglishLevel level,
            Long ownerId,
            Set<LessonType> focusAreas,
            CourseCriteriaSource criteriaSource
    ) {
        this.title = title;
        this.description = description;
        this.emoji = emoji;
        this.targetType = targetType;
        this.level = level;
        this.ownerId = ownerId;
        this.focusAreas = focusAreas != null ? focusAreas : new HashSet<>();
        this.criteriaSource = criteriaSource;
    }

    public void updateDetails(String title, String description, String emoji, MemberType targetType, EnglishLevel level) {
        this.title = title;
        this.description = description;
        this.emoji = emoji;
        this.targetType = targetType;
        this.level = level;
    }

    public boolean isPersonal() {
        return ownerId != null;
    }
}
