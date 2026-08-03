package com.edu.eduplatform.member.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EmailVerificationServiceTest {

    private final EmailVerificationService emailVerificationService = new EmailVerificationService();

    @Test
    void verify_올바른_코드면_성공한다() {
        String code = emailVerificationService.issueCode("a@example.com");

        assertThat(emailVerificationService.verify("a@example.com", code)).isTrue();
    }

    @Test
    void verify_틀린_코드는_실패하고_소모되지_않아_다시_시도할_수_있다() {
        String code = emailVerificationService.issueCode("a@example.com");

        assertThat(emailVerificationService.verify("a@example.com", "000000".equals(code) ? "111111" : "000000")).isFalse();
        assertThat(emailVerificationService.verify("a@example.com", code)).isTrue();
    }

    @Test
    void verify_성공한_코드는_재사용할_수_없다() {
        String code = emailVerificationService.issueCode("a@example.com");

        assertThat(emailVerificationService.verify("a@example.com", code)).isTrue();
        assertThat(emailVerificationService.verify("a@example.com", code)).isFalse();
    }

    @Test
    void verify_발급된_적_없는_이메일은_실패한다() {
        assertThat(emailVerificationService.verify("nobody@example.com", "123456")).isFalse();
    }

    @Test
    void verify_만료_시각이_지나면_실패한다() {
        emailVerificationService.forceState("a@example.com", "123456", Instant.now().minusSeconds(1));

        assertThat(emailVerificationService.verify("a@example.com", "123456")).isFalse();
    }
}
