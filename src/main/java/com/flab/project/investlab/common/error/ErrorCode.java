package com.flab.project.investlab.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    MEMBER_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 회원 정보입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String detail;

    ErrorCode(HttpStatus status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
