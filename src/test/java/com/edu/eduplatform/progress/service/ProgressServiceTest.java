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
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
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

    @Test
    void recommendFocusAreas_레벨_내_전체_레슨_대비_커버리지가_가장_낮은_영역을_추천한다() throws Exception {
        MemberResponse member = new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);

        Course officialCourse = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("어휘1")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson vocabLesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("어휘2")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson writingLesson1 = withId(Lesson.builder().courseId(100L).orderNo(3).title("쓰기1")
                .content("내용").lessonType(LessonType.WRITING).build(), 20L);
        Lesson writingLesson2 = withId(Lesson.builder().courseId(100L).orderNo(4).title("쓰기2")
                .content("내용").lessonType(LessonType.WRITING).build(), 21L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L))
                .thenReturn(List.of(vocabLesson1, vocabLesson2, writingLesson1, writingLesson2));

        // VOCAB 2개 다 완료, WRITING은 2개 중 1개만 완료
        LearningProgress vocabDone1 = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        vocabDone1.complete();
        LearningProgress vocabDone2 = LearningProgress.builder().memberId(1L).lessonId(11L).build();
        vocabDone2.complete();
        LearningProgress writingDone = LearningProgress.builder().memberId(1L).lessonId(20L).build();
        writingDone.complete();

        when(learningProgressRepository.findByMemberId(1L))
                .thenReturn(List.of(vocabDone1, vocabDone2, writingDone));
        when(lessonRepository.findAllById(List.of(10L, 11L, 20L)))
                .thenReturn(List.of(vocabLesson1, vocabLesson2, writingLesson1));

        // VOCAB 커버리지 100%(2/2), WRITING 커버리지 50%(1/2) → WRITING만 추천
        assertThat(progressService.recommendFocusAreas(1L)).containsExactly(LessonType.WRITING);
    }

    @Test
    void recommendFocusAreas_커버리지가_동률이면_모두_추천한다() throws Exception {
        MemberResponse member = new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);

        Course officialCourse = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("어휘1")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson vocabLesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("어휘2")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson writingLesson1 = withId(Lesson.builder().courseId(100L).orderNo(3).title("쓰기1")
                .content("내용").lessonType(LessonType.WRITING).build(), 20L);
        Lesson writingLesson2 = withId(Lesson.builder().courseId(100L).orderNo(4).title("쓰기2")
                .content("내용").lessonType(LessonType.WRITING).build(), 21L);
        Lesson readingLesson1 = withId(Lesson.builder().courseId(100L).orderNo(5).title("읽기1")
                .content("내용").lessonType(LessonType.READING).build(), 30L);
        Lesson readingLesson2 = withId(Lesson.builder().courseId(100L).orderNo(6).title("읽기2")
                .content("내용").lessonType(LessonType.READING).build(), 31L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L))
                .thenReturn(List.of(vocabLesson1, vocabLesson2, writingLesson1, writingLesson2, readingLesson1, readingLesson2));

        // VOCAB/WRITING/READING 모두 2개 중 1개만 완료 → 커버리지 50%로 동률
        LearningProgress vocabDone = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        vocabDone.complete();
        LearningProgress writingDone = LearningProgress.builder().memberId(1L).lessonId(20L).build();
        writingDone.complete();
        LearningProgress readingDone = LearningProgress.builder().memberId(1L).lessonId(30L).build();
        readingDone.complete();

        when(learningProgressRepository.findByMemberId(1L))
                .thenReturn(List.of(vocabDone, writingDone, readingDone));
        when(lessonRepository.findAllById(List.of(10L, 20L, 30L)))
                .thenReturn(List.of(vocabLesson1, writingLesson1, readingLesson1));

        assertThat(progressService.recommendFocusAreas(1L))
                .containsExactlyInAnyOrder(LessonType.VOCAB, LessonType.WRITING, LessonType.READING);
    }

    @Test
    void recommendFocusAreas_완료한_레슨이_적으면_예외를_던진다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now()));
        LearningProgress onlyOne = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        onlyOne.complete();
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(onlyOne));

        assertThatThrownBy(() -> progressService.recommendFocusAreas(1L))
                .isInstanceOf(InsufficientHistoryException.class);
    }

    @Test
    void recommendFocusAreas_존재하지_않는_회원이면_예외를_던진다() {
        when(memberService.getMember(999L)).thenThrow(new MemberNotFoundException(999L));

        assertThatThrownBy(() -> progressService.recommendFocusAreas(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    private static <T> T withId(T entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return entity;
    }
}
