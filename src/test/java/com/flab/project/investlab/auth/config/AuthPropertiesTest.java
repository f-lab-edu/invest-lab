package com.flab.project.investlab.auth.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthPropertiesTest {

    @Test
    void JWT_비밀키가_32바이트보다_짧으면_생성을_거부한다() {
        // when & then
        assertThatThrownBy(() -> properties("a".repeat(31)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void JWT_비밀키가_32바이트이면_생성한다() {
        // when & then
        assertThatCode(() -> properties("a".repeat(32)))
                .doesNotThrowAnyException();
    }

    private AuthProperties properties(String secret) {
        return new AuthProperties(
                secret,
                "https://api.investlab.local",
                Duration.ofMinutes(30),
                Duration.ofDays(7),
                true,
                "Lax"
        );
    }
}
