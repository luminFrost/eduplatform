package com.edu.eduplatform.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * 회원가입 직후 "가입했으니 바로 로그인된 상태로 시작" UX를 위해 프로그램적으로 인증 처리하는 창구.
 * SecurityContextHolderFilter는 컨텍스트를 읽기만 하고 자동 저장하지 않으므로,
 * securityContextRepository.saveContext()로 직접 세션에 남겨야 다음 요청에서도 인증이 유지된다.
 */
@Component
@RequiredArgsConstructor
public class CurrentMemberSession {

    private final SecurityContextRepository securityContextRepository;

    public void login(HttpServletRequest request, HttpServletResponse response, UserDetails userDetails) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
