package com.flab.project.investlab.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    @Test
    void 로그인_요청은_이메일의_공백을_제거한다() {
        // When
        LoginRequest request = new LoginRequest(" member@example.com ", "password1234");

        // Then
        assertThat(request.email()).isEqualTo("member@example.com");
    }
}
