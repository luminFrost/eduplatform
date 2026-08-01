package com.edu.eduplatform.quiz.dto;

import java.util.List;

/** 정답은 노출하지 않는다(레슨 이해도 퀴즈의 LessonQuiz와 같은 원칙). */
public record DailyWordQuiz(String sentenceWithBlank, String translation, List<String> options) {
}
