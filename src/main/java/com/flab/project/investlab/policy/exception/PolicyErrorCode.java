package com.flab.project.investlab.policy.exception;

import com.flab.project.investlab.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PolicyErrorCode implements ErrorCode {

    PRESET_NOT_FOUND(
            "POLICY_PRESET_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "프리셋을 찾을 수 없습니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    PolicyErrorCode(final String code, final HttpStatus httpStatus, final String message) {
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
