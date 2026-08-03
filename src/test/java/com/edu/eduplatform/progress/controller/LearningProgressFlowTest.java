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
class LearningProgressFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    void 회원가입하면_세션에_기록되어_로그인없이_마이페이지와_학습완료를_쓸_수_있다() throws Exception {
        MockHttpSession session = signUp("flow-test@example.com", "플로우테스터");
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
        MockHttpSession session = signUp("quiz-flow-test@example.com", "퀴즈플로우테스터");

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
    void 코스를_다_완료하면_상세_페이지에_다음_코스_안내가_보인다() throws Exception {
        MockHttpSession session = signUp("next-course-test@example.com", "다음코스테스터");

        Course courseA = courseRepository.save(Course.builder()
                .title("완료용코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lessonA = lessonRepository.save(Lesson.builder()
                .courseId(courseA.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        // courseA보다 id가 큰(=로드맵상 다음) 코스 — search()가 courseA 다음으로 이 코스를 찾아야 한다.
        Course courseB = courseRepository.save(Course.builder()
                .title("다음코스").description("설명").emoji("📗")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(courseB.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(get("/courses/{id}", courseA.getId()).session(session))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("이 코스를 모두 완료했어요"))));

        mockMvc.perform(post("/lessons/{id}/complete", lessonA.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/lessons/" + lessonA.getId()));

        mockMvc.perform(get("/courses/{id}", courseA.getId()).session(session))
                .andExpect(content().string(containsString("이 코스를 모두 완료했어요")))
                .andExpect(content().string(containsString("다음코스")));
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

    @Test
    void 세션이_없으면_복습_페이지는_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/my/review"))
                .andExpect(redirectedUrl("/login"));
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
