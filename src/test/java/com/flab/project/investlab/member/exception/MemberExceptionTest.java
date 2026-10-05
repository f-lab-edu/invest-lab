package com.flab.project.investlab.member.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class MemberExceptionTest {

    @Test
    void 회원_예외는_에러코드와_HTTP_상태를_포함한다() {
        // Given
        MemberException exception = new MemberException(MemberErrorCode.EMAIL_DUPLICATED);

        // When
        String errorCode = exception.getErrorCode();
        HttpStatus httpStatus = exception.getHttpStatus();

        // Then
        assertThat(errorCode).isEqualTo("MEMBER_EMAIL_DUPLICATED");
        assertThat(httpStatus).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
    }
}
