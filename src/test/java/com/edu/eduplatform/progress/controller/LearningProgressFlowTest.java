package com.edu.eduplatform.progress.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
                        .param("email", "flow-test@example.com")
                        .param("nickname", "플로우테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER"))
                .andExpect(status().is3xxRedirection())
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

        mockMvc.perform(post("/lessons/{id}/complete", lesson.getId()).session(session))
                .andExpect(redirectedUrl("/lessons/" + lesson.getId()));

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("플로우테스트코스")))
                .andExpect(content().string(containsString("1 / 1개 완료 (100%)")));
    }

    @Test
    void 세션이_없으면_마이페이지는_회원가입으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/my"))
                .andExpect(redirectedUrl("/members/new"));
    }

    @Test
    void 세션이_없으면_학습완료는_회원가입으로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("비로그인코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(post("/lessons/{id}/complete", lesson.getId()))
                .andExpect(redirectedUrl("/members/new"));
    }
}
