package com.edu.eduplatform.course.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CourseApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    void 코스등록_성공하면_201과_코스정보를_반환한다() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new CreateRequest(
                "여행 영어 실전 표현", "공항, 숙소에서 바로 쓰는 표현", MemberType.ADULT, EnglishLevel.ELEMENTARY));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("여행 영어 실전 표현"))
                .andExpect(jsonPath("$.targetType").value("ADULT"));
    }

    @Test
    void 코스등록_제목이_비어있으면_400을_반환한다() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new CreateRequest(
                "", "설명", MemberType.ADULT, EnglishLevel.BEGINNER));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 코스목록_대상과_레벨로_필터링된다() throws Exception {
        courseRepository.save(Course.builder()
                .title("필터테스트코스").description("설명")
                .targetType(MemberType.CHILD).level(EnglishLevel.ADVANCED).build());

        mockMvc.perform(get("/api/courses").param("target", "CHILD").param("level", "ADVANCED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("필터테스트코스")))
                .andExpect(jsonPath("$[*].targetType", everyItem(is("CHILD"))))
                .andExpect(jsonPath("$[*].level", everyItem(is("ADVANCED"))));
    }

    @Test
    void 코스의_레슨목록은_orderNo_순으로_반환된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("레슨목록테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(2).title("2과").content("내용2").build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과").content("내용1").build());

        mockMvc.perform(get("/api/courses/{id}/lessons", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("1과"))
                .andExpect(jsonPath("$[1].title").value("2과"));
    }

    @Test
    void 존재하지_않는_코스의_레슨목록을_조회하면_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/courses/{id}/lessons", 999_999L))
                .andExpect(status().isNotFound());
    }

    private record CreateRequest(String title, String description, MemberType targetType, EnglishLevel level) {
    }
}
