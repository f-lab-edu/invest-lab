package com.flab.project.investlab.auth.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthPropertiesTest {

    @Test
    void JWT_비밀키가_설정되지_않으면_생성을_거부한다() {
        // When & Then
        assertThatThrownBy(() -> new AuthProperties(
                "${JWT_SECRET}",
                "https://api.investlab.local",
                Duration.ofMinutes(30),
                Duration.ofDays(7),
                true,
                "Lax"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
