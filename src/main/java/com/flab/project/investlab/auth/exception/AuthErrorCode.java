package com.flab.project.investlab.auth.exception;

import com.flab.project.investlab.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {

    LOGIN_FAILED(
            "AUTH_LOGIN_FAILED",
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),
    ACCESS_TOKEN_INVALID(
            "AUTH_ACCESS_TOKEN_INVALID",
            HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다."
    ),
    REFRESH_TOKEN_INVALID(
            "AUTH_REFRESH_TOKEN_INVALID",
            HttpStatus.UNAUTHORIZED,
            "리프레시 토큰이 올바르지 않습니다."
    ),
    REFRESH_TOKEN_EXPIRED(
            "AUTH_REFRESH_TOKEN_EXPIRED",
            HttpStatus.UNAUTHORIZED,
            "리프레시 토큰이 만료되었습니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    AuthErrorCode(String code, HttpStatus httpStatus, String message) {
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
