package com.edu.eduplatform.course.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseReview;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.course.repository.CourseReviewRepository;
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
class CourseReviewAdminControllerTest {

    private static final String RAW_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Test
    void 비로그인이면_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/admin/reviews"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 일반_회원은_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("review-admin-member@example.com", "일반회원", MemberRole.USER);

        mockMvc.perform(get("/admin/reviews").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_전체_리뷰를_보고_삭제할_수_있다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("리뷰관리테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Member reviewer = memberRepository.save(Member.builder()
                .email("review-author@example.com").nickname("리뷰작성자")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());
        CourseReview review = courseReviewRepository.save(CourseReview.builder()
                .memberId(reviewer.getId()).courseId(course.getId()).rating(1).comment("삭제될 리뷰").build());

        MockHttpSession session = loginAs("review-admin@example.com", "관리자", MemberRole.ADMIN);

        mockMvc.perform(get("/admin/reviews").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("리뷰관리테스트코스")))
                .andExpect(content().string(containsString("리뷰작성자")))
                .andExpect(content().string(containsString("삭제될 리뷰")));

        mockMvc.perform(post("/admin/reviews/{id}/delete", review.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/admin/reviews"));

        assertThat(courseReviewRepository.findById(review.getId())).isEmpty();
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
