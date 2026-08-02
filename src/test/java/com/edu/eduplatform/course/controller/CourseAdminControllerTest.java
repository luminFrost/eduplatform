package com.edu.eduplatform.course.controller;

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
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
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
class CourseAdminControllerTest {

    private static final String RAW_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 비로그인이면_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/admin/courses"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 일반_회원은_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("course-admin-member@example.com", "일반회원", MemberRole.USER);

        mockMvc.perform(get("/admin/courses").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_코스를_생성하고_상세에서_확인할_수_있다() throws Exception {
        MockHttpSession session = loginAs("course-admin@example.com", "관리자", MemberRole.ADMIN);

        MvcResult result = mockMvc.perform(post("/admin/courses/new").session(session).with(csrf())
                        .param("title", "관리자가 만든 코스")
                        .param("description", "설명")
                        .param("emoji", "🆕")
                        .param("targetType", "ADULT")
                        .param("level", "BEGINNER"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).matches(".*/admin/courses/\\d+");

        mockMvc.perform(get(redirectedUrl).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("관리자가 만든 코스")));
    }

    @Test
    void 관리자는_코스_제목을_비우면_검증_오류로_폼이_그대로_남는다() throws Exception {
        MockHttpSession session = loginAs("course-admin-invalid@example.com", "관리자2", MemberRole.ADMIN);

        mockMvc.perform(post("/admin/courses/new").session(session).with(csrf())
                        .param("title", "")
                        .param("description", "설명")
                        .param("targetType", "ADULT")
                        .param("level", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("코스명을 입력해 주세요")));
    }

    private MockHttpSession loginAs(String email, String nickname, MemberRole role) throws Exception {
        memberRepository.save(Member.builder()
                .email(email).nickname(nickname)
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).role(role).build());

        MvcResult loginResult = mockMvc.perform(post("/login").with(csrf())
                        .param("email", email)
                        .param("password", RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        assertThat(session).isNotNull();
        return session;
    }
}
