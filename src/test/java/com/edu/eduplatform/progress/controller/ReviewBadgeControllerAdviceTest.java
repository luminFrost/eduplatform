package com.edu.eduplatform.progress.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewBadgeControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    void 복습_대상이_있으면_마이페이지_링크에_배지가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("배지테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        MockHttpSession session = signUp("badge-test@example.com", "배지테스터");

        mockMvc.perform(post("/lessons/{id}/complete", lesson.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/lessons/" + lesson.getId()));

        LearningProgress progress = learningProgressRepository.findByMemberIdAndLessonId(
                        currentMemberId(session), lesson.getId())
                .orElseThrow();
        setCompletedAt(progress, LocalDateTime.now().minusDays(4));
        learningProgressRepository.save(progress);

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("review-badge")))
                .andExpect(content().string(containsString(">1<")));
    }

    @Test
    void 복습_대상이_없으면_배지가_안_보인다() throws Exception {
        MockHttpSession session = signUp("badge-none-test@example.com", "배지없음테스터");

        mockMvc.perform(get("/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("review-badge"))));
    }

    @Test
    void 비로그인이면_배지가_안_보인다() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("review-badge"))));
    }

    private Long currentMemberId(MockHttpSession session) {
        Object principal = ((org.springframework.security.core.context.SecurityContext) session
                .getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getPrincipal();
        return ((com.edu.eduplatform.member.security.MemberPrincipal) principal).getMemberId();
    }

    private static void setCompletedAt(LearningProgress progress, LocalDateTime completedAt) throws Exception {
        Field field = LearningProgress.class.getDeclaredField("completedAt");
        field.setAccessible(true);
        field.set(progress, completedAt);
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
