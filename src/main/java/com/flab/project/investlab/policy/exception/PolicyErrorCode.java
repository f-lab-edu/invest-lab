package com.flab.project.investlab.policy.exception;

import com.flab.project.investlab.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PolicyErrorCode implements ErrorCode {

    PRESET_NOT_FOUND(
            "POLICY_PRESET_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "프리셋을 찾을 수 없습니다."
    ),
    TARGET_SUM_INVALID(
            "POLICY_TARGET_SUM_INVALID",
            HttpStatus.BAD_REQUEST,
            "현금을 포함한 목표 비중 합계는 100%여야 합니다."
    ),
    ASSET_CLASS_DUPLICATED(
            "POLICY_ASSET_CLASS_DUPLICATED",
            HttpStatus.BAD_REQUEST,
            "같은 자산군을 중복해서 배분할 수 없습니다."
    ),
    INSTRUMENT_DUPLICATED(
            "POLICY_INSTRUMENT_DUPLICATED",
            HttpStatus.BAD_REQUEST,
            "같은 ETF를 중복해서 배분할 수 없습니다."
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
