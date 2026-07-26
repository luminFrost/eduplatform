package com.edu.eduplatform.course.domain;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학습 코스. 대상(초등/성인)과 난이도(입문~고급)를 가진다.
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnglishLevel level;

    @Builder
    public Course(String title, String description, MemberType targetType, EnglishLevel level) {
        this.title = title;
        this.description = description;
        this.targetType = targetType;
        this.level = level;
    }
}
