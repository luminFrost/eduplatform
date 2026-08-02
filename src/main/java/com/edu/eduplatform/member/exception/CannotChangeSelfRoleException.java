package com.edu.eduplatform.member.exception;

public class CannotChangeSelfRoleException extends RuntimeException {

    public CannotChangeSelfRoleException() {
        super("자기 자신의 역할은 변경할 수 없습니다.");
    }
}
