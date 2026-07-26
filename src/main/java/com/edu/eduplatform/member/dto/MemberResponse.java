package com.edu.eduplatform.member.dto;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String nickname,
        MemberType memberType,
        EnglishLevel level,
        LocalDateTime createdAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getMemberType(),
                member.getLevel(),
                member.getCreatedAt()
        );
    }
}
