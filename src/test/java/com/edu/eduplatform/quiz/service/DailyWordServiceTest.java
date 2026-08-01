package com.edu.eduplatform.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.ContentLine;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.LineType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.quiz.dto.DailyWord;
import com.edu.eduplatform.quiz.dto.DailyWordQuiz;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyWordServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private DailyWordService dailyWordService;

    private static final MemberResponse MEMBER = new MemberResponse(
            1L, "test@example.com", "테스터", MemberType.ADULT, EnglishLevel.BEGINNER, LocalDateTime.now());

    @Test
    void getTodayWords_회원_레벨의_공식_코스에서_단어장을_만든다() throws Exception {
        stubTwoPhraseLessons();

        List<DailyWord> words = dailyWordService.getTodayWords(MEMBER);

        assertThat(words).hasSize(2);
        assertThat(words).extracting(DailyWord::english)
                .containsExactlyInAnyOrder("I would like a coffee, please.", "I really enjoy summer vacation.");
    }

    @Test
    void getTodayWords_재료가_없으면_빈_목록을_반환한다() {
        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null)).thenReturn(List.of());

        List<DailyWord> words = dailyWordService.getTodayWords(MEMBER);

        assertThat(words).isEmpty();
    }

    @Test
    void getTodayQuiz_핵심_단어를_빈칸으로_만든_퀴즈를_반환한다() throws Exception {
        stubTwoPhraseLessons();

        Optional<DailyWordQuiz> quiz = dailyWordService.getTodayQuiz(MEMBER);

        assertThat(quiz).isPresent();
        assertThat(quiz.get().sentenceWithBlank()).contains("___");
        assertThat(quiz.get().options()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void checkAnswer_오늘의_정답과_대소문자_무시하고_비교한다() throws Exception {
        stubTwoPhraseLessons();

        Optional<DailyWordQuiz> quiz = dailyWordService.getTodayQuiz(MEMBER);
        assertThat(quiz).isPresent();
        // sentenceWithBlank에서 빈칸 자리를 알 수 없으니, 두 후보 문장의 핵심 단어 중 하나로 정답 여부만 확인.
        boolean correctForCoffee = dailyWordService.checkAnswer(MEMBER, "COFFEE");
        boolean correctForVacation = dailyWordService.checkAnswer(MEMBER, "VACATION");

        assertThat(correctForCoffee || correctForVacation).isTrue();
        assertThat(dailyWordService.checkAnswer(MEMBER, "definitely-wrong")).isFalse();
    }

    private void stubTwoPhraseLessons() throws Exception {
        Course course = withCourseId(Course.builder()
                .title("코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 1L);
        Lesson lesson1 = withLessonId(Lesson.builder().courseId(1L).orderNo(1).title("1과")
                .content("I would like a coffee, please. — 저는 커피를 부탁드립니다.")
                .lessonType(LessonType.VOCAB).build(), 10L);
        Lesson lesson2 = withLessonId(Lesson.builder().courseId(1L).orderNo(2).title("2과")
                .content("I really enjoy summer vacation. — 나는 여름 방학을 정말 좋아해요.")
                .lessonType(LessonType.VOCAB).build(), 11L);

        when(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null)).thenReturn(List.of(course));
        when(lessonRepository.findByCourseIdIn(List.of(1L))).thenReturn(List.of(lesson1, lesson2));
        when(lessonService.parseContent(lesson1.getContent())).thenReturn(List.of(
                new ContentLine(LineType.PHRASE, "I would like a coffee, please.", "저는 커피를 부탁드립니다.", null, null)));
        when(lessonService.parseContent(lesson2.getContent())).thenReturn(List.of(
                new ContentLine(LineType.PHRASE, "I really enjoy summer vacation.", "나는 여름 방학을 정말 좋아해요.", null, null)));
    }

    private static Course withCourseId(Course course, Long id) throws Exception {
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
