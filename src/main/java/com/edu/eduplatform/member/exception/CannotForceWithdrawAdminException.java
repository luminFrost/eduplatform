package com.edu.eduplatform.member.exception;

public class CannotForceWithdrawAdminException extends RuntimeException {

    public CannotForceWithdrawAdminException() {
        super("관리자 계정은 강제 탈퇴시킬 수 없습니다. 역할을 먼저 변경해 주세요.");
    }
}
