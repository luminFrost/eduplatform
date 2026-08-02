package com.edu.eduplatform.member.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private final LoginAttemptService loginAttemptService = new LoginAttemptService();

    @Test
    void isLocked_기록이_없으면_잠기지_않는다() {
        assertThat(loginAttemptService.isLocked("nobody@example.com")).isFalse();
    }

    @Test
    void isLocked_최대_시도_횟수_미만이면_잠기지_않는다() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS - 1; i++) {
            loginAttemptService.recordFailure("user@example.com");
        }

        assertThat(loginAttemptService.isLocked("user@example.com")).isFalse();
    }

    @Test
    void isLocked_최대_시도_횟수에_도달하면_잠긴다() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            loginAttemptService.recordFailure("user@example.com");
        }

        assertThat(loginAttemptService.isLocked("user@example.com")).isTrue();
    }

    @Test
    void isLocked_이메일_대소문자와_무관하게_같은_계정으로_취급한다() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            loginAttemptService.recordFailure("User@Example.com");
        }

        assertThat(loginAttemptService.isLocked("user@example.com")).isTrue();
    }

    @Test
    void recordSuccess_로그인에_성공하면_실패_기록이_초기화된다() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            loginAttemptService.recordFailure("user@example.com");
        }
        assertThat(loginAttemptService.isLocked("user@example.com")).isTrue();

        loginAttemptService.recordSuccess("user@example.com");

        assertThat(loginAttemptService.isLocked("user@example.com")).isFalse();
    }

    @Test
    void isLocked_잠금_유지_시간이_지나면_풀린다() {
        Instant longAgo = Instant.now().minus(LoginAttemptService.LOCKOUT_DURATION).minusSeconds(60);
        loginAttemptService.forceState("user@example.com", LoginAttemptService.MAX_ATTEMPTS, longAgo);

        assertThat(loginAttemptService.isLocked("user@example.com")).isFalse();
    }

    @Test
    void isLocked_잠금_유지_시간_안이면_계속_잠겨있다() {
        Instant justNow = Instant.now().minusSeconds(10);
        loginAttemptService.forceState("user@example.com", LoginAttemptService.MAX_ATTEMPTS, justNow);

        assertThat(loginAttemptService.isLocked("user@example.com")).isTrue();
    }
}
