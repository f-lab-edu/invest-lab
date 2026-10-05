package com.flab.project.investlab.common.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException exception) {
        return createProblemDetail(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationException() {
        return createProblemDetail(ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation() {
        return createProblemDetail(ErrorCode.MEMBER_DUPLICATED);
    }

    private ProblemDetail createProblemDetail(ErrorCode errorCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                errorCode.getStatus(),
                errorCode.getDetail()
        );
        problemDetail.setProperty("code", errorCode.name());
        return problemDetail;
    }
}
