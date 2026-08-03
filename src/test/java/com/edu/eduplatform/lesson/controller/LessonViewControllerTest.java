package com.edu.eduplatform.lesson.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
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
class LessonViewControllerTest {

    private static final String RAW_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 비로그인이어도_첫_레슨은_콘텐츠가_그대로_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("잠금테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson1 = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("Hello. — 안녕.").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(get("/lessons/{id}", lesson1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello")))
                .andExpect(content().string(not(containsString("이 레슨은 회원만"))));
    }

    @Test
    void 비로그인이면_두번째_레슨부터_잠금_안내를_보여주고_콘텐츠는_감춘다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("잠금테스트코스2").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("Hello. — 안녕.").lessonType(LessonType.VOCAB).build());
        Lesson lesson2 = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(2).title("2과")
                .content("Goodbye. — 안녕히 가세요.").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(get("/lessons/{id}", lesson2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이 레슨은 회원만")))
                .andExpect(content().string(not(containsString("Goodbye"))));
    }

    @Test
    void 로그인한_회원은_두번째_레슨도_정상_열람한다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("잠금테스트코스3").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("Hello. — 안녕.").lessonType(LessonType.VOCAB).build());
        Lesson lesson2 = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(2).title("2과")
                .content("Goodbye. — 안녕히 가세요.").lessonType(LessonType.VOCAB).build());

        MockHttpSession session = loginAs("lesson-lock-member@example.com", "회원");

        mockMvc.perform(get("/lessons/{id}", lesson2.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Goodbye")))
                .andExpect(content().string(not(containsString("이 레슨은 회원만"))));
    }

    private MockHttpSession loginAs(String email, String nickname) throws Exception {
        memberRepository.save(Member.builder()
                .email(email).nickname(nickname)
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());

        MvcResult loginResult = mockMvc.perform(post("/login").with(csrf())
                        .param("email", email)
                        .param("password", RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }
}
