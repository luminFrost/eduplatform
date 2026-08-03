package com.edu.eduplatform.member.exception;

public class CannotWithdrawAdminException extends RuntimeException {

    public CannotWithdrawAdminException() {
        super("관리자 계정은 마이페이지에서 탈퇴할 수 없습니다. 다른 관리자에게 역할 변경을 요청해 주세요.");
    }
}
