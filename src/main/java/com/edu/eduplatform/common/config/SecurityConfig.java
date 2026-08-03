package com.edu.eduplatform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/**
 * 두 개의 필터체인으로 나눈다. formLogin과 httpBasic을 같은 체인에 같이 두면 Spring Security가
 * 인증되지 않은 모든 요청에 (경로 상관없이) 필터체인 하나당 하나의 기본 AuthenticationEntryPoint만
 * 쓰기 때문에 httpBasic의 401 challenge가 formLogin의 "/login으로 리다이렉트"를 덮어써버린다
 * (직접 겪음 — 브라우저 라우트까지 401을 반환하던 버그).
 * - apiSecurityFilterChain: /api/** 전용. HTTP Basic, 세션 없음(STATELESS), CSRF 없음.
 *   회원 데이터를 바꾸는 API(POST /api/progress/complete, /api/courses/personal[/history-based])는
 *   인증을 요구해 memberId를 요청 본문이 아니라 인증된 사용자에서 가져온다 — 요청 본문의 memberId를
 *   그대로 신뢰하던 기존 구조를 여기서 닫는다. 그 외 API(GET 조회, 회원가입)는 계속 공개.
 * - webSecurityFilterChain: 그 외 전부. 이메일+비밀번호 세션 로그인(브라우저).
 * 둘 다 같은 MemberUserDetailsService/PasswordEncoder를 쓴다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        // 아래 2개는 특정 규칙이라, 뒤따르는 anyRequest().permitAll()보다 먼저 와야 한다.
                        .requestMatchers(HttpMethod.POST, "/api/progress/complete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/courses/personal", "/api/courses/personal/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        // httpBasic 인증 실패 시 response.sendError(401)이 Tomcat의 /error 내부 포워드를 태우는데,
                        // 이 체인이 catch-all(anyRequest().authenticated())이라 /error까지 로그인 리다이렉트로
                        // 덮어써버리는 문제가 있었다 — /error는 항상 permitAll로 열어둔다.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/", "/login", "/css/**", "/images/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/members/new").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/new").permitAll()
                        .requestMatchers("/members/new/verify").permitAll()
                        .requestMatchers("/members/new/level-test").permitAll()
                        .requestMatchers("/password-reset", "/password-reset/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // 아래는 특정 규칙이라, 뒤따르는 일반 /courses/** permitAll보다 먼저 와야 한다.
                        .requestMatchers("/my", "/my/**").authenticated()
                        .requestMatchers("/courses/personal/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lessons/*/complete").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/lessons/*").permitAll()
                        .requestMatchers("/quiz/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/my", false)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        // h2-console은 인라인 스크립트를 광범위하게 써서 CSP를 걸면 콘솔 자체가 망가진다 —
                        // 개발용 도구는 permitAll/CSRF 제외와 같은 원칙으로 CSP에서도 뺀다.
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                new NegatedRequestMatcher(PathPatternRequestMatcher.pathPattern("/h2-console/**")),
                                new ContentSecurityPolicyHeaderWriter(
                                        "default-src 'self'; "
                                        + "script-src 'self'; "
                                        // style-src의 unsafe-inline은 my/dashboard.html 진도 막대의 th:style(서버가
                                        // 계산한 퍼센트)을 위해 필요 — script-src는 타협 없이 'self'만 허용.
                                        + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                                        + "font-src 'self' https://fonts.gstatic.com; "
                                        + "img-src 'self'; "
                                        + "object-src 'none'; "
                                        + "base-uri 'self'; "
                                        + "form-action 'self'; "
                                        + "frame-ancestors 'self'")))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // SPEAKING 레슨이 실제로 마이크(SpeechRecognition)를 쓰므로 microphone은 self로 열어둔다.
                        // permissionsPolicy()는 체이닝용 반환 타입이 아니라 permissionsPolicyHeader()를 쓴다.
                        .permissionsPolicyHeader(permissions -> permissions.policy(
                                "camera=(), microphone=(self), geolocation=(), payment=()"))
                        // isSecure()(HTTPS)일 때만 실제로 헤더가 나가 지금의 로컬 HTTP 환경엔 영향 없음 —
                        // 나중에 HTTPS로 배포하면 바로 의미를 갖도록 의도를 코드에 명시해둔다.
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                );

        return http.build();
    }
}
