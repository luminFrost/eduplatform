package com.edu.eduplatform.member.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("존재하지 않는 회원입니다: " + id);
    }
}
