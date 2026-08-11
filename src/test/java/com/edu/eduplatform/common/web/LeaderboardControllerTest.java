package com.edu.eduplatform.common.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
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
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    void 비로그인_상태에서도_리더보드에_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("학습 리더보드")));
    }

    @Test
    void 로그인한_회원이_레슨을_완료하면_본인_행이_표시된다() throws Exception {
        MockHttpSession session = signUp("leaderboard-test@example.com", "리더보드테스터");
        Course course = courseRepository.save(Course.builder()
                .title("리더보드테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(post("/lessons/{id}/complete", lesson.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/lessons/" + lesson.getId()));

        mockMvc.perform(get("/leaderboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("리더보드테스터")))
                .andExpect(content().string(containsString("lesson-row me")));
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
                .andExpect(redirectedUrl("/members/new/verify"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) requestResult.getRequest().getSession();

        String code = emailVerificationService.issueCode(email);
        mockMvc.perform(post("/members/new/verify").session(session).with(csrf())
                        .param("code", code))
                .andExpect(redirectedUrl("/my"));

        return session;
    }
}
