package com.edu.eduplatform.progress.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ProgressApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 레슨완료_요청하면_204를_반환하고_진행_기록이_완료로_저장된다() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .email("progress-api-test@example.com").nickname("진도API테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Course course = courseRepository.save(Course.builder()
                .title("진도테스트코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        String requestBody = objectMapper.writeValueAsString(new CompleteRequest(member.getId(), lesson.getId()));

        mockMvc.perform(post("/api/progress/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        boolean completed = learningProgressRepository.findByMemberIdAndLessonId(member.getId(), lesson.getId())
                .map(p -> p.isCompleted())
                .orElse(false);
        assertThat(completed).isTrue();
    }

    @Test
    void memberId가_없으면_400을_반환한다() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new CompleteRequest(null, 1L));

        mockMvc.perform(post("/api/progress/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_회원이면_404를_반환한다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("진도테스트코스2").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        String requestBody = objectMapper.writeValueAsString(new CompleteRequest(999_999L, lesson.getId()));

        mockMvc.perform(post("/api/progress/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void 존재하지_않는_레슨이면_404를_반환한다() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .email("progress-api-test2@example.com").nickname("진도API테스터2")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        String requestBody = objectMapper.writeValueAsString(new CompleteRequest(member.getId(), 999_999L));

        mockMvc.perform(post("/api/progress/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    private record CompleteRequest(Long memberId, Long lessonId) {
    }
}
