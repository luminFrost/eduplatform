package com.edu.eduplatform.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.member.security.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MemberViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    void 유효한_폼을_제출하면_인증_페이지로_리다이렉트된다() throws Exception {
        mockMvc.perform(post("/members/new").with(csrf())
                        .param("email", "verify-flow@example.com")
                        .param("nickname", "인증플로우테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(redirectedUrl("/members/new/verify"));
    }

    @Test
    void 이미_가입된_이메일이면_1단계에서_바로_에러를_보여준다() throws Exception {
        memberRepository.save(Member.builder()
                .email("dup-flow@example.com").nickname("기존회원")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build());

        mockMvc.perform(post("/members/new").with(csrf())
                        .param("email", "dup-flow@example.com")
                        .param("nickname", "새닉네임")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이미")));
    }

    @Test
    void 세션에_대기중인_가입정보가_없으면_인증페이지_접근시_처음으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/members/new/verify"))
                .andExpect(redirectedUrl("/members/new"));
    }

    @Test
    void 올바른_인증번호를_제출하면_실제_계정이_만들어지고_로그인된다() throws Exception {
        MvcResult step1 = mockMvc.perform(post("/members/new").with(csrf())
                        .param("email", "verify-success@example.com")
                        .param("nickname", "인증성공테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(redirectedUrl("/members/new/verify"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) step1.getRequest().getSession();

        String code = emailVerificationService.issueCode("verify-success@example.com");

        mockMvc.perform(post("/members/new/verify").session(session).with(csrf())
                        .param("code", code))
                .andExpect(redirectedUrl("/my"));

        assertThat(memberRepository.findByEmail("verify-success@example.com")).isPresent();

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("인증성공테스터")));
    }

    @Test
    void 틀린_인증번호를_제출하면_에러와_함께_재시도_화면을_보여준다() throws Exception {
        MvcResult step1 = mockMvc.perform(post("/members/new").with(csrf())
                        .param("email", "verify-fail@example.com")
                        .param("nickname", "인증실패테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(redirectedUrl("/members/new/verify"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) step1.getRequest().getSession();

        mockMvc.perform(post("/members/new/verify").session(session).with(csrf())
                        .param("code", "wrong-code"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("인증번호가 올바르지 않거나")));

        assertThat(memberRepository.findByEmail("verify-fail@example.com")).isEmpty();
    }
}
