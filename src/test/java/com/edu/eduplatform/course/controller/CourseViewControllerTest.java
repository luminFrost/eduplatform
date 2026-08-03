package com.edu.eduplatform.course.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CourseViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    void 코스목록_잘못된_target값이면_500대신_필터없이_보여준다() throws Exception {
        mockMvc.perform(get("/courses").param("target", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스목록_잘못된_level값이면_500대신_필터없이_보여준다() throws Exception {
        mockMvc.perform(get("/courses").param("level", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스상세_잘못된_type값이면_500대신_전체_레슨을_보여준다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("뷰컨트롤러테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}", course.getId()).param("type", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스상세_비로그인이면_두번째_레슨부터_잠금_배지가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("배지테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("🔒 회원 전용")));
    }
}
