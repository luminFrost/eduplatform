package com.edu.eduplatform.lesson.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse;
import com.edu.eduplatform.lesson.dto.LessonSearchResultResponse;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.lesson.service.LessonService.IconPair;
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

    @Mock
    private IconCatalog iconCatalog;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void getLessonCounts_courseId_여러_개의_레슨_수를_한번에_집계한다() throws Exception {
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(100L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(100L).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson lesson3 = withLessonId(Lesson.builder().courseId(200L).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build(), 12L);
        when(lessonRepository.findByCourseIdIn(List.of(100L, 200L))).thenReturn(List.of(lesson1, lesson2, lesson3));

        var result = lessonService.getLessonCounts(List.of(100L, 200L));

        assertThat(result).containsEntry(100L, 2L).containsEntry(200L, 1L);
    }

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
        assertThat(detail.lessonType()).isEqualTo(LessonType.VOCAB);
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
        assertThat(detail.lessonType()).isEqualTo(LessonType.VOCAB);
    }

    @Test
    void deriveQuizAnswer_PHRASE_문장에서_불용어를_제외한_가장_긴_단어를_뽑는다() throws Exception {
        Lesson lesson = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과")
                .content("I would like a coffee, please. — 저는 커피를 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        Optional<String> answer = lessonService.deriveQuizAnswer(10L);

        assertThat(answer).contains("coffee");
    }

    @Test
    void deriveQuizAnswer_PHRASE_줄이_없으면_빈_값을_반환한다() throws Exception {
        Lesson lesson = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과")
                .content("A는 Apple(사과)의 A예요.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        Optional<String> answer = lessonService.deriveQuizAnswer(10L);

        assertThat(answer).isEmpty();
    }

    @Test
    void getDetail_오답_보기를_같은_코스_다른_레슨들의_PHRASE_문장에서_모은다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 1L);
        Lesson target = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과")
                .content("I would like a coffee, please. — 저는 커피를 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        Lesson sibling1 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과")
                .content("I really enjoy summer vacation. — 나는 여름 방학을 정말 좋아해요.")
                .lessonType(LessonType.VOCAB).build(), 11L);
        Lesson sibling2 = withLessonId(Lesson.builder().courseId(1L).orderNo(3).title("3과")
                .content("The weather is beautiful today. — 오늘 날씨가 아름다워요.")
                .lessonType(LessonType.VOCAB).build(), 12L);
        List<Lesson> siblings = List.of(target, sibling1, sibling2);

        when(lessonRepository.findById(10L)).thenReturn(Optional.of(target));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        LessonDetailResponse detail = lessonService.getDetail(10L);

        assertThat(detail.quiz()).isNotNull();
        assertThat(detail.quiz().sentenceWithBlank()).contains("___").doesNotContain("coffee");
        assertThat(detail.quiz().translation()).isEqualTo("저는 커피를 부탁드립니다.");
        assertThat(detail.quiz().options())
                .contains("coffee", "vacation", "beautiful")
                .hasSize(3);
    }

    @Test
    void getDetail_오답_후보가_없으면_퀴즈를_생략한다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 1L);
        Lesson target = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과")
                .content("I would like a coffee, please. — 저는 커피를 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        List<Lesson> siblings = List.of(target);

        when(lessonRepository.findById(10L)).thenReturn(Optional.of(target));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        LessonDetailResponse detail = lessonService.getDetail(10L);

        assertThat(detail.quiz()).isNull();
    }

    @Test
    void collectIconPairs_BEGINNER와_ELEMENTARY_레벨의_아이콘_붙은_단어만_모은다() throws Exception {
        Course beginnerCourse = withId(Course.builder()
                .title("입문 코스").description("설명")
                .targetType(MemberType.CHILD).level(EnglishLevel.BEGINNER).build(), 1L);
        Course elementaryCourse = withId(Course.builder()
                .title("초급 코스").description("설명")
                .targetType(MemberType.CHILD).level(EnglishLevel.ELEMENTARY).build(), 2L);
        Lesson beginnerLesson = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과")
                .content("🍎 An apple is red. — 사과는 빨간색이에요.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        Lesson elementaryLesson = withLessonId(Lesson.builder().courseId(2L).orderNo(1).title("1과")
                .content("🐘 A big elephant walks. — 큰 코끼리가 걸어요.")
                .lessonType(LessonType.VOCAB).build(), 20L);

        when(courseRepository.search(MemberType.CHILD, EnglishLevel.BEGINNER, null, null)).thenReturn(List.of(beginnerCourse));
        when(courseRepository.search(MemberType.CHILD, EnglishLevel.ELEMENTARY, null, null)).thenReturn(List.of(elementaryCourse));
        when(lessonRepository.findByCourseIdIn(List.of(1L, 2L))).thenReturn(List.of(beginnerLesson, elementaryLesson));
        when(iconCatalog.resolveImagePath("🍎")).thenReturn("/images/openmoji/1F34E.svg");
        when(iconCatalog.resolveImagePath("🐘")).thenReturn("/images/openmoji/1F418.svg");

        List<IconPair> pairs = lessonService.collectIconPairs(MemberType.CHILD);

        assertThat(pairs).extracting(IconPair::word).containsExactlyInAnyOrder("apple", "elephant");
        verify(courseRepository, never()).search(eq(MemberType.CHILD), eq(EnglishLevel.ADVANCED), any(), any());
        verify(courseRepository, never()).search(eq(MemberType.CHILD), eq(EnglishLevel.INTERMEDIATE), any(), any());
    }

    @Test
    void createLesson_존재하는_코스면_레슨을_저장한다() {
        when(courseRepository.existsById(100L)).thenReturn(true);
        var request = new com.edu.eduplatform.lesson.dto.LessonAdminRequest("새 레슨", 1, "내용", LessonType.VOCAB);

        lessonService.createLesson(100L, request);

        org.mockito.ArgumentCaptor<Lesson> captor = org.mockito.ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        assertThat(captor.getValue().getCourseId()).isEqualTo(100L);
        assertThat(captor.getValue().getTitle()).isEqualTo("새 레슨");
    }

    @Test
    void createLesson_존재하지_않는_코스면_예외를_던진다() {
        when(courseRepository.existsById(999L)).thenReturn(false);
        var request = new com.edu.eduplatform.lesson.dto.LessonAdminRequest("새 레슨", 1, "내용", LessonType.VOCAB);

        assertThatThrownBy(() -> lessonService.createLesson(999L, request))
                .isInstanceOf(com.edu.eduplatform.course.exception.CourseNotFoundException.class);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateLesson_존재하는_레슨이면_내용을_수정한다() throws Exception {
        Lesson lesson = withLessonId(Lesson.builder()
                .courseId(100L).orderNo(1).title("기존 제목")
                .content("기존 내용").lessonType(LessonType.VOCAB).build(), 10L);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        var request = new com.edu.eduplatform.lesson.dto.LessonAdminRequest("새 제목", 2, "새 내용", LessonType.WRITING);

        lessonService.updateLesson(10L, request);

        assertThat(lesson.getTitle()).isEqualTo("새 제목");
        assertThat(lesson.getOrderNo()).isEqualTo(2);
        assertThat(lesson.getContent()).isEqualTo("새 내용");
        assertThat(lesson.getLessonType()).isEqualTo(LessonType.WRITING);
        verify(lessonRepository).save(lesson);
    }

    @Test
    void updateLesson_존재하지_않는_레슨이면_예외를_던진다() {
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());
        var request = new com.edu.eduplatform.lesson.dto.LessonAdminRequest("새 제목", 1, "내용", LessonType.VOCAB);

        assertThatThrownBy(() -> lessonService.updateLesson(999L, request))
                .isInstanceOf(com.edu.eduplatform.lesson.exception.LessonNotFoundException.class);
    }

    @Test
    void deleteLesson_존재하는_레슨이면_삭제한다() {
        when(lessonRepository.existsById(10L)).thenReturn(true);

        lessonService.deleteLesson(10L);

        verify(lessonRepository).deleteById(10L);
    }

    @Test
    void deleteLesson_존재하지_않는_레슨이면_예외를_던진다() {
        when(lessonRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> lessonService.deleteLesson(999L))
                .isInstanceOf(com.edu.eduplatform.lesson.exception.LessonNotFoundException.class);
        verify(lessonRepository, never()).deleteById(any());
    }

    @Test
    void moveLesson_위로_이동하면_인접한_레슨과_orderNo가_바뀐다() throws Exception {
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과").content("내용1").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과").content("내용2").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson lesson3 = withLessonId(Lesson.builder().courseId(1L).orderNo(3).title("3과").content("내용3").lessonType(LessonType.VOCAB).build(), 12L);
        List<Lesson> siblings = List.of(lesson1, lesson2, lesson3);

        when(lessonRepository.findById(11L)).thenReturn(Optional.of(lesson2));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        lessonService.moveLesson(11L, "up");

        assertThat(lesson2.getOrderNo()).isEqualTo(1);
        assertThat(lesson1.getOrderNo()).isEqualTo(2);
        assertThat(lesson3.getOrderNo()).isEqualTo(3);
    }

    @Test
    void moveLesson_아래로_이동하면_인접한_레슨과_orderNo가_바뀐다() throws Exception {
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과").content("내용1").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과").content("내용2").lessonType(LessonType.VOCAB).build(), 11L);
        Lesson lesson3 = withLessonId(Lesson.builder().courseId(1L).orderNo(3).title("3과").content("내용3").lessonType(LessonType.VOCAB).build(), 12L);
        List<Lesson> siblings = List.of(lesson1, lesson2, lesson3);

        when(lessonRepository.findById(11L)).thenReturn(Optional.of(lesson2));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        lessonService.moveLesson(11L, "down");

        assertThat(lesson2.getOrderNo()).isEqualTo(3);
        assertThat(lesson3.getOrderNo()).isEqualTo(2);
        assertThat(lesson1.getOrderNo()).isEqualTo(1);
    }

    @Test
    void moveLesson_맨_위에서_위로_이동하면_아무것도_바뀌지_않는다() throws Exception {
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과").content("내용1").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과").content("내용2").lessonType(LessonType.VOCAB).build(), 11L);
        List<Lesson> siblings = List.of(lesson1, lesson2);

        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        lessonService.moveLesson(10L, "up");

        assertThat(lesson1.getOrderNo()).isEqualTo(1);
        assertThat(lesson2.getOrderNo()).isEqualTo(2);
    }

    @Test
    void moveLesson_맨_아래에서_아래로_이동하면_아무것도_바뀌지_않는다() throws Exception {
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과").content("내용1").lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과").content("내용2").lessonType(LessonType.VOCAB).build(), 11L);
        List<Lesson> siblings = List.of(lesson1, lesson2);

        when(lessonRepository.findById(11L)).thenReturn(Optional.of(lesson2));
        when(lessonRepository.findByCourseIdOrderByOrderNoAsc(1L)).thenReturn(siblings);

        lessonService.moveLesson(11L, "down");

        assertThat(lesson1.getOrderNo()).isEqualTo(1);
        assertThat(lesson2.getOrderNo()).isEqualTo(2);
    }

    @Test
    void moveLesson_존재하지_않는_레슨이면_예외를_던진다() {
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.moveLesson(999L, "up"))
                .isInstanceOf(com.edu.eduplatform.lesson.exception.LessonNotFoundException.class);
    }

    @Test
    void searchLessons_매칭된_레슨을_코스_정보와_스니펫과_함께_반환한다() throws Exception {
        Course course = withId(Course.builder()
                .title("여행 영어").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.ELEMENTARY).build(), 100L);
        Lesson lesson = withLessonId(Lesson.builder()
                .courseId(100L).orderNo(1).title("1과")
                .content("I would like a coffee, please. — 저는 커피를 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        when(lessonRepository.searchByContent("coffee")).thenReturn(List.of(lesson));
        when(courseRepository.findAllById(List.of(100L))).thenReturn(List.of(course));

        List<LessonSearchResultResponse> result = lessonService.searchLessons("coffee");

        assertThat(result).hasSize(1);
        LessonSearchResultResponse r = result.get(0);
        assertThat(r.lessonId()).isEqualTo(10L);
        assertThat(r.courseId()).isEqualTo(100L);
        assertThat(r.courseTitle()).isEqualTo("여행 영어");
        assertThat(r.snippetText()).isEqualTo("I would like a coffee, please.");
        assertThat(r.snippetSubtext()).isEqualTo("저는 커피를 부탁드립니다.");
    }

    @Test
    void searchLessons_한글_번역에만_키워드가_있어도_찾는다() throws Exception {
        Course course = withId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.ELEMENTARY).build(), 100L);
        Lesson lesson = withLessonId(Lesson.builder()
                .courseId(100L).orderNo(1).title("1과")
                .content("I need help. — 도와주세요 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        when(lessonRepository.searchByContent("부탁")).thenReturn(List.of(lesson));
        when(courseRepository.findAllById(List.of(100L))).thenReturn(List.of(course));

        List<LessonSearchResultResponse> result = lessonService.searchLessons("부탁");

        assertThat(result.get(0).snippetSubtext()).isEqualTo("도와주세요 부탁드립니다.");
    }

    @Test
    void searchLessons_키워드가_없으면_빈_목록을_반환한다() {
        assertThat(lessonService.searchLessons(null)).isEmpty();
        assertThat(lessonService.searchLessons("  ")).isEmpty();
    }

    @Test
    void searchLessons_매칭이_없으면_빈_목록을_반환한다() {
        when(lessonRepository.searchByContent("없는단어")).thenReturn(List.of());

        assertThat(lessonService.searchLessons("없는단어")).isEmpty();
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
