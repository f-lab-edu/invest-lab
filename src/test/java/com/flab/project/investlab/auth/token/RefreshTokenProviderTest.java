package com.flab.project.investlab.auth.token;

import com.flab.project.investlab.auth.config.AuthProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenProviderTest {

    private static final Instant NOW = Instant.parse("2026-10-05T00:00:00Z");

    private final AuthProperties properties = new AuthProperties(
            "investlab-local-jwt-secret-key-32bytes",
            "https://api.investlab.local",
            Duration.ofMinutes(30),
            Duration.ofDays(7),
            true,
            "Lax"
    );
    private final RefreshTokenProvider tokenProvider = new RefreshTokenProvider(
            properties,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void 리프레시_토큰은_SHA_256으로_해시한다() {
        // when
        final String hash = tokenProvider.hash("refresh-token");

        // then
        assertThat(hash).isEqualTo(
                "0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120"
        );
    }

    @Test
    void 리프레시_토큰_만료시간은_현재로부터_7일이다() {
        // then
        assertThat(tokenProvider.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
    }
}
