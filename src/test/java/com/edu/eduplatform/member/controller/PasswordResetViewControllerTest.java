package com.edu.eduplatform.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.member.security.PasswordResetTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 비로그인이어도_재설정_요청_폼에_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/password-reset"))
                .andExpect(status().isOk());
    }

    @Test
    void 이메일을_제출하면_존재_여부와_무관하게_같은_화면으로_리다이렉트된다() throws Exception {
        mockMvc.perform(post("/password-reset").with(csrf())
                        .param("email", "nobody@example.com"))
                .andExpect(redirectedUrl("/password-reset?requested"));
    }

    @Test
    void 유효한_토큰이면_확인_폼이_정상_렌더링된다() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .email("reset-confirm@example.com").nickname("테스터")
                .memberType(com.edu.eduplatform.member.domain.MemberType.ADULT)
                .level(com.edu.eduplatform.member.domain.EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode("oldPassword1")).build());
        String token = passwordResetTokenService.issueToken(member.getId());

        mockMvc.perform(get("/password-reset/confirm").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("유효하지 않거나"))));
    }

    @Test
    void 무효한_토큰이면_에러_메시지를_보여준다() throws Exception {
        mockMvc.perform(get("/password-reset/confirm").param("token", "bogus-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("유효하지 않거나")));
    }

    @Test
    void 유효한_토큰으로_새_비밀번호를_제출하면_로그인이_가능해진다() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .email("reset-success@example.com").nickname("테스터")
                .memberType(com.edu.eduplatform.member.domain.MemberType.ADULT)
                .level(com.edu.eduplatform.member.domain.EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode("oldPassword1")).build());
        String token = passwordResetTokenService.issueToken(member.getId());

        mockMvc.perform(post("/password-reset/confirm").with(csrf())
                        .param("token", token)
                        .param("newPassword", "newPassword1"))
                .andExpect(redirectedUrl("/login?resetSuccess"));

        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword1", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("oldPassword1", updated.getPassword())).isFalse();

        // 소모된 토큰은 재사용 불가
        mockMvc.perform(get("/password-reset/confirm").param("token", token))
                .andExpect(content().string(containsString("유효하지 않거나")));
    }
}
