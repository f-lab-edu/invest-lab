package com.flab.project.investlab.auth.exception;

import com.flab.project.investlab.common.error.BusinessException;

public class AuthException extends BusinessException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
