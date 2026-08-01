package com.edu.eduplatform.quiz.dto;

import java.util.List;

/** 그림 퀴즈 문항. 정답은 노출하지 않는다(진단 테스트 QuestionResponse와 같은 원칙). */
public record PictureQuizQuestion(String iconImage, List<String> options) {
}
