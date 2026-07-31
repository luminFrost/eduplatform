package com.edu.eduplatform.course.dto;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseCreateRequest(

        @NotBlank(message = "코스명을 입력해 주세요.")
        String title,

        String description,

        String emoji,

        @NotNull(message = "대상을 선택해 주세요.")
        MemberType targetType,

        @NotNull(message = "레벨을 선택해 주세요.")
        EnglishLevel level
) {
}
