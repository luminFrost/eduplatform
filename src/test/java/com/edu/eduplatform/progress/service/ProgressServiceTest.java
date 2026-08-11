package com.edu.eduplatform.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import com.edu.eduplatform.progress.dto.DailyActivityResponse;
import com.edu.eduplatform.progress.dto.DashboardSummaryResponse;
import com.edu.eduplatform.progress.dto.ReviewLessonResponse;
import com.edu.eduplatform.progress.dto.SkillAreaProgressResponse;
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.lang.reflect.Field;
import java.time.LocalDate;
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
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0));
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
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0));
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
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0));
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
    void isCourseFullyCompleted_모든_레슨을_완료하면_true() throws Exception {
        Lesson lesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of(lesson1, lesson2));

        LearningProgress done1 = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        done1.complete();
        LearningProgress done2 = LearningProgress.builder().memberId(1L).lessonId(11L).build();
        done2.complete();
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(done1, done2));

        assertThat(progressService.isCourseFullyCompleted(1L, 100L)).isTrue();
    }

    @Test
    void isCourseFullyCompleted_일부만_완료하면_false() throws Exception {
        Lesson lesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of(lesson1, lesson2));

        LearningProgress done1 = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        done1.complete();
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(done1));

        assertThat(progressService.isCourseFullyCompleted(1L, 100L)).isFalse();
    }

    @Test
    void isCourseFullyCompleted_레슨이_없는_코스는_false() {
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of());

        assertThat(progressService.isCourseFullyCompleted(1L, 100L)).isFalse();
    }

    @Test
    void getCourseCompletionDate_모두_완료하면_가장_늦은_완료시각을_반환한다() throws Exception {
        Lesson lesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of(lesson1, lesson2));

        LocalDateTime earlier = LocalDateTime.now().minusDays(1);
        LocalDateTime later = LocalDateTime.now();
        LearningProgress done1 = withCompletedAt(newCompleted(10L), earlier);
        LearningProgress done2 = withCompletedAt(newCompleted(11L), later);
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(done1, done2));

        assertThat(progressService.getCourseCompletionDate(1L, 100L)).contains(later);
    }

    @Test
    void getCourseCompletionDate_일부만_완료하면_빈_값을_반환한다() throws Exception {
        Lesson lesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of(lesson1, lesson2));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(newCompleted(10L)));

        assertThat(progressService.getCourseCompletionDate(1L, 100L)).isEmpty();
    }

    @Test
    void getCourseCompletionDate_레슨이_없는_코스는_빈_값을_반환한다() {
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(100L)).thenReturn(List.of());

        assertThat(progressService.getCourseCompletionDate(1L, 100L)).isEmpty();
    }

    @Test
    void getLearnerCount_리포지토리_결과를_그대로_반환한다() {
        when(learningProgressRepository.countDistinctMembersByCourseId(100L)).thenReturn(3L);

        assertThat(progressService.getLearnerCount(100L)).isEqualTo(3L);
    }

    @Test
    void getLearnerCounts_배치_결과를_Map으로_변환한다() {
        LearningProgressRepository.CourseLearnerCountProjection count100 =
                mock(LearningProgressRepository.CourseLearnerCountProjection.class);
        when(count100.getCourseId()).thenReturn(100L);
        when(count100.getLearnerCount()).thenReturn(2L);
        LearningProgressRepository.CourseLearnerCountProjection count200 =
                mock(LearningProgressRepository.CourseLearnerCountProjection.class);
        when(count200.getCourseId()).thenReturn(200L);
        when(count200.getLearnerCount()).thenReturn(5L);
        when(learningProgressRepository.countDistinctMembersByCourseIdIn(List.of(100L, 200L)))
                .thenReturn(List.of(count100, count200));

        var result = progressService.getLearnerCounts(List.of(100L, 200L));

        assertThat(result).containsEntry(100L, 2L).containsEntry(200L, 5L);
    }

    @Test
    void getLessonsDueForReview_기준일보다_오래된_완료_레슨을_반환한다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Lesson lesson = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        LearningProgress dueProgress = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        dueProgress.complete();

        when(learningProgressRepository.findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of(dueProgress));
        when(lessonRepository.findAllById(List.of(10L))).thenReturn(List.of(lesson));
        when(courseRepository.findAllById(List.of(100L))).thenReturn(List.of(course));

        List<ReviewLessonResponse> result = progressService.getLessonsDueForReview(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).lessonId()).isEqualTo(10L);
        assertThat(result.get(0).courseId()).isEqualTo(100L);
        assertThat(result.get(0).courseTitle()).isEqualTo("코스");
    }

    @Test
    void getLessonsDueForReview_복습_대상이_없으면_빈_목록을_반환한다() {
        when(learningProgressRepository.findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of());

        assertThat(progressService.getLessonsDueForReview(1L)).isEmpty();
    }

    @Test
    void getRecentActivity_완료한_레슨을_최신순으로_반환한다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Lesson lesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        LearningProgress older = withCompletedAt(newCompleted(10L), LocalDateTime.now().minusDays(1));
        LearningProgress newer = withCompletedAt(newCompleted(11L), LocalDateTime.now());
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(older, newer));
        when(lessonRepository.findAllById(any())).thenReturn(List.of(lesson1, lesson2));
        when(courseRepository.findAllById(any())).thenReturn(List.of(course));

        List<ReviewLessonResponse> result = progressService.getRecentActivity(1L);

        assertThat(result).extracting(ReviewLessonResponse::lessonId).containsExactly(11L, 10L);
    }

    @Test
    void getRecentActivity_완료_기록이_없으면_빈_목록을_반환한다() {
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        assertThat(progressService.getRecentActivity(1L)).isEmpty();
    }

    @Test
    void getRecentActivity_10개를_초과하면_최근_10개만_반환한다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        List<LearningProgress> progresses = new java.util.ArrayList<>();
        List<Lesson> lessons = new java.util.ArrayList<>();
        for (long i = 1; i <= 12; i++) {
            lessons.add(withId(Lesson.builder().courseId(100L).orderNo((int) i).title(i + "과")
                    .content("내용").lessonType(LessonType.VOCAB).build(), i));
            progresses.add(withCompletedAt(newCompleted(i), LocalDateTime.now().minusMinutes(12 - i)));
        }
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(progresses);
        when(lessonRepository.findAllById(any())).thenReturn(lessons);
        when(courseRepository.findAllById(any())).thenReturn(List.of(course));

        List<ReviewLessonResponse> result = progressService.getRecentActivity(1L);

        assertThat(result).hasSize(10);
        assertThat(result.get(0).lessonId()).isEqualTo(12L);
        assertThat(result.get(9).lessonId()).isEqualTo(3L);
    }

    @Test
    void countLessonsDueForReview_대상_개수를_반환한다() {
        LearningProgress due1 = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        due1.complete();
        LearningProgress due2 = LearningProgress.builder().memberId(1L).lessonId(11L).build();
        due2.complete();

        when(learningProgressRepository.findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of(due1, due2));

        assertThat(progressService.countLessonsDueForReview(1L)).isEqualTo(2);
    }

    @Test
    void countLessonsDueForReview_상한을_넘으면_상한에서_멈춘다() {
        List<LearningProgress> elevenDue = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> {
                    LearningProgress progress = LearningProgress.builder().memberId(1L).lessonId((long) i).build();
                    progress.complete();
                    return progress;
                })
                .toList();

        when(learningProgressRepository.findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(
                eq(1L), any(LocalDateTime.class))).thenReturn(elevenDue);

        assertThat(progressService.countLessonsDueForReview(1L)).isEqualTo(10);
    }

    @Test
    void markReviewed_완료_시각을_지금으로_밀어낸다() {
        LearningProgress progress = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        progress.complete();
        LocalDateTime originalCompletedAt = progress.getCompletedAt();
        when(learningProgressRepository.findByMemberIdAndLessonId(1L, 10L)).thenReturn(Optional.of(progress));

        progressService.markReviewed(1L, 10L);

        verify(learningProgressRepository).save(progress);
        assertThat(progress.getCompletedAt()).isAfterOrEqualTo(originalCompletedAt);
    }

    @Test
    void markReviewed_기록이_없으면_예외를_던진다() {
        when(learningProgressRepository.findByMemberIdAndLessonId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.markReviewed(1L, 10L))
                .isInstanceOf(LessonNotFoundException.class);
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
        when(lessonRepository.findByCourseIdIn(List.of(100L))).thenReturn(List.of(lesson1, lesson2));
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
    void getCompletedCourseCount_레슨을_전부_완료한_코스만_센다() throws Exception {
        Course doneCourse = withId(Course.builder()
                .title("완료 코스").description("설명").emoji("🗣️")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Course partialCourse = withId(Course.builder()
                .title("진행중 코스").description("설명").emoji("📖")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 200L);

        Lesson doneLesson = withId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson partialLesson1 = withId(Lesson.builder().courseId(200L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 20L);
        Lesson partialLesson2 = withId(Lesson.builder().courseId(200L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 21L);

        LearningProgress doneProgress = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        doneProgress.complete();
        LearningProgress partialProgress = LearningProgress.builder().memberId(1L).lessonId(20L).build();
        partialProgress.complete();

        when(learningProgressRepository.findByMemberId(1L))
                .thenReturn(List.of(doneProgress, partialProgress));
        when(lessonRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(doneLesson, partialLesson1));
        // touchedCourseIds는 HashMap 기반 스트림에서 나와 순서가 보장되지 않으므로 순서 무관 매처를 쓴다.
        when(lessonRepository.findByCourseIdIn(any()))
                .thenReturn(List.of(doneLesson, partialLesson1, partialLesson2));
        when(courseRepository.findAllById(any())).thenReturn(List.of(doneCourse, partialCourse));

        assertThat(progressService.getCompletedCourseCount(1L)).isEqualTo(1);
    }

    @Test
    void getCompletedCourseCount_기록이_없으면_0을_반환한다() {
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        assertThat(progressService.getCompletedCourseCount(1L)).isEqualTo(0);
    }

    @Test
    void recommendFocusAreas_레벨_내_전체_레슨_대비_커버리지가_가장_낮은_영역을_추천한다() throws Exception {
        MemberResponse member = new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0);
        when(memberService.getMember(1L)).thenReturn(member);

        Course officialCourse = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("어휘1")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson vocabLesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("어휘2")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson writingLesson1 = withId(Lesson.builder().courseId(100L).orderNo(3).title("쓰기1")
                .content("내용").lessonType(LessonType.WRITING).build(), 20L);
        Lesson writingLesson2 = withId(Lesson.builder().courseId(100L).orderNo(4).title("쓰기2")
                .content("내용").lessonType(LessonType.WRITING).build(), 21L);
        when(lessonRepository.findByCourseIdIn(List.of(100L)))
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
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0);
        when(memberService.getMember(1L)).thenReturn(member);

        Course officialCourse = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null))
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
        when(lessonRepository.findByCourseIdIn(List.of(100L)))
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
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0));
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

    @Test
    void getSkillAreaProgress_영역별_완료_대비_전체_레슨_수를_반환한다() throws Exception {
        MemberResponse member = new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0);
        when(memberService.getMember(1L)).thenReturn(member);

        Course officialCourse = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("어휘1")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson vocabLesson2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("어휘2")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson writingLesson1 = withId(Lesson.builder().courseId(100L).orderNo(3).title("쓰기1")
                .content("내용").lessonType(LessonType.WRITING).build(), 20L);
        Lesson writingLesson2 = withId(Lesson.builder().courseId(100L).orderNo(4).title("쓰기2")
                .content("내용").lessonType(LessonType.WRITING).build(), 21L);
        when(lessonRepository.findByCourseIdIn(List.of(100L)))
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

        List<SkillAreaProgressResponse> result = progressService.getSkillAreaProgress(1L);

        assertThat(result).hasSize(2);
        // LessonType.values() 순서(VOCAB → WRITING)로 반환된다.
        assertThat(result.get(0).lessonType()).isEqualTo(LessonType.VOCAB);
        assertThat(result.get(0).completedLessons()).isEqualTo(2);
        assertThat(result.get(0).totalLessons()).isEqualTo(2);
        assertThat(result.get(0).percentage()).isEqualTo(100);
        assertThat(result.get(1).lessonType()).isEqualTo(LessonType.WRITING);
        assertThat(result.get(1).completedLessons()).isEqualTo(1);
        assertThat(result.get(1).totalLessons()).isEqualTo(2);
        assertThat(result.get(1).percentage()).isEqualTo(50);
    }

    @Test
    void getDashboardSummary_코스_진도를_합산해_전체_통계를_계산한다() throws Exception {
        Course courseA = withId(Course.builder()
                .title("코스A").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Course courseB = withId(Course.builder()
                .title("코스B").description("설명").emoji("📗")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 200L);

        Lesson lessonA1 = withId(Lesson.builder().courseId(100L).orderNo(1).title("A1")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lessonA2 = withId(Lesson.builder().courseId(100L).orderNo(2).title("A2")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson lessonB1 = withId(Lesson.builder().courseId(200L).orderNo(1).title("B1")
                .content("내용").lessonType(LessonType.READING).build(), 20L);
        Lesson lessonB2 = withId(Lesson.builder().courseId(200L).orderNo(2).title("B2")
                .content("내용").lessonType(LessonType.READING).build(), 21L);

        // 코스A: 2개 중 1개 완료, 코스B: 2개 중 2개 완료 → 총 3/4 = 75%
        LearningProgress progressA1 = LearningProgress.builder().memberId(1L).lessonId(10L).build();
        progressA1.complete();
        LearningProgress progressA2 = LearningProgress.builder().memberId(1L).lessonId(11L).build();
        LearningProgress progressB1 = LearningProgress.builder().memberId(1L).lessonId(20L).build();
        progressB1.complete();
        LearningProgress progressB2 = LearningProgress.builder().memberId(1L).lessonId(21L).build();
        progressB2.complete();

        when(learningProgressRepository.findByMemberId(1L))
                .thenReturn(List.of(progressA1, progressA2, progressB1, progressB2));
        when(lessonRepository.findAllById(List.of(10L, 11L, 20L, 21L)))
                .thenReturn(List.of(lessonA1, lessonA2, lessonB1, lessonB2));
        // touchedCourseIds는 HashMap 기반 스트림에서 나와 순서가 보장되지 않으므로 순서 무관 매처를 쓴다.
        when(lessonRepository.findByCourseIdIn(any())).thenReturn(List.of(lessonA1, lessonA2, lessonB1, lessonB2));
        when(courseRepository.findAllById(any())).thenReturn(List.of(courseA, courseB));

        DashboardSummaryResponse summary = progressService.getDashboardSummary(1L);

        assertThat(summary.completedLessons()).isEqualTo(3);
        assertThat(summary.coursesInProgress()).isEqualTo(2);
        assertThat(summary.overallPercentage()).isEqualTo(75);
        assertThat(summary.currentStreak()).isEqualTo(1);
    }

    @Test
    void getDashboardSummary_기록이_없으면_전부_0이다() {
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        DashboardSummaryResponse summary = progressService.getDashboardSummary(1L);

        assertThat(summary.completedLessons()).isEqualTo(0);
        assertThat(summary.coursesInProgress()).isEqualTo(0);
        assertThat(summary.overallPercentage()).isEqualTo(0);
        assertThat(summary.currentStreak()).isEqualTo(0);
    }

    @Test
    void getCurrentStreak_오늘까지_연속으로_학습했으면_연속_일수를_반환한다() throws Exception {
        LearningProgress today = withCompletedAt(newCompleted(10L), LocalDateTime.now());
        LearningProgress yesterday = withCompletedAt(newCompleted(11L), LocalDateTime.now().minusDays(1));
        LearningProgress twoDaysAgo = withCompletedAt(newCompleted(12L), LocalDateTime.now().minusDays(2));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(today, yesterday, twoDaysAgo));

        assertThat(progressService.getCurrentStreak(1L)).isEqualTo(3);
    }

    @Test
    void getCurrentStreak_오늘_안_했어도_어제까지_이어져_있으면_유지된다() throws Exception {
        LearningProgress yesterday = withCompletedAt(newCompleted(10L), LocalDateTime.now().minusDays(1));
        LearningProgress twoDaysAgo = withCompletedAt(newCompleted(11L), LocalDateTime.now().minusDays(2));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(yesterday, twoDaysAgo));

        assertThat(progressService.getCurrentStreak(1L)).isEqualTo(2);
    }

    @Test
    void getCurrentStreak_어제_오늘_공백이면_끊긴_것으로_본다() throws Exception {
        LearningProgress threeDaysAgo = withCompletedAt(newCompleted(10L), LocalDateTime.now().minusDays(3));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(threeDaysAgo));

        assertThat(progressService.getCurrentStreak(1L)).isEqualTo(0);
    }

    @Test
    void getCurrentStreak_기록이_없으면_0을_반환한다() {
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        assertThat(progressService.getCurrentStreak(1L)).isEqualTo(0);
    }

    @Test
    void getMonthlyActivity_이번_달_1일부터_말일까지_날짜순으로_반환한다() throws Exception {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LearningProgress day1a = withCompletedAt(newCompleted(10L), start.atStartOfDay().plusHours(10));
        LearningProgress day1b = withCompletedAt(newCompleted(11L), start.atStartOfDay().plusHours(11));
        LearningProgress day1c = withCompletedAt(newCompleted(12L), start.atStartOfDay().plusHours(12));
        LearningProgress day3 = withCompletedAt(newCompleted(13L), start.plusDays(2).atStartOfDay().plusHours(9));
        when(learningProgressRepository.findByMemberId(1L))
                .thenReturn(List.of(day1a, day1b, day1c, day3));

        List<DailyActivityResponse> result = progressService.getMonthlyActivity(1L);

        int expectedDays = LocalDate.now().lengthOfMonth();
        assertThat(result).hasSize(expectedDays);
        assertThat(result.get(0).date()).isEqualTo(start);
        assertThat(result.get(0).completedCount()).isEqualTo(3);
        assertThat(result.get(2).date()).isEqualTo(start.plusDays(2));
        assertThat(result.get(2).completedCount()).isEqualTo(1);
        assertThat(result.get(1).completedCount()).isEqualTo(0);
        assertThat(result.get(expectedDays - 1).date()).isEqualTo(LocalDate.now().withDayOfMonth(expectedDays));
    }

    @Test
    void getMonthlyActivity_기록이_없으면_이번_달_전부_0으로_채운다() {
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        List<DailyActivityResponse> result = progressService.getMonthlyActivity(1L);

        assertThat(result).hasSize(LocalDate.now().lengthOfMonth());
        assertThat(result).allMatch(d -> d.completedCount() == 0);
    }

    @Test
    void getWeeklyGoalProgress_목표와_이번주_완료수를_함께_반환한다() throws Exception {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 5));
        LearningProgress today = withCompletedAt(newCompleted(10L), LocalDateTime.now());
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(today));

        var result = progressService.getWeeklyGoalProgress(1L);

        assertThat(result.goal()).isEqualTo(5);
        assertThat(result.completed()).isEqualTo(1);
    }

    @Test
    void getWeeklyGoalProgress_이번_주_이전에_완료한_레슨은_제외한다() throws Exception {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 5));
        LearningProgress lastWeek = withCompletedAt(newCompleted(10L), LocalDateTime.now().minusDays(10));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of(lastWeek));

        var result = progressService.getWeeklyGoalProgress(1L);

        assertThat(result.completed()).isEqualTo(0);
    }

    @Test
    void getWeeklyGoalProgress_목표를_설정하지_않았으면_0을_반환한다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(
                1L, "a@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now(), 0));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        var result = progressService.getWeeklyGoalProgress(1L);

        assertThat(result.goal()).isEqualTo(0);
        assertThat(result.percentage()).isEqualTo(0);
    }

    private static LearningProgress newCompleted(Long lessonId) {
        LearningProgress progress = LearningProgress.builder().memberId(1L).lessonId(lessonId).build();
        progress.complete();
        return progress;
    }

    private static LearningProgress withCompletedAt(LearningProgress progress, LocalDateTime completedAt) throws Exception {
        Field field = LearningProgress.class.getDeclaredField("completedAt");
        field.setAccessible(true);
        field.set(progress, completedAt);
        return progress;
    }

    private static <T> T withId(T entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return entity;
    }
}
