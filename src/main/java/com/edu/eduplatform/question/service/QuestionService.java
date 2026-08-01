package com.edu.eduplatform.question.service;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import com.edu.eduplatform.question.dto.QuestionResponse;
import com.edu.eduplatform.question.exception.DiagnosticTestIncompleteException;
import com.edu.eduplatform.question.repository.QuestionRepository;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<QuestionResponse> getDiagnosticTestQuestions(MemberType targetType, EnglishLevel level) {
        return questionRepository.findByTargetTypeAndLevel(targetType, level).stream()
                .map(QuestionResponse::from)
                .toList();
    }

    /**
     * 영역별(어휘/읽기/쓰기/듣기/말하기) 정답률이 가장 낮은 영역(들)을 반환한다 — 동률이면 모두 포함한다.
     * {@link com.edu.eduplatform.progress.service.ProgressService#recommendFocusAreas}의 "min 값 →
     * EnumSet" 패턴을 그대로 따르되, 대리 지표인 커버리지 대신 실제 정오답을 쓴다.
     */
    public Set<LessonType> determineFocusAreas(MemberType targetType, EnglishLevel level, Map<Long, Integer> answersByQuestionId) {
        List<Question> questions = questionRepository.findByTargetTypeAndLevel(targetType, level);
        boolean allAnswered = questions.stream().map(Question::getId).allMatch(answersByQuestionId::containsKey);
        if (questions.isEmpty() || !allAnswered) {
            throw new DiagnosticTestIncompleteException();
        }

        Map<LessonType, long[]> scoreByType = new EnumMap<>(LessonType.class);
        for (Question question : questions) {
            long[] counts = scoreByType.computeIfAbsent(question.getLessonType(), type -> new long[2]);
            counts[1]++;
            if (Objects.equals(answersByQuestionId.get(question.getId()), question.getCorrectOptionIndex())) {
                counts[0]++;
            }
        }

        double lowestScore = scoreByType.values().stream()
                .mapToDouble(counts -> counts[0] / (double) counts[1])
                .min()
                .orElseThrow();

        Set<LessonType> focusAreas = EnumSet.noneOf(LessonType.class);
        scoreByType.forEach((type, counts) -> {
            double score = counts[0] / (double) counts[1];
            if (score == lowestScore) {
                focusAreas.add(type);
            }
        });

        return focusAreas;
    }
}
