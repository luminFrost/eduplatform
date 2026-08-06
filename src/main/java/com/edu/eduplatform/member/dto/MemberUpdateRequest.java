package com.edu.eduplatform.member.dto;

import com.edu.eduplatform.member.domain.EnglishLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberUpdateRequest(

        @NotBlank(message = "닉네임을 입력해 주세요.")
        String nickname,

        @NotNull(message = "학습 레벨을 선택해 주세요.")
        EnglishLevel level,

        @Min(value = 0, message = "주간 목표는 0 이상이어야 해요.")
        int weeklyGoal
) {
}
