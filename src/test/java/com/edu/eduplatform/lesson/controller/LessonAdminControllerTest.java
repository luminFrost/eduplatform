package com.edu.eduplatform.lesson.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
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
class LessonAdminControllerTest {

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
    private LessonRepository lessonRepository;

    @Test
    void 일반_회원은_레슨_생성에_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("lesson-admin-member@example.com", "일반회원", MemberRole.USER);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/admin/courses/" + course.getId() + "/lessons/new").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_레슨을_생성하고_수정하고_삭제할_수_있다() throws Exception {
        MockHttpSession session = loginAs("lesson-admin@example.com", "관리자", MemberRole.ADMIN);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(post("/admin/courses/" + course.getId() + "/lessons/new").session(session).with(csrf())
                        .param("title", "새 레슨")
                        .param("orderNo", "1")
                        .param("content", "Hello. — 안녕.")
                        .param("lessonType", "VOCAB"))
                .andExpect(status().is3xxRedirection());

        assertThat(lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId())).hasSize(1);
        Lesson created = lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId()).get(0);
        assertThat(created.getTitle()).isEqualTo("새 레슨");

        mockMvc.perform(post("/admin/lessons/" + created.getId() + "/edit").session(session).with(csrf())
                        .param("title", "수정된 레슨")
                        .param("orderNo", "1")
                        .param("content", "Hi. — 안녕.")
                        .param("lessonType", "VOCAB"))
                .andExpect(status().is3xxRedirection());

        Lesson updated = lessonRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("수정된 레슨");

        MvcResult deleteResult = mockMvc.perform(post("/admin/lessons/" + created.getId() + "/delete").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(deleteResult.getResponse().getRedirectedUrl()).contains("/admin/courses/" + course.getId());
        assertThat(lessonRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void 일반_회원은_레슨_순서_이동에_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("lesson-move-member@example.com", "일반회원", MemberRole.USER);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).title("1과").orderNo(1).content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(post("/admin/lessons/" + lesson.getId() + "/move").session(session).with(csrf())
                        .param("direction", "up"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_레슨_순서를_위아래로_이동할_수_있다() throws Exception {
        MockHttpSession session = loginAs("lesson-move-admin@example.com", "관리자", MemberRole.ADMIN);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson1 = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).title("1과").orderNo(1).content("내용1").lessonType(LessonType.VOCAB).build());
        Lesson lesson2 = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).title("2과").orderNo(2).content("내용2").lessonType(LessonType.VOCAB).build());

        MvcResult moveResult = mockMvc.perform(post("/admin/lessons/" + lesson2.getId() + "/move").session(session).with(csrf())
                        .param("direction", "up"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(moveResult.getResponse().getRedirectedUrl()).contains("/admin/courses/" + course.getId());
        assertThat(lessonRepository.findById(lesson2.getId()).orElseThrow().getOrderNo()).isEqualTo(1);
        assertThat(lessonRepository.findById(lesson1.getId()).orElseThrow().getOrderNo()).isEqualTo(2);
    }

    @Test
    void 일반_회원은_레슨_일괄_등록에_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("lesson-import-member@example.com", "일반회원", MemberRole.USER);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(post("/admin/courses/" + course.getId() + "/lessons/import").session(session).with(csrf())
                        .param("json", "[]"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_JSON_배열로_레슨_여러개를_한번에_등록할_수_있다() throws Exception {
        MockHttpSession session = loginAs("lesson-import-admin@example.com", "관리자", MemberRole.ADMIN);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).title("1과").orderNo(1).content("내용").lessonType(LessonType.VOCAB).build());

        String json = """
                [
                  {"title": "2과", "lessonType": "VOCAB", "content": "Hi. — 안녕."},
                  {"title": "3과", "lessonType": "READING", "content": "Bye. — 잘가."}
                ]
                """;

        mockMvc.perform(post("/admin/courses/" + course.getId() + "/lessons/import").session(session).with(csrf())
                        .param("json", json))
                .andExpect(status().is3xxRedirection());

        var lessons = lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId());
        assertThat(lessons).hasSize(3);
        assertThat(lessons).extracting(Lesson::getTitle).containsExactly("1과", "2과", "3과");
        assertThat(lessons).extracting(Lesson::getOrderNo).containsExactly(1, 2, 3);
    }

    @Test
    void 잘못된_JSON_형식이면_에러를_보여주고_아무것도_저장하지_않는다() throws Exception {
        MockHttpSession session = loginAs("lesson-import-malformed@example.com", "관리자", MemberRole.ADMIN);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(post("/admin/courses/" + course.getId() + "/lessons/import").session(session).with(csrf())
                        .param("json", "이건 JSON이 아니에요"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("JSON 형식을 확인해 주세요")));

        assertThat(lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId())).isEmpty();
    }

    @Test
    void 항목중_하나가_검증에_실패하면_행_번호와_함께_에러를_보여주고_아무것도_저장하지_않는다() throws Exception {
        MockHttpSession session = loginAs("lesson-import-invalid@example.com", "관리자", MemberRole.ADMIN);
        Course course = courseRepository.save(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        String json = """
                [
                  {"title": "1과", "lessonType": "VOCAB", "content": "Hi. — 안녕."},
                  {"title": "", "lessonType": "VOCAB", "content": "Bye. — 잘가."}
                ]
                """;

        mockMvc.perform(post("/admin/courses/" + course.getId() + "/lessons/import").session(session).with(csrf())
                        .param("json", json))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("2번째 항목")));

        assertThat(lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId())).isEmpty();
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
