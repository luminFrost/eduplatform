package com.edu.eduplatform.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(

        @NotBlank
        String token,

        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
        String newPassword
) {
}
