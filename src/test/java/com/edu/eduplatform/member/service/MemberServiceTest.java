package com.edu.eduplatform.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    void signUp_동시가입으로_유니크_제약을_위반하면_중복이메일_예외로_변환한다() {
        MemberCreateRequest request = new MemberCreateRequest(
                "race@example.com", "레이스테스터", MemberType.ADULT, EnglishLevel.BEGINNER, "password1234");
        // 첫 조회 시점엔 중복이 없어 보이지만(레이스), 실제 저장 시점엔 이미 다른 요청이 먼저 커밋한 상황을 재현.
        when(memberRepository.findByEmail("race@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password1234")).thenReturn("hashed");
        when(memberRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
