package com.edu.eduplatform.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.common.dto.CoverageReportRow;
import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import com.edu.eduplatform.question.repository.QuestionRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentCoverageServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private ContentCoverageService contentCoverageService;

    @Test
    void getCoverageReport_8개_조합_전부를_반환한다() {
        when(courseRepository.search(any(), any(), isNull(), isNull())).thenReturn(List.of());
        when(lessonRepository.findByCourseIdIn(any())).thenReturn(List.of());
        when(questionRepository.findByTargetTypeAndLevel(any(), any())).thenReturn(List.of());

        List<CoverageReportRow> result = contentCoverageService.getCoverageReport();

        assertThat(result).hasSize(MemberType.values().length * EnglishLevel.values().length);
        assertThat(result).allMatch(row -> row.lessonCountByType().isEmpty() && row.questionCountByType().isEmpty());
    }

    @Test
    void getCoverageReport_영역별_레슨_문항_수를_정확히_집계한다() throws Exception {
        when(courseRepository.search(any(), any(), isNull(), isNull())).thenReturn(List.of());
        when(lessonRepository.findByCourseIdIn(any())).thenReturn(List.of());
        when(questionRepository.findByTargetTypeAndLevel(any(), any())).thenReturn(List.of());

        Course course = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null)).thenReturn(List.of(course));

        Lesson vocab1 = Lesson.builder().courseId(100L).orderNo(1).title("1과").content("내용").lessonType(LessonType.VOCAB).build();
        Lesson vocab2 = Lesson.builder().courseId(100L).orderNo(2).title("2과").content("내용").lessonType(LessonType.VOCAB).build();
        Lesson writing1 = Lesson.builder().courseId(100L).orderNo(3).title("3과").content("내용").lessonType(LessonType.WRITING).build();
        when(lessonRepository.findByCourseIdIn(List.of(100L))).thenReturn(List.of(vocab1, vocab2, writing1));

        Question q1 = Question.builder().targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .lessonType(LessonType.VOCAB).prompt("문제").options(List.of("a", "b", "c", "d")).correctOptionIndex(0).build();
        when(questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER)).thenReturn(List.of(q1));

        List<CoverageReportRow> result = contentCoverageService.getCoverageReport();

        CoverageReportRow targetRow = result.stream()
                .filter(row -> row.targetType() == MemberType.ADULT && row.level() == EnglishLevel.BEGINNER)
                .findFirst()
                .orElseThrow();
        assertThat(targetRow.lessonCountByType().get(LessonType.VOCAB)).isEqualTo(2);
        assertThat(targetRow.lessonCountByType().get(LessonType.WRITING)).isEqualTo(1);
        assertThat(targetRow.lessonCountByType().getOrDefault(LessonType.READING, 0)).isEqualTo(0);
        assertThat(targetRow.questionCountByType().get(LessonType.VOCAB)).isEqualTo(1);
    }

    private static Course withId(Course course, Long id) throws Exception {
        Field field = course.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(course, id);
        return course;
    }
}
