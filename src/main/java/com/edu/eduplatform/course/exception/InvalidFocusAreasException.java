package com.edu.eduplatform.course.exception;

public class InvalidFocusAreasException extends RuntimeException {

    public InvalidFocusAreasException() {
        super("비중을 둘 영역을 하나 이상 선택해 주세요.");
    }
}
