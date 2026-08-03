package com.edu.eduplatform.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseBookmark;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.course.dto.PersonalCourseCreationResult;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.exception.InvalidFocusAreasException;
import com.edu.eduplatform.course.repository.CourseBookmarkRepository;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
import com.edu.eduplatform.progress.service.ProgressService;
import com.edu.eduplatform.question.exception.DiagnosticTestIncompleteException;
import com.edu.eduplatform.question.service.QuestionService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Mock
    private ProgressService progressService;

    @Mock
    private QuestionService questionService;

    @Mock
    private CourseBookmarkRepository courseBookmarkRepository;

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
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(1).title("인사와 자기소개")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson writingLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(2).title("일기 쓰기")
                .content("내용").lessonType(LessonType.WRITING).build(), 11L);
        when(lessonRepository.findByCourseIdIn(List.of(100L)))
                .thenReturn(List.of(vocabLesson, writingLesson));

        Course savedPersonalCourse = withId(Course.builder()
                .title("테스터님의 맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.VOCAB))
                .criteriaSource(CourseCriteriaSource.SELF_SELECTED).build(), 200L);
        when(courseRepository.save(any(Course.class))).thenReturn(savedPersonalCourse);

        PersonalCourseCreationResult result = courseService.createPersonalCourse(1L, Set.of(LessonType.VOCAB));

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
        assertThatThrownBy(() -> courseService.createPersonalCourse(1L, Collections.emptySet()))
                .isInstanceOf(InvalidFocusAreasException.class);

        assertThatThrownBy(() -> courseService.createPersonalCourse(1L, null))
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

        PersonalCourseCreationResult result = courseService.createPersonalCourse(1L, Set.of(LessonType.VOCAB));

        assertThat(result.created()).isFalse();
        assertThat(result.course().id()).isEqualTo(200L);
        verify(courseRepository, never()).save(any(Course.class));
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void createPersonalCourseFromHistory_추천된_영역의_레슨만_복사해_개인_코스를_만든다() throws Exception {
        MemberResponse member = new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);
        when(progressService.recommendFocusAreas(1L)).thenReturn(Set.of(LessonType.WRITING));
        when(courseRepository.findByOwnerIdOrderByIdDesc(1L)).thenReturn(Collections.emptyList());

        Course officialCourse = withId(Course.builder()
                .title("왕초보 회화").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(1).title("인사와 자기소개")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson writingLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(2).title("일기 쓰기")
                .content("내용").lessonType(LessonType.WRITING).build(), 11L);
        when(lessonRepository.findByCourseIdIn(List.of(100L)))
                .thenReturn(List.of(vocabLesson, writingLesson));

        Course savedPersonalCourse = withId(Course.builder()
                .title("테스터님의 맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.WRITING))
                .criteriaSource(CourseCriteriaSource.HISTORY_BASED).build(), 200L);
        when(courseRepository.save(any(Course.class))).thenReturn(savedPersonalCourse);

        PersonalCourseCreationResult result = courseService.createPersonalCourseFromHistory(1L);

        assertThat(result.created()).isTrue();
        assertThat(result.course().ownerId()).isEqualTo(1L);
        assertThat(result.course().criteriaSource()).isEqualTo(CourseCriteriaSource.HISTORY_BASED);

        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(lessonCaptor.capture());
        Lesson copiedLesson = lessonCaptor.getValue();
        assertThat(copiedLesson.getTitle()).isEqualTo("일기 쓰기");
        assertThat(copiedLesson.getLessonType()).isEqualTo(LessonType.WRITING);
    }

    @Test
    void createPersonalCourseFromHistory_이력이_부족하면_예외를_그대로_전파하고_저장하지_않는다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now()));
        when(progressService.recommendFocusAreas(1L)).thenThrow(new InsufficientHistoryException());

        assertThatThrownBy(() -> courseService.createPersonalCourseFromHistory(1L))
                .isInstanceOf(InsufficientHistoryException.class);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void createPersonalCourseFromDiagnosticTest_채점된_영역의_레슨만_복사해_개인_코스를_만든다() throws Exception {
        MemberResponse member = new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);
        Map<Long, Integer> answers = Map.of(1000L, 0);
        when(questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, answers))
                .thenReturn(Set.of(LessonType.WRITING));
        when(courseRepository.findByOwnerIdOrderByIdDesc(1L)).thenReturn(Collections.emptyList());

        Course officialCourse = withId(Course.builder()
                .title("왕초보 회화").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null, null))
                .thenReturn(List.of(officialCourse));

        Lesson vocabLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(1).title("인사와 자기소개")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson writingLesson = withId(Lesson.builder()
                .courseId(100L).orderNo(2).title("일기 쓰기")
                .content("내용").lessonType(LessonType.WRITING).build(), 11L);
        when(lessonRepository.findByCourseIdIn(List.of(100L)))
                .thenReturn(List.of(vocabLesson, writingLesson));

        Course savedPersonalCourse = withId(Course.builder()
                .title("테스터님의 맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.WRITING))
                .criteriaSource(CourseCriteriaSource.DIAGNOSTIC_TEST).build(), 200L);
        when(courseRepository.save(any(Course.class))).thenReturn(savedPersonalCourse);

        PersonalCourseCreationResult result = courseService.createPersonalCourseFromDiagnosticTest(1L, answers);

        assertThat(result.created()).isTrue();
        assertThat(result.course().ownerId()).isEqualTo(1L);
        assertThat(result.course().criteriaSource()).isEqualTo(CourseCriteriaSource.DIAGNOSTIC_TEST);

        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(lessonCaptor.capture());
        Lesson copiedLesson = lessonCaptor.getValue();
        assertThat(copiedLesson.getTitle()).isEqualTo("일기 쓰기");
        assertThat(copiedLesson.getLessonType()).isEqualTo(LessonType.WRITING);
    }

    @Test
    void createPersonalCourseFromDiagnosticTest_문항을_다_안_풀면_예외를_그대로_전파하고_저장하지_않는다() {
        when(memberService.getMember(1L)).thenReturn(new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now()));
        Map<Long, Integer> incompleteAnswers = Map.of(1000L, 0);
        when(questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, incompleteAnswers))
                .thenThrow(new DiagnosticTestIncompleteException());

        assertThatThrownBy(() -> courseService.createPersonalCourseFromDiagnosticTest(1L, incompleteAnswers))
                .isInstanceOf(DiagnosticTestIncompleteException.class);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void createPersonalCourseFromDiagnosticTest_이미_같은_영역의_개인_코스가_있으면_새로_만들지_않고_기존_코스를_반환한다() throws Exception {
        MemberResponse member = new MemberResponse(1L, "a@example.com", "테스터",
                MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());
        when(memberService.getMember(1L)).thenReturn(member);
        Map<Long, Integer> answers = Map.of(1000L, 0);
        when(questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, answers))
                .thenReturn(Set.of(LessonType.VOCAB));

        Course existingPersonalCourse = withId(Course.builder()
                .title("테스터님의 맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.VOCAB))
                .criteriaSource(CourseCriteriaSource.DIAGNOSTIC_TEST).build(), 200L);
        when(courseRepository.findByOwnerIdOrderByIdDesc(1L)).thenReturn(List.of(existingPersonalCourse));

        PersonalCourseCreationResult result = courseService.createPersonalCourseFromDiagnosticTest(1L, answers);

        assertThat(result.created()).isFalse();
        assertThat(result.course().id()).isEqualTo(200L);
        verify(courseRepository, never()).save(any(Course.class));
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void recommendNextCourse_로드맵상_다음_미완료_코스를_반환한다() throws Exception {
        Course current = withId(Course.builder()
                .title("1번 코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Course next = withId(Course.builder()
                .title("2번 코스").description("설명").emoji("📗")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 101L);
        when(courseRepository.findById(100L)).thenReturn(java.util.Optional.of(current));
        when(courseRepository.search(MemberType.ADULT, null, null, null)).thenReturn(List.of(current, next));
        when(progressService.isCourseFullyCompleted(1L, 101L)).thenReturn(false);

        var result = courseService.recommendNextCourse(1L, 100L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(101L);
    }

    @Test
    void recommendNextCourse_이미_완료한_코스는_건너뛴다() throws Exception {
        Course current = withId(Course.builder()
                .title("1번 코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Course alreadyDone = withId(Course.builder()
                .title("2번 코스").description("설명").emoji("📗")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 101L);
        Course notYetDone = withId(Course.builder()
                .title("3번 코스").description("설명").emoji("📙")
                .targetType(MemberType.ADULT).level(EnglishLevel.ELEMENTARY).build(), 102L);
        when(courseRepository.findById(100L)).thenReturn(java.util.Optional.of(current));
        when(courseRepository.search(MemberType.ADULT, null, null, null))
                .thenReturn(List.of(current, alreadyDone, notYetDone));
        when(progressService.isCourseFullyCompleted(1L, 101L)).thenReturn(true);
        when(progressService.isCourseFullyCompleted(1L, 102L)).thenReturn(false);

        var result = courseService.recommendNextCourse(1L, 100L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(102L);
    }

    @Test
    void recommendNextCourse_로드맵_끝이면_빈_값을_반환한다() throws Exception {
        Course current = withId(Course.builder()
                .title("마지막 코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.ADVANCED).build(), 100L);
        when(courseRepository.findById(100L)).thenReturn(java.util.Optional.of(current));
        when(courseRepository.search(MemberType.ADULT, null, null, null)).thenReturn(List.of(current));

        var result = courseService.recommendNextCourse(1L, 100L);

        assertThat(result).isEmpty();
    }

    @Test
    void recommendNextCourse_개인_코스는_로드맵에_없어_빈_값을_반환한다() throws Exception {
        Course personalCourse = withId(Course.builder()
                .title("맞춤 코스").description("설명").emoji("🎯")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .ownerId(1L).focusAreas(Set.of(LessonType.VOCAB))
                .criteriaSource(CourseCriteriaSource.SELF_SELECTED).build(), 200L);
        Course officialCourse = withId(Course.builder()
                .title("공식 코스").description("설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.findById(200L)).thenReturn(java.util.Optional.of(personalCourse));
        // search()는 ownerId is null인 공식 코스만 반환 — personalCourse(200L)는 결과에 없다.
        when(courseRepository.search(MemberType.ADULT, null, null, null)).thenReturn(List.of(officialCourse));

        var result = courseService.recommendNextCourse(1L, 200L);

        assertThat(result).isEmpty();
    }

    @Test
    void toggleBookmark_북마크가_없으면_추가하고_true를_반환한다() {
        when(courseRepository.existsById(100L)).thenReturn(true);
        when(courseBookmarkRepository.existsByMemberIdAndCourseId(1L, 100L)).thenReturn(false);

        boolean result = courseService.toggleBookmark(1L, 100L);

        assertThat(result).isTrue();
        verify(courseBookmarkRepository).save(any(CourseBookmark.class));
    }

    @Test
    void toggleBookmark_북마크가_있으면_삭제하고_false를_반환한다() {
        when(courseRepository.existsById(100L)).thenReturn(true);
        when(courseBookmarkRepository.existsByMemberIdAndCourseId(1L, 100L)).thenReturn(true);

        boolean result = courseService.toggleBookmark(1L, 100L);

        assertThat(result).isFalse();
        verify(courseBookmarkRepository).deleteByMemberIdAndCourseId(1L, 100L);
        verify(courseBookmarkRepository, never()).save(any());
    }

    @Test
    void toggleBookmark_존재하지_않는_코스면_예외를_던진다() {
        when(courseRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> courseService.toggleBookmark(1L, 999L))
                .isInstanceOf(CourseNotFoundException.class);
        verify(courseBookmarkRepository, never()).save(any());
    }

    @Test
    void isBookmarked_존재여부를_그대로_반환한다() {
        when(courseBookmarkRepository.existsByMemberIdAndCourseId(1L, 100L)).thenReturn(true);

        assertThat(courseService.isBookmarked(1L, 100L)).isTrue();
    }

    @Test
    void listBookmarkedCourses_북마크_최신순으로_반환하고_삭제된_코스는_걸러진다() throws Exception {
        CourseBookmark bookmark1 = withId(CourseBookmark.builder().memberId(1L).courseId(100L).build(), 1L);
        CourseBookmark bookmark2 = withId(CourseBookmark.builder().memberId(1L).courseId(200L).build(), 2L);
        Course course200 = withId(Course.builder()
                .title("최근 북마크").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 200L);
        // course100(id=100)은 이미 삭제된 상태를 가정 — findAllById 결과에 없음.
        when(courseBookmarkRepository.findByMemberIdOrderByIdDesc(1L)).thenReturn(List.of(bookmark2, bookmark1));
        when(courseRepository.findAllById(List.of(200L, 100L))).thenReturn(List.of(course200));

        List<com.edu.eduplatform.course.dto.CourseResponse> result = courseService.listBookmarkedCourses(1L);

        assertThat(result).extracting(com.edu.eduplatform.course.dto.CourseResponse::id).containsExactly(200L);
    }

    @Test
    void updateCourse_존재하는_코스면_내용을_수정한다() throws Exception {
        Course course = withId(Course.builder()
                .title("기존 제목").description("기존 설명").emoji("📘")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        when(courseRepository.findById(100L)).thenReturn(java.util.Optional.of(course));

        var request = new com.edu.eduplatform.course.dto.CourseCreateRequest(
                "새 제목", "새 설명", "🆕", MemberType.CHILD, EnglishLevel.ADVANCED);
        var result = courseService.updateCourse(100L, request);

        assertThat(result.title()).isEqualTo("새 제목");
        assertThat(result.description()).isEqualTo("새 설명");
        assertThat(result.emoji()).isEqualTo("🆕");
        assertThat(result.targetType()).isEqualTo(MemberType.CHILD);
        assertThat(result.level()).isEqualTo(EnglishLevel.ADVANCED);
    }

    @Test
    void updateCourse_존재하지_않는_코스면_예외를_던진다() {
        when(courseRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        var request = new com.edu.eduplatform.course.dto.CourseCreateRequest(
                "새 제목", "새 설명", "🆕", MemberType.CHILD, EnglishLevel.ADVANCED);

        assertThatThrownBy(() -> courseService.updateCourse(999L, request))
                .isInstanceOf(com.edu.eduplatform.course.exception.CourseNotFoundException.class);
    }

    private static <T> T withId(T entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return entity;
    }
}
