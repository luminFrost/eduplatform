package com.edu.eduplatform.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import com.edu.eduplatform.question.dto.QuestionResponse;
import com.edu.eduplatform.question.exception.DiagnosticTestIncompleteException;
import com.edu.eduplatform.question.repository.QuestionRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void getDiagnosticTestQuestions_대상과_레벨에_맞는_문항을_정답_없이_반환한다() throws Exception {
        Question question = withId(Question.builder()
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).lessonType(LessonType.VOCAB)
                .prompt("빈칸에 알맞은 단어를 고르세요: I ___ to school.")
                .options(List.of("go", "goes", "going", "gone"))
                .correctOptionIndex(0)
                .build(), 1L);
        when(questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER))
                .thenReturn(List.of(question));

        List<QuestionResponse> result = questionService.getDiagnosticTestQuestions(MemberType.ADULT, EnglishLevel.BEGINNER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).lessonType()).isEqualTo(LessonType.VOCAB);
        assertThat(result.get(0).options()).containsExactly("go", "goes", "going", "gone");
    }

    @Test
    void determineFocusAreas_정답률이_가장_낮은_영역을_반환한다() throws Exception {
        // VOCAB 2문항 다 정답, WRITING 2문항 중 1개만 정답
        Question vocab1 = withId(question(LessonType.VOCAB, 0), 1L);
        Question vocab2 = withId(question(LessonType.VOCAB, 0), 2L);
        Question writing1 = withId(question(LessonType.WRITING, 0), 3L);
        Question writing2 = withId(question(LessonType.WRITING, 0), 4L);
        when(questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER))
                .thenReturn(List.of(vocab1, vocab2, writing1, writing2));

        Map<Long, Integer> answers = Map.of(1L, 0, 2L, 0, 3L, 0, 4L, 1); // writing2는 오답(1 != 0)

        Set<LessonType> focusAreas = questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, answers);

        assertThat(focusAreas).containsExactly(LessonType.WRITING);
    }

    @Test
    void determineFocusAreas_정답률이_동률이면_모두_반환한다() throws Exception {
        // VOCAB, WRITING 둘 다 2문항 중 1개만 정답 → 50%로 동률
        Question vocab1 = withId(question(LessonType.VOCAB, 0), 1L);
        Question vocab2 = withId(question(LessonType.VOCAB, 0), 2L);
        Question writing1 = withId(question(LessonType.WRITING, 0), 3L);
        Question writing2 = withId(question(LessonType.WRITING, 0), 4L);
        when(questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER))
                .thenReturn(List.of(vocab1, vocab2, writing1, writing2));

        Map<Long, Integer> answers = Map.of(1L, 0, 2L, 1, 3L, 0, 4L, 1);

        Set<LessonType> focusAreas = questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, answers);

        assertThat(focusAreas).containsExactlyInAnyOrder(LessonType.VOCAB, LessonType.WRITING);
    }

    @Test
    void determineFocusAreas_답하지_않은_문항이_있으면_예외를_던진다() throws Exception {
        Question vocab1 = withId(question(LessonType.VOCAB, 0), 1L);
        Question vocab2 = withId(question(LessonType.VOCAB, 0), 2L);
        when(questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER))
                .thenReturn(List.of(vocab1, vocab2));

        Map<Long, Integer> incompleteAnswers = Map.of(1L, 0); // 2L 답 누락

        assertThatThrownBy(() -> questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, incompleteAnswers))
                .isInstanceOf(DiagnosticTestIncompleteException.class);
    }

    @Test
    void determineFocusAreas_해당_조합에_문항이_없으면_예외를_던진다() {
        when(questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER))
                .thenReturn(List.of());

        assertThatThrownBy(() -> questionService.determineFocusAreas(MemberType.ADULT, EnglishLevel.BEGINNER, Map.of()))
                .isInstanceOf(DiagnosticTestIncompleteException.class);
    }

    @Test
    void getLevelPlacementQuestions_레벨당_두_문항씩_총_여덟_개를_반환한다() throws Exception {
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.BEGINNER, 1L, 2L, 3L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.ELEMENTARY, 10L, 11L, 12L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.INTERMEDIATE, 20L, 21L, 22L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.ADVANCED, 30L, 31L, 32L);

        List<QuestionResponse> result = questionService.getLevelPlacementQuestions(MemberType.ADULT);

        assertThat(result).hasSize(8);
        assertThat(result).extracting(QuestionResponse::id)
                .containsExactly(1L, 2L, 10L, 11L, 20L, 21L, 30L, 31L);
    }

    @Test
    void recommendLevel_통과한_마지막_레벨을_추천한다() throws Exception {
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.BEGINNER, 1L, 2L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.ELEMENTARY, 3L, 4L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.INTERMEDIATE, 5L, 6L);
        // BEGINNER/ELEMENTARY 다 정답, INTERMEDIATE는 다 오답 → ELEMENTARY까지만 통과.
        Map<Long, Integer> answers = Map.of(1L, 0, 2L, 0, 3L, 0, 4L, 0, 5L, 1, 6L, 1);

        EnglishLevel recommended = questionService.recommendLevel(MemberType.ADULT, answers);

        assertThat(recommended).isEqualTo(EnglishLevel.ELEMENTARY);
    }

    @Test
    void recommendLevel_전부_통과하면_ADVANCED를_추천한다() throws Exception {
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.BEGINNER, 1L, 2L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.ELEMENTARY, 3L, 4L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.INTERMEDIATE, 5L, 6L);
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.ADVANCED, 7L, 8L);
        Map<Long, Integer> answers = Map.of(1L, 0, 2L, 0, 3L, 0, 4L, 0, 5L, 0, 6L, 0, 7L, 0, 8L, 0);

        EnglishLevel recommended = questionService.recommendLevel(MemberType.ADULT, answers);

        assertThat(recommended).isEqualTo(EnglishLevel.ADVANCED);
    }

    @Test
    void recommendLevel_BEGINNER부터_실패해도_BEGINNER를_추천한다() throws Exception {
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.BEGINNER, 1L, 2L);
        Map<Long, Integer> answers = Map.of(1L, 1, 2L, 1); // 둘 다 오답(정답은 0)

        EnglishLevel recommended = questionService.recommendLevel(MemberType.ADULT, answers);

        assertThat(recommended).isEqualTo(EnglishLevel.BEGINNER);
    }

    @Test
    void recommendLevel_미답변_문항은_오답으로_처리한다() throws Exception {
        stubLevelQuestions(MemberType.ADULT, EnglishLevel.BEGINNER, 1L, 2L);
        Map<Long, Integer> answers = Map.of(1L, 0); // 2L 미답변

        EnglishLevel recommended = questionService.recommendLevel(MemberType.ADULT, answers);

        // 1/2 = 50% >= 임계치라 BEGINNER는 여전히 통과, 다음 레벨(ELEMENTARY)은 재료가 없어 멈춘다.
        assertThat(recommended).isEqualTo(EnglishLevel.BEGINNER);
    }

    private void stubLevelQuestions(MemberType targetType, EnglishLevel level, Long... ids) throws Exception {
        List<Question> questions = new ArrayList<>();
        for (Long id : ids) {
            questions.add(withId(question(LessonType.VOCAB, 0), id));
        }
        when(questionRepository.findByTargetTypeAndLevel(targetType, level)).thenReturn(questions);
    }

    private static Question question(LessonType lessonType, int correctOptionIndex) {
        return Question.builder()
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).lessonType(lessonType)
                .prompt("문제")
                .options(List.of("A", "B", "C", "D"))
                .correctOptionIndex(correctOptionIndex)
                .build();
    }

    private static Question withId(Question question, Long id) throws Exception {
        Field field = question.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(question, id);
        return question;
    }
}
