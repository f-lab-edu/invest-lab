package com.flab.project.investlab.policy.exception;

import com.flab.project.investlab.common.error.BusinessException;

public class PolicyException extends BusinessException {

    public PolicyException(final PolicyErrorCode errorCode) {
        super(errorCode);
    }
}
