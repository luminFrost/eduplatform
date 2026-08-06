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
class MemberAdminControllerTest {

    private static final String RAW_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 비로그인이면_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/admin/members"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 일반_회원은_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("member-admin-user@example.com", "일반회원", MemberRole.USER);

        mockMvc.perform(get("/admin/members").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_다른_회원을_승격하고_다시_강등할_수_있다() throws Exception {
        MockHttpSession session = loginAs("member-admin@example.com", "관리자", MemberRole.ADMIN);
        Member target = memberRepository.save(Member.builder()
                .email("promote-target@example.com").nickname("승격대상")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());
        assertThat(target.getRole()).isEqualTo(MemberRole.USER);

        mockMvc.perform(post("/admin/members/" + target.getId() + "/role").session(session).with(csrf())
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        Member promoted = memberRepository.findById(target.getId()).orElseThrow();
        assertThat(promoted.getRole()).isEqualTo(MemberRole.ADMIN);

        mockMvc.perform(post("/admin/members/" + target.getId() + "/role").session(session).with(csrf())
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection());

        Member demoted = memberRepository.findById(target.getId()).orElseThrow();
        assertThat(demoted.getRole()).isEqualTo(MemberRole.USER);
    }

    @Test
    void 관리자는_자기_자신을_강등할_수_없다() throws Exception {
        MockHttpSession session = loginAs("member-admin-self@example.com", "관리자2", MemberRole.ADMIN);
        Member self = memberRepository.findByEmail("member-admin-self@example.com").orElseThrow();

        MvcResult result = mockMvc.perform(post("/admin/members/" + self.getId() + "/role").session(session).with(csrf())
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result.getResponse().getRedirectedUrl()).contains("error=");

        Member stillAdmin = memberRepository.findById(self.getId()).orElseThrow();
        assertThat(stillAdmin.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void 관리자는_회원을_이메일로_검색할_수_있다() throws Exception {
        MockHttpSession session = loginAs("member-admin-search@example.com", "관리자3", MemberRole.ADMIN);
        memberRepository.save(Member.builder()
                .email("findme-luna@example.com").nickname("루나")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());

        mockMvc.perform(get("/admin/members").session(session).param("keyword", "luna"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("findme-luna@example.com")));
    }

    @Test
    void 관리자는_일반_회원을_강제_탈퇴시킬_수_있다() throws Exception {
        MockHttpSession session = loginAs("member-admin-force-withdraw@example.com", "강제탈퇴관리자", MemberRole.ADMIN);
        Member target = memberRepository.save(Member.builder()
                .email("force-withdraw-target@example.com").nickname("탈퇴대상")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());

        mockMvc.perform(post("/admin/members/" + target.getId() + "/withdraw").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(memberRepository.findById(target.getId())).isEmpty();
    }

    @Test
    void 관리자_계정을_대상으로_강제_탈퇴시도하면_에러와_함께_계정이_남는다() throws Exception {
        MockHttpSession session = loginAs("member-admin-force-withdraw-2@example.com", "강제탈퇴관리자2", MemberRole.ADMIN);
        Member otherAdmin = memberRepository.save(Member.builder()
                .email("force-withdraw-admin-target@example.com").nickname("다른관리자")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).role(MemberRole.ADMIN).build());

        MvcResult result = mockMvc.perform(post("/admin/members/" + otherAdmin.getId() + "/withdraw").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(result.getResponse().getRedirectedUrl()).contains("error=");

        assertThat(memberRepository.findById(otherAdmin.getId())).isPresent();
    }

    @Test
    void 일반_회원은_강제_탈퇴_요청시_403이다() throws Exception {
        MockHttpSession session = loginAs("member-admin-force-withdraw-user@example.com", "일반회원2", MemberRole.USER);
        Member target = memberRepository.save(Member.builder()
                .email("force-withdraw-target-2@example.com").nickname("탈퇴대상2")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());

        mockMvc.perform(post("/admin/members/" + target.getId() + "/withdraw").session(session).with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(memberRepository.findById(target.getId())).isPresent();
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
