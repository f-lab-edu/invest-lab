package com.flab.project.investlab.member.exception;

import com.flab.project.investlab.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements ErrorCode {

    EMAIL_DUPLICATED(
            "MEMBER_EMAIL_DUPLICATED",
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),
    NICKNAME_DUPLICATED(
            "MEMBER_NICKNAME_DUPLICATED",
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다."
    ),
    MEMBER_DUPLICATED(
            "MEMBER_DUPLICATED",
            HttpStatus.CONFLICT,
            "이미 존재하는 회원 정보입니다."
    ),
    MEMBER_NOT_FOUND(
            "MEMBER_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "회원을 찾을 수 없습니다."
    ),
    PASSWORD_TOO_LONG(
            "MEMBER_PASSWORD_TOO_LONG",
            HttpStatus.BAD_REQUEST,
            "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    MemberErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
