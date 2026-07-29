package com.edu.eduplatform.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.dto.CourseProgressResponse;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private ProgressService progressService;

    @Test
    void complete_기존_기록이_없으면_새로_만들어_완료_처리한다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now()));
        when(lessonRepository.existsById(10L)).thenReturn(true);
        when(learningProgressRepository.findByMemberIdAndLessonId(1L, 10L)).thenReturn(Optional.empty());

        progressService.complete(1L, 10L);

        ArgumentCaptor<LearningProgress> captor = ArgumentCaptor.forClass(LearningProgress.class);
        verify(learningProgressRepository).save(captor.capture());
        assertThat(captor.getValue().isCompleted()).isTrue();
        assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
        assertThat(captor.getValue().getLessonId()).isEqualTo(10L);
    }

    @Test
    void complete_이미_완료된_기록이면_다시_완료_처리하지_않고_그대로_저장한다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now()));
        when(lessonRepository.existsById(10L)).thenReturn(true);
        LearningProgress existing = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        existing.complete();
        var firstCompletedAt = existing.getCompletedAt();
        when(learningProgressRepository.findByMemberIdAndLessonId(1L, 10L)).thenReturn(Optional.of(existing));

        progressService.complete(1L, 10L);

        verify(learningProgressRepository, times(1)).save(existing);
        assertThat(existing.getCompletedAt()).isEqualTo(firstCompletedAt);
    }

    @Test
    void complete_존재하지_않는_회원이면_예외를_던지고_기록하지_않는다() {
        when(memberService.getMember(999L)).thenThrow(new MemberNotFoundException(999L));

        assertThatThrownBy(() -> progressService.complete(999L, 10L))
                .isInstanceOf(MemberNotFoundException.class);
        verify(learningProgressRepository, never()).save(any());
    }

    @Test
    void complete_존재하지_않는_레슨이면_예외를_던지고_기록하지_않는다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now()));
        when(lessonRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> progressService.complete(1L, 999L))
                .isInstanceOf(LessonNotFoundException.class);
        verify(learningProgressRepository, never()).save(any());
    }

    @Test
    void isCompleted_기록이_없으면_false() {
        when(learningProgressRepository.findByMemberIdAndLessonId(1L, 10L)).thenReturn(Optional.empty());

        assertThat(progressService.isCompleted(1L, 10L)).isFalse();
        verify(learningProgressRepository, never()).save(any());
    }

    @Test
    void getCourseProgress_코스별로_완료_레슨수와_전체_레슨수를_집계한다() throws Exception {
        Course course = withId(Course.builder()
                .title("왕초보 회화").description("설명").emoji("🗣️")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);

        Lesson lesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);

        LearningProgress completedProgress = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        completedProgress.complete();
        LearningProgress incompleteProgress = LearningProgress.builder().memberId(1L).lessonId(11L).build();

        when(learningProgressRepository.findByMemberId(1L))
                .thenReturn(List.of(completedProgress, incompleteProgress));
        when(lessonRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(lesson1, lesson2));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of(lesson1, lesson2));
        when(courseRepository.findAllById(List.of(100L))).thenReturn(List.of(course));

        List<CourseProgressResponse> result = progressService.getCourseProgress(1L);

        assertThat(result).hasSize(1);
        CourseProgressResponse courseProgress = result.get(0);
        assertThat(courseProgress.courseId()).isEqualTo(100L);
        assertThat(courseProgress.totalLessons()).isEqualTo(2);
        assertThat(courseProgress.completedLessons()).isEqualTo(1);
        assertThat(courseProgress.percentage()).isEqualTo(50);
    }

    @Test
    void getCourseProgress_기록이_없으면_빈_목록을_반환한다() {
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        assertThat(progressService.getCourseProgress(1L)).isEmpty();
    }

    private static <T> T withId(T entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return entity;
    }
}
