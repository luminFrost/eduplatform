package com.edu.eduplatform.member.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PasswordResetTokenServiceTest {

    private final PasswordResetTokenService passwordResetTokenService = new PasswordResetTokenService();

    @Test
    void issueToken_발급_직후_peek하면_회원id를_반환한다() {
        String token = passwordResetTokenService.issueToken(1L);

        assertThat(passwordResetTokenService.peek(token)).contains(1L);
    }

    @Test
    void consume_소모한_토큰은_다시_사용할_수_없다() {
        String token = passwordResetTokenService.issueToken(1L);

        Optional<Long> result = passwordResetTokenService.consume(token);

        assertThat(result).contains(1L);
        assertThat(passwordResetTokenService.peek(token)).isEmpty();
        assertThat(passwordResetTokenService.consume(token)).isEmpty();
    }

    @Test
    void peek_존재하지_않는_토큰은_빈_값을_반환한다() {
        assertThat(passwordResetTokenService.peek("nonexistent")).isEmpty();
    }

    @Test
    void peek_만료_시각이_지나면_빈_값을_반환한다() {
        passwordResetTokenService.forceState("expired-token", 1L, Instant.now().minusSeconds(1));

        assertThat(passwordResetTokenService.peek("expired-token")).isEmpty();
    }
}
