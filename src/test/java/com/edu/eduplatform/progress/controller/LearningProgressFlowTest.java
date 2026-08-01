package com.edu.eduplatform.progress.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LearningProgressFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    void 회원가입하면_세션에_기록되어_로그인없이_마이페이지와_학습완료를_쓸_수_있다() throws Exception {
        MvcResult signUpResult = mockMvc.perform(post("/members")
                        .with(csrf())
                        .param("email", "flow-test@example.com")
                        .param("nickname", "플로우테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) signUpResult.getRequest().getSession();
        assertThat(session).isNotNull();

        Course course = courseRepository.save(Course.builder()
                .title("플로우테스트코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("플로우테스터")));

        mockMvc.perform(post("/lessons/{id}/complete", lesson.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/lessons/" + lesson.getId()));

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("플로우테스트코스")))
                .andExpect(content().string(containsString("1 / 1개 완료 (100%)")));
    }

    @Test
    void 세션이_없으면_마이페이지는_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/my"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void PHRASE_문장이_있는_레슨은_퀴즈에_틀리면_완료되지_않고_맞으면_완료된다() throws Exception {
        MvcResult signUpResult = mockMvc.perform(post("/members")
                        .with(csrf())
                        .param("email", "quiz-flow-test@example.com")
                        .param("nickname", "퀴즈플로우테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) signUpResult.getRequest().getSession();

        Course course = courseRepository.save(Course.builder()
                .title("퀴즈플로우코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson target = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("I would like a coffee, please. — 저는 커피를 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(2).title("2과")
                .content("I really enjoy summer vacation. — 나는 여름 방학을 정말 좋아해요.")
                .lessonType(LessonType.VOCAB).build());

        mockMvc.perform(post("/lessons/{id}/complete", target.getId()).session(session).with(csrf())
                        .param("quizAnswer", "vacation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("정답이 아니에요")));

        mockMvc.perform(get("/lessons/{id}", target.getId()).session(session))
                .andExpect(content().string(containsString("학습 완료")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("완료함 ✓"))));

        mockMvc.perform(post("/lessons/{id}/complete", target.getId()).session(session).with(csrf())
                        .param("quizAnswer", "coffee"))
                .andExpect(redirectedUrl("/lessons/" + target.getId()));

        mockMvc.perform(get("/lessons/{id}", target.getId()).session(session))
                .andExpect(content().string(containsString("완료함 ✓")));
    }

    @Test
    void 세션이_없으면_학습완료는_로그인으로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("비로그인코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        // CSRF 토큰 없이 보내면 CsrfFilter가 인가 로직보다 먼저 403을 반환해버려서
        // "로그인으로 리다이렉트"를 검증하려면 유효한 CSRF 토큰은 같이 줘야 한다.
        mockMvc.perform(post("/lessons/{id}/complete", lesson.getId()).with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }
}
