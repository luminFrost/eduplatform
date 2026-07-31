package com.edu.eduplatform.progress.exception;

public class InsufficientHistoryException extends RuntimeException {

    public InsufficientHistoryException() {
        super("아직 학습 이력이 충분하지 않아요. 레슨을 몇 개 더 완료한 뒤 다시 시도해 주세요.");
    }
}
