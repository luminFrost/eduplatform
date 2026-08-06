package com.edu.eduplatform.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.member.security.EmailVerificationService;
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

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private MemberRepository memberRepository;

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
                        .param("level", "INTERMEDIATE")
                        .param("weeklyGoal", "0"))
                .andExpect(redirectedUrl("/my/profile?updated"));

        mockMvc.perform(get("/my/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("바뀐닉네임")));
    }

    @Test
    void 로그인한_회원은_주간_학습_목표를_설정할_수_있다() throws Exception {
        MockHttpSession session = signUp("profile-weekly-goal-test@example.com", "목표테스터");

        mockMvc.perform(post("/my/profile").session(session).with(csrf())
                        .param("nickname", "목표테스터")
                        .param("level", "BEGINNER")
                        .param("weeklyGoal", "5"))
                .andExpect(redirectedUrl("/my/profile?updated"));

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0 / 5개 (0%)")));
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

    @Test
    void 올바른_비밀번호로_탈퇴하면_계정이_삭제되고_로그아웃된다() throws Exception {
        String email = "withdraw-flow@example.com";
        MockHttpSession session = signUp(email, "탈퇴플로우테스터");

        mockMvc.perform(post("/my/profile/withdraw").session(session).with(csrf())
                        .param("password", "password1234"))
                .andExpect(redirectedUrl("/login?withdrawn"));

        assertThat(memberRepository.findByEmail(email)).isEmpty();

        mockMvc.perform(get("/my").session(session))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 틀린_비밀번호로_탈퇴하면_에러_메시지와_함께_계정이_남아있다() throws Exception {
        String email = "withdraw-wrong-flow@example.com";
        MockHttpSession session = signUp(email, "탈퇴실패테스터");

        mockMvc.perform(post("/my/profile/withdraw").session(session).with(csrf())
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("현재 비밀번호가 올바르지 않습니다")));

        assertThat(memberRepository.findByEmail(email)).isPresent();
    }

    private MockHttpSession signUp(String email, String nickname) throws Exception {
        MvcResult requestResult = mockMvc.perform(post("/members/new")
                        .with(csrf())
                        .param("email", email)
                        .param("nickname", nickname)
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) requestResult.getRequest().getSession();
        assertThat(session).isNotNull();

        String code = emailVerificationService.issueCode(email);
        mockMvc.perform(post("/members/new/verify").session(session).with(csrf())
                        .param("code", code))
                .andExpect(redirectedUrl("/my"));

        return session;
    }
}
