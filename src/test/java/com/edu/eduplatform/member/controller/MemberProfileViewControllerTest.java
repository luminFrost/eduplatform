package com.edu.eduplatform.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MemberProfileViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 비로그인이면_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/my/profile"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 로그인한_회원은_닉네임과_레벨을_수정할_수_있다() throws Exception {
        MockHttpSession session = signUp("profile-test@example.com", "프로필테스터");

        mockMvc.perform(post("/my/profile").session(session).with(csrf())
                        .param("nickname", "바뀐닉네임")
                        .param("level", "INTERMEDIATE"))
                .andExpect(redirectedUrl("/my/profile?updated"));

        mockMvc.perform(get("/my/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("바뀐닉네임")));
    }

    @Test
    void 현재_비밀번호가_틀리면_에러_메시지와_함께_폼이_그대로_남는다() throws Exception {
        MockHttpSession session = signUp("profile-pw-test@example.com", "비번테스터");

        mockMvc.perform(post("/my/profile/password").session(session).with(csrf())
                        .param("currentPassword", "wrong-password")
                        .param("newPassword", "newPassword1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("현재 비밀번호가 올바르지 않습니다")));
    }

    @Test
    void 현재_비밀번호가_맞으면_비밀번호가_바뀐다() throws Exception {
        MockHttpSession session = signUp("profile-pw-ok-test@example.com", "비번성공테스터");

        mockMvc.perform(post("/my/profile/password").session(session).with(csrf())
                        .param("currentPassword", "password1234")
                        .param("newPassword", "newPassword1"))
                .andExpect(redirectedUrl("/my/profile?passwordChanged"));
    }

    private MockHttpSession signUp(String email, String nickname) throws Exception {
        MvcResult signUpResult = mockMvc.perform(post("/members")
                        .with(csrf())
                        .param("email", email)
                        .param("nickname", nickname)
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) signUpResult.getRequest().getSession();
        assertThat(session).isNotNull();
        return session;
    }
}
