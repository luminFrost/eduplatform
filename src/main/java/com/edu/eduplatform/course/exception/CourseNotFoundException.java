package com.edu.eduplatform.course.exception;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(Long id) {
        super("존재하지 않는 코스입니다: " + id);
    }
}
