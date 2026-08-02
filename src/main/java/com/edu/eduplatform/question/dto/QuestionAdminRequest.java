package com.edu.eduplatform.question.dto;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuestionAdminRequest(

        @NotNull(message = "대상을 선택해 주세요.")
        MemberType targetType,

        @NotNull(message = "레벨을 선택해 주세요.")
        EnglishLevel level,

        @NotNull(message = "영역을 선택해 주세요.")
        LessonType lessonType,

        @NotBlank(message = "문제를 입력해 주세요.")
        String prompt,

        String audioText,

        @NotBlank(message = "보기 1을 입력해 주세요.")
        String option1,

        @NotBlank(message = "보기 2를 입력해 주세요.")
        String option2,

        @NotBlank(message = "보기 3을 입력해 주세요.")
        String option3,

        @NotBlank(message = "보기 4를 입력해 주세요.")
        String option4,

        int correctOptionIndex
) {
    public List<String> options() {
        return List.of(option1, option2, option3, option4);
    }
}
