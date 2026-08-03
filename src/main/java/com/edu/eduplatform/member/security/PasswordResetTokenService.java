package com.edu.eduplatform.member.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 재설정 토큰을 인메모리로 추적한다. H2도 인메모리로 쓰는 이 프로젝트 규모에 맞춘 단순함
 * (재부팅하면 초기화됨을 그대로 수용) — {@link LoginAttemptService}와 같은 패턴.
 */
@Component
public class PasswordResetTokenService {

    static final Duration TOKEN_VALIDITY = Duration.ofMinutes(30);

    private final Map<String, TokenRecord> tokensByValue = new ConcurrentHashMap<>();

    public String issueToken(Long memberId) {
        String token = UUID.randomUUID().toString();
        tokensByValue.put(token, new TokenRecord(memberId, Instant.now().plus(TOKEN_VALIDITY)));
        return token;
    }

    /** 토큰을 소모하지 않고 유효성만 확인 — 재설정 폼을 보여줄 때 쓴다. */
    public Optional<Long> peek(String token) {
        TokenRecord record = tokensByValue.get(token);
        if (record == null || Instant.now().isAfter(record.expiresAt())) {
            return Optional.empty();
        }
        return Optional.of(record.memberId());
    }

    /** 실제 비밀번호 변경 시 토큰을 소모(1회용)한다. */
    public Optional<Long> consume(String token) {
        Optional<Long> memberId = peek(token);
        tokensByValue.remove(token);
        return memberId;
    }

    /** 테스트 전용 — 실제 시간 흐름 없이 만료 경계를 검증할 때 쓴다. */
    void forceState(String token, Long memberId, Instant expiresAt) {
        tokensByValue.put(token, new TokenRecord(memberId, expiresAt));
    }

    private record TokenRecord(Long memberId, Instant expiresAt) {
    }
}
