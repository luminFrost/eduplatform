package com.edu.eduplatform.member.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 회원가입 이메일 인증번호를 인메모리로 추적한다. H2도 인메모리로 쓰는 이 프로젝트 규모에 맞춘 단순함
 * (재부팅하면 초기화됨을 그대로 수용) — {@link PasswordResetTokenService}와 같은 패턴.
 */
@Component
public class EmailVerificationService {

    static final Duration CODE_VALIDITY = Duration.ofMinutes(10);

    private final Map<String, VerificationRecord> codesByEmail = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String issueCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        codesByEmail.put(normalize(email), new VerificationRecord(code, Instant.now().plus(CODE_VALIDITY)));
        return code;
    }

    /** 틀린 코드는 소모하지 않는다 — 만료 전까지 다시 시도할 수 있다. */
    public boolean verify(String email, String code) {
        String key = normalize(email);
        VerificationRecord record = codesByEmail.get(key);
        if (record == null || Instant.now().isAfter(record.expiresAt()) || !record.code().equals(code)) {
            return false;
        }
        codesByEmail.remove(key);
        return true;
    }

    /** 테스트 전용 — 실제 시간 흐름 없이 만료 경계를 검증할 때 쓴다. */
    void forceState(String email, String code, Instant expiresAt) {
        codesByEmail.put(normalize(email), new VerificationRecord(code, expiresAt));
    }

    private static String normalize(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    private record VerificationRecord(String code, Instant expiresAt) {
    }
}
