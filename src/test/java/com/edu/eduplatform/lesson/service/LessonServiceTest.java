package com.edu.eduplatform.lesson.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void getDetail_중간_레슨은_이전과_다음_레슨_id를_모두_가진다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 1L);
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과").content("내용1").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과").content("내용2").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson lesson3 = withLessonId(Lesson.builder().courseId(1L).orderNo(3).title("3과").content("내용3").lessonType(LessonType.VOCAB).build(), 12L);
        List<Lesson> siblings = List.of(lesson1, lesson2, lesson3);

        when(lessonRepository.findById(11L)).thenReturn(Optional.of(lesson2));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        LessonDetailResponse detail = lessonService.getDetail(11L);

        assertThat(detail.totalLessonsInCourse()).isEqualTo(3);
        assertThat(detail.prevLessonId()).isEqualTo(10L);
        assertThat(detail.nextLessonId()).isEqualTo(12L);
    }

    @Test
    void getDetail_첫_레슨은_이전_레슨이_없다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 1L);
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과").content("내용1").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과").content("내용2").lessonType(LessonType.VOCAB).build(), 11L);
        List<Lesson> siblings = List.of(lesson1, lesson2);

        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson1));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        LessonDetailResponse detail = lessonService.getDetail(10L);

        assertThat(detail.prevLessonId()).isNull();
        assertThat(detail.nextLessonId()).isEqualTo(11L);
    }

    private static Course withId(Course course, Long id) throws Exception {
        setField(course, "id", id);
        return course;
    }

    private static Lesson withLessonId(Lesson lesson, Long id) throws Exception {
        setField(lesson, "id", id);
        return lesson;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
