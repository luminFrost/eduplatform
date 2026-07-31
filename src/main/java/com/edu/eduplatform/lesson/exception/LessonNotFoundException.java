package com.edu.eduplatform.lesson.exception;

public class LessonNotFoundException extends RuntimeException {

    public LessonNotFoundException(Long id) {
        super("존재하지 않는 레슨입니다: " + id);
    }
}
