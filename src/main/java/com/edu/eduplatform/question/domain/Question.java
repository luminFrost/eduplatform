package com.edu.eduplatform.question.domain;

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
import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 진단 테스트(DIAGNOSTIC_TEST) 문항. 특정 레슨이 아니라 대상·레벨·영역(LessonType)에 매인다 —
 * 듣기·말하기처럼 아직 레슨이 없는 영역도 문제를 낼 수 있어야 하기 때문(DESIGN.md의 lessonId 기반
 * Quiz/Question 스케치와 다르게 간 이유).
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnglishLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonType lessonType;

    @Column(nullable = false, length = 500)
    private String prompt;

    /** LISTENING 문항에서 speechSynthesis로 재생할 영어 텍스트. 그 외 타입은 null. */
    private String audioText;

    /** 보기 목록. 정답 인덱스와 순서가 반드시 일치해야 하므로 @OrderColumn 필수. */
    @ElementCollection
    @CollectionTable(name = "question_option", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_index")
    @Column(name = "option_text", nullable = false)
    private List<String> options = new ArrayList<>();

    @Column(nullable = false)
    private int correctOptionIndex;

    @Builder
    public Question(
            MemberType targetType,
            EnglishLevel level,
            LessonType lessonType,
            String prompt,
            String audioText,
            List<String> options,
            int correctOptionIndex
    ) {
        this.targetType = targetType;
        this.level = level;
        this.lessonType = lessonType;
        this.prompt = prompt;
        this.audioText = audioText;
        this.options = options != null ? options : new ArrayList<>();
        this.correctOptionIndex = correctOptionIndex;
    }

    public void updateDetails(MemberType targetType, EnglishLevel level, LessonType lessonType,
                               String prompt, String audioText, List<String> options, int correctOptionIndex) {
        this.targetType = targetType;
        this.level = level;
        this.lessonType = lessonType;
        this.prompt = prompt;
        this.audioText = audioText;
        this.options = new ArrayList<>(options);
        this.correctOptionIndex = correctOptionIndex;
    }
}
