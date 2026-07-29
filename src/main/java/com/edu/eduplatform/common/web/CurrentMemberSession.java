package com.edu.eduplatform.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * "현재 회원"을 세션에 기억해두는 유일한 창구.
 * 실제 로그인(비밀번호 검증)은 Phase 6에서 붙는다 — 그 전까지는 회원가입/선택 시점에
 * 이 클래스로 세션에 회원 id만 기록해두고, {@link CurrentMemberIdArgumentResolver}가 꺼내 쓴다.
 * 나중에 토큰/AOP 기반으로 바꿀 때도 변경 범위는 이 클래스 + 리졸버로 한정된다.
 */
@Component
public class CurrentMemberSession {

    private static final String SESSION_KEY = "CURRENT_MEMBER_ID";

    public void set(HttpServletRequest request, Long memberId) {
        request.getSession(true).setAttribute(SESSION_KEY, memberId);
    }

    public Long get(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (Long) session.getAttribute(SESSION_KEY) : null;
    }
}
