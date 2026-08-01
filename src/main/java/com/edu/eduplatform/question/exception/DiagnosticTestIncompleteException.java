package com.edu.eduplatform.question.exception;

public class DiagnosticTestIncompleteException extends RuntimeException {

    public DiagnosticTestIncompleteException() {
        super("모든 문항에 답해야 결과를 만들 수 있어요.");
    }
}
