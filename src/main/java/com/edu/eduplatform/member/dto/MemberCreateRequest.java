package com.edu.eduplatform.member.dto;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(

        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "닉네임을 입력해 주세요.")
        String nickname,

        @NotNull(message = "회원 유형을 선택해 주세요.")
        MemberType memberType,

        @NotNull(message = "학습 레벨을 선택해 주세요.")
        EnglishLevel level,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {
}
