package com.edu.eduplatform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * 이메일+비밀번호 세션 로그인. REST API(/api/**)는 이번 범위에서 제외하고 permitAll로 둔다
 * (요청 본문의 memberId를 그대로 신뢰하는 기존 구조는 후속 과제 — CLAUDE.md 참고).
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/members/new").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // 아래 3개는 특정 규칙이라, 뒤따르는 일반 /courses/** permitAll보다 먼저 와야 한다.
                        .requestMatchers("/my", "/my/**").authenticated()
                        .requestMatchers("/courses/personal/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lessons/*/complete").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/lessons/*").permitAll()
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
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
