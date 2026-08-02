package com.edu.eduplatform.member.domain;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
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

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnglishLevel level;

    /** BCrypt로 해시된 값만 저장한다 (평문 아님). */
    @Column(nullable = false)
    private String password;

    @Builder
    public Member(String email, String nickname, MemberType memberType, EnglishLevel level, String password) {
        this.email = email;
        this.nickname = nickname;
        this.memberType = memberType;
        this.level = level;
        this.password = password;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeLevel(EnglishLevel level) {
        this.level = level;
    }

    /** 인자는 반드시 이미 해시된 값이어야 한다 — 평문 비밀번호를 그대로 넘기지 않는다. */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
