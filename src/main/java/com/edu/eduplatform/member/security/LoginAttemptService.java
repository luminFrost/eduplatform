package com.edu.eduplatform.member.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * 무차별 대입(브루트포스) 방지 — 이메일별로 로그인 실패 횟수를 인메모리로 추적한다.
 * H2도 인메모리로 쓰는 이 프로젝트 규모에 맞춘 단순함(재부팅하면 초기화됨을 그대로 수용, Redis 등
 * 외부 저장소는 과함). Spring Security의 {@link org.springframework.security.authentication.ProviderManager}가
 * 인증 성공/실패마다 자동으로 발행하는 이벤트만 구독해서 만들어 SecurityConfig는 전혀 안 건드린다.
 */
@Component
public class LoginAttemptService {

    static final int MAX_ATTEMPTS = 5;
    static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final Map<String, AttemptRecord> attemptsByEmail = new ConcurrentHashMap<>();

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        recordFailure(event.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        recordSuccess(event.getAuthentication().getName());
    }

    void recordFailure(String email) {
        String key = normalize(email);
        attemptsByEmail.merge(key, new AttemptRecord(1, Instant.now()),
                (existing, ignored) -> new AttemptRecord(existing.count() + 1, Instant.now()));
    }

    void recordSuccess(String email) {
        attemptsByEmail.remove(normalize(email));
    }

    public boolean isLocked(String email) {
        AttemptRecord record = attemptsByEmail.get(normalize(email));
        if (record == null || record.count() < MAX_ATTEMPTS) {
            return false;
        }
        if (Instant.now().isAfter(record.lastFailureAt().plus(LOCKOUT_DURATION))) {
            attemptsByEmail.remove(normalize(email));
            return false;
        }
        return true;
    }

    /** 테스트 전용 — 실제 시간 흐름 없이 잠금 만료 경계를 검증할 때 쓴다. */
    void forceState(String email, int count, Instant lastFailureAt) {
        attemptsByEmail.put(normalize(email), new AttemptRecord(count, lastFailureAt));
    }

    private static String normalize(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    private record AttemptRecord(int count, Instant lastFailureAt) {
    }
}
