package com.edu.eduplatform.member.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("현재 비밀번호가 올바르지 않습니다.");
    }
}
