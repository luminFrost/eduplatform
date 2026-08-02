package com.edu.eduplatform.member.dto;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.domain.MemberType;
import java.time.LocalDateTime;

public record MemberAdminResponse(
        Long id,
        String email,
        String nickname,
        MemberType memberType,
        EnglishLevel level,
        MemberRole role,
        LocalDateTime createdAt
) {

    public static MemberAdminResponse from(Member member) {
        return new MemberAdminResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getMemberType(),
                member.getLevel(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
