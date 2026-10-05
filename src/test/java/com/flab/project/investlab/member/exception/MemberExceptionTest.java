package com.flab.project.investlab.member.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class MemberExceptionTest {

    @Test
    void 회원_예외는_에러코드와_HTTP_상태를_포함한다() {
        // Given
        final MemberException exception = new MemberException(MemberErrorCode.EMAIL_DUPLICATED);

        // Then
        assertThat(exception.getErrorCode()).isEqualTo("MEMBER_EMAIL_DUPLICATED");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }
}
