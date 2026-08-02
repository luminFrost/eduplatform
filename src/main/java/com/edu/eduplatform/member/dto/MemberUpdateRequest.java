package com.edu.eduplatform.member.dto;

import com.edu.eduplatform.member.domain.EnglishLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberUpdateRequest(

        @NotBlank(message = "닉네임을 입력해 주세요.")
        String nickname,

        @NotNull(message = "학습 레벨을 선택해 주세요.")
        EnglishLevel level
) {
}
