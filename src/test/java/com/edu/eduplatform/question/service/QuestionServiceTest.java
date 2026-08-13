package com.edu.eduplatform.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import com.edu.eduplatform.question.dto.QuestionAdminRequest;
import com.edu.eduplatform.question.dto.QuestionAdminResponse;
import com.edu.eduplatform.question.dto.QuestionResponse;
import com.edu.eduplatform.question.exception.DiagnosticTestIncompleteException;
import com.edu.eduplatform.question.exception.QuestionNotFoundException;
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

    @Test
    void getAllQuestions_필터_없이_전체를_반환한다() throws Exception {
        Question q1 = withId(question(LessonType.VOCAB, 0), 1L);
        Question q2 = withId(question(LessonType.READING, 1), 2L);
        when(questionRepository.findAll()).thenReturn(List.of(q1, q2));

        List<QuestionAdminResponse> result = questionService.getAllQuestions();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(QuestionAdminResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void createQuestion_요청대로_문항을_저장한다() {
        var request = new QuestionAdminRequest(
                MemberType.ADULT, EnglishLevel.BEGINNER, LessonType.VOCAB, "새 문제",
                null, "보기1", "보기2", "보기3", "보기4", 2);

        questionService.createQuestion(request);

        org.mockito.ArgumentCaptor<Question> captor = org.mockito.ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getPrompt()).isEqualTo("새 문제");
        assertThat(captor.getValue().getOptions()).containsExactly("보기1", "보기2", "보기3", "보기4");
        assertThat(captor.getValue().getCorrectOptionIndex()).isEqualTo(2);
    }

    @Test
    void createQuestions_여러_건을_한번에_저장한다() {
        List<QuestionAdminRequest> requests = List.of(
                new QuestionAdminRequest(MemberType.ADULT, EnglishLevel.BEGINNER, LessonType.VOCAB,
                        "문제1", null, "보기1", "보기2", "보기3", "보기4", 0),
                new QuestionAdminRequest(MemberType.CHILD, EnglishLevel.ELEMENTARY, LessonType.READING,
                        "문제2", null, "a", "b", "c", "d", 1));

        questionService.createQuestions(requests);

        org.mockito.ArgumentCaptor<Question> captor = org.mockito.ArgumentCaptor.forClass(Question.class);
        verify(questionRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Question::getPrompt).containsExactly("문제1", "문제2");
    }

    @Test
    void createQuestions_빈_목록이면_아무것도_저장하지_않는다() {
        questionService.createQuestions(List.of());

        verify(questionRepository, never()).save(any());
    }

    @Test
    void updateQuestion_존재하는_문항이면_내용을_수정한다() throws Exception {
        Question question = withId(question(LessonType.VOCAB, 0), 1L);
        when(questionRepository.findById(1L)).thenReturn(java.util.Optional.of(question));
        var request = new QuestionAdminRequest(
                MemberType.CHILD, EnglishLevel.ADVANCED, LessonType.WRITING, "수정된 문제",
                "audio text", "새보기1", "새보기2", "새보기3", "새보기4", 3);

        questionService.updateQuestion(1L, request);

        assertThat(question.getPrompt()).isEqualTo("수정된 문제");
        assertThat(question.getTargetType()).isEqualTo(MemberType.CHILD);
        assertThat(question.getLevel()).isEqualTo(EnglishLevel.ADVANCED);
        assertThat(question.getLessonType()).isEqualTo(LessonType.WRITING);
        assertThat(question.getAudioText()).isEqualTo("audio text");
        assertThat(question.getOptions()).containsExactly("새보기1", "새보기2", "새보기3", "새보기4");
        assertThat(question.getCorrectOptionIndex()).isEqualTo(3);
        verify(questionRepository).save(question);
    }

    @Test
    void updateQuestion_존재하지_않는_문항이면_예외를_던진다() {
        when(questionRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        var request = new QuestionAdminRequest(
                MemberType.ADULT, EnglishLevel.BEGINNER, LessonType.VOCAB, "문제",
                null, "1", "2", "3", "4", 0);

        assertThatThrownBy(() -> questionService.updateQuestion(999L, request))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void deleteQuestion_존재하는_문항이면_삭제한다() {
        when(questionRepository.existsById(1L)).thenReturn(true);

        questionService.deleteQuestion(1L);

        verify(questionRepository).deleteById(1L);
    }

    @Test
    void deleteQuestion_존재하지_않는_문항이면_예외를_던진다() {
        when(questionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> questionService.deleteQuestion(999L))
                .isInstanceOf(QuestionNotFoundException.class);
        verify(questionRepository, never()).deleteById(any());
    }

    private static Question withId(Question question, Long id) throws Exception {
        Field field = question.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(question, id);
        return question;
    }
}
