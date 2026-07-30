package com.edu.eduplatform.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.course.dto.PersonalCourseCreateRequest;
import com.edu.eduplatform.course.dto.PersonalCourseCreationResult;
import com.edu.eduplatform.course.exception.InvalidFocusAreasException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private CourseService courseService;

    @Test
    void createPersonalCourse_선택한_영역의_레슨만_복사해_개인_코스를_만든다() throws Exception {
        MemberResponse member = new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);
        when(courseRepository.findByOwnerIdOrderByIdDesc(1L)).thenReturn(Collections.emptyList());

        Course officialCourse = withId(Course.builder()
                .title("왕초보 회화").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(1).title("인사와 자기소개")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson writingLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(2).title("일기 쓰기")
                .content("내용").lessonType(LessonType.WRITING).build(), 11L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L))
                .thenReturn(List.of(vocabLesson, writingLesson));

        Course savedPersonalCourse = withId(Course.builder()
                .title("테스터님의 맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.VOCAB))
                .criteriaSource(CourseCriteriaSource.SELF_SELECTED).build(), 200L);
        when(courseRepository.save(any(Course.class))).thenReturn(savedPersonalCourse);

        PersonalCourseCreationResult result = courseService.createPersonalCourse(
                new PersonalCourseCreateRequest(1L, Set.of(LessonType.VOCAB)));

        assertThat(result.created()).isTrue();
        assertThat(result.course().ownerId()).isEqualTo(1L);
        assertThat(result.course().criteriaSource()).isEqualTo(CourseCriteriaSource.SELF_SELECTED);

        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(lessonCaptor.capture());
        Lesson copiedLesson = lessonCaptor.getValue();
        assertThat(copiedLesson.getCourseId()).isEqualTo(200L);
        assertThat(copiedLesson.getTitle()).isEqualTo("인사와 자기소개");
        assertThat(copiedLesson.getLessonType()).isEqualTo(LessonType.VOCAB);
        assertThat(copiedLesson.getOrderNo()).isEqualTo(1);
    }

    @Test
    void createPersonalCourse_영역을_선택하지_않으면_예외를_던진다() {
        assertThatThrownBy(() -> courseService.createPersonalCourse(
                new PersonalCourseCreateRequest(1L, Collections.emptySet())))
                .isInstanceOf(InvalidFocusAreasException.class);

        assertThatThrownBy(() -> courseService.createPersonalCourse(
                new PersonalCourseCreateRequest(1L, null)))
                .isInstanceOf(InvalidFocusAreasException.class);
    }

    @Test
    void createPersonalCourse_이미_같은_영역의_개인_코스가_있으면_새로_만들지_않고_기존_코스를_반환한다() throws Exception {
        MemberResponse member = new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);

        Course existingPersonalCourse = withId(Course.builder()
                .title("테스터님의 맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.VOCAB))
                .criteriaSource(CourseCriteriaSource.SELF_SELECTED).build(), 200L);
        when(courseRepository.findByOwnerIdOrderByIdDesc(1L)).thenReturn(List.of(existingPersonalCourse));

        PersonalCourseCreationResult result = courseService.createPersonalCourse(
                new PersonalCourseCreateRequest(1L, Set.of(LessonType.VOCAB)));

        assertThat(result.created()).isFalse();
        assertThat(result.course().id()).isEqualTo(200L);
        verify(courseRepository, never()).save(any(Course.class));
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    private static <T> T withId(T entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return entity;
    }
}
