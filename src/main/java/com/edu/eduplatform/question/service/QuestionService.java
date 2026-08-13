package com.edu.eduplatform.question.service;

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
import java.util.Arrays;
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

    /** 레벨 배치 테스트에서 레벨당 뽑아 쓰는 문항 수(영역 무관, 앞의 N개). */
    private static final int PLACEMENT_QUESTIONS_PER_LEVEL = 2;
    /** 이 비율 이상 맞혀야 그 레벨을 "통과"한 것으로 본다. */
    private static final double PLACEMENT_PASS_THRESHOLD = 0.5;

    private final QuestionRepository questionRepository;

    /** 관리자 문항 목록 — 필터 없이 전체를 반환한다(80건 안팎이라 별도 동적 쿼리 없이 스트림 필터링으로 충분). */
    public List<QuestionAdminResponse> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(QuestionService::toAdminResponse)
                .toList();
    }

    public QuestionAdminResponse getQuestionForEdit(Long id) {
        return toAdminResponse(questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id)));
    }

    @Transactional
    public void createQuestion(QuestionAdminRequest request) {
        questionRepository.save(Question.builder()
                .targetType(request.targetType())
                .level(request.level())
                .lessonType(request.lessonType())
                .prompt(request.prompt())
                .audioText(request.audioText())
                .options(request.options())
                .correctOptionIndex(request.correctOptionIndex())
                .build());
    }

    /** 여러 문항을 한 번에 생성한다 — 관리자 JSON 일괄 등록에 쓰인다. */
    @Transactional
    public void createQuestions(List<QuestionAdminRequest> requests) {
        for (QuestionAdminRequest request : requests) {
            questionRepository.save(Question.builder()
                    .targetType(request.targetType())
                    .level(request.level())
                    .lessonType(request.lessonType())
                    .prompt(request.prompt())
                    .audioText(request.audioText())
                    .options(request.options())
                    .correctOptionIndex(request.correctOptionIndex())
                    .build());
        }
    }

    @Transactional
    public void updateQuestion(Long id, QuestionAdminRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));
        question.updateDetails(request.targetType(), request.level(), request.lessonType(),
                request.prompt(), request.audioText(), request.options(), request.correctOptionIndex());
        questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new QuestionNotFoundException(id);
        }
        questionRepository.deleteById(id);
    }

    private static QuestionAdminResponse toAdminResponse(Question question) {
        return new QuestionAdminResponse(
                question.getId(), question.getTargetType(), question.getLevel(), question.getLessonType(),
                question.getPrompt(), question.getAudioText(), question.getOptions(), question.getCorrectOptionIndex());
    }

    public List<QuestionResponse> getDiagnosticTestQuestions(MemberType targetType, EnglishLevel level) {
        return questionRepository.findByTargetTypeAndLevel(targetType, level).stream()
                .map(QuestionResponse::from)
                .toList();
    }

    /** 가입 전 레벨 배치 테스트 문항 — 레벨마다 앞의 {@link #PLACEMENT_QUESTIONS_PER_LEVEL}개씩, 총 4레벨분. */
    public List<QuestionResponse> getLevelPlacementQuestions(MemberType targetType) {
        return Arrays.stream(EnglishLevel.values())
                .flatMap(level -> questionRepository.findByTargetTypeAndLevel(targetType, level).stream()
                        .limit(PLACEMENT_QUESTIONS_PER_LEVEL))
                .map(QuestionResponse::from)
                .toList();
    }

    /**
     * BEGINNER부터 순서대로 레벨별 정답률을 확인해, 과반 이상 맞힌 마지막 레벨을 추천한다. 어느 레벨에서
     * 과반 미만이면 더 높은 레벨은 보지 않고 멈춘다 — BEGINNER조차 과반 미만이어도 BEGINNER를 추천한다
     * (최저 바닥). 미답변 문항은 오답으로 친다 — 진단 테스트와 달리 낮은 판돈의 "제안"이라 전부 답해야
     * 한다는 강제가 없다.
     */
    public EnglishLevel recommendLevel(MemberType targetType, Map<Long, Integer> answersByQuestionId) {
        EnglishLevel recommended = EnglishLevel.BEGINNER;
        for (EnglishLevel level : EnglishLevel.values()) {
            List<Question> levelQuestions = questionRepository.findByTargetTypeAndLevel(targetType, level).stream()
                    .limit(PLACEMENT_QUESTIONS_PER_LEVEL)
                    .toList();
            if (levelQuestions.isEmpty()) {
                break;
            }
            long correct = levelQuestions.stream()
                    .filter(question -> Objects.equals(
                            answersByQuestionId.get(question.getId()), question.getCorrectOptionIndex()))
                    .count();
            if (correct / (double) levelQuestions.size() < PLACEMENT_PASS_THRESHOLD) {
                break;
            }
            recommended = level;
        }
        return recommended;
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
