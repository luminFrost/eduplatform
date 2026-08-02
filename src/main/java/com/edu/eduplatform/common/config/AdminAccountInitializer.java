package com.edu.eduplatform.common.config;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고정 관리자 계정을 시딩한다(H2가 인메모리라 재부팅마다 사라지므로 매번 새로 필요).
 * 로그인 정보는 CLAUDE.md에 기록 — 회원가입 폼으로는 ADMIN을 만들 수 없다(자가 승격 방지).
 */
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    public static final String ADMIN_EMAIL = "admin@eduplatform.com";
    public static final String ADMIN_PASSWORD = "Admin1234!";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (memberRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }

        memberRepository.save(Member.builder()
                .email(ADMIN_EMAIL)
                .nickname("관리자")
                .memberType(MemberType.ADULT)
                .level(EnglishLevel.ADVANCED)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(MemberRole.ADMIN)
                .build());
    }
}
