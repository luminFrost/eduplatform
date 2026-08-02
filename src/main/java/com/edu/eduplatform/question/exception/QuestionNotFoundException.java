package com.edu.eduplatform.question.exception;

public class QuestionNotFoundException extends RuntimeException {

    public QuestionNotFoundException(Long id) {
        super("존재하지 않는 문항입니다: " + id);
    }
}
