package com.flab.project.investlab.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String jwtSecret,
        String jwtIssuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        boolean refreshCookieSecure,
        String refreshCookieSameSite
) {

    private static final int MIN_SECRET_BYTES = 32;

    // 환경변수가 없으면 "${JWT_SECRET}" 문자열이 그대로 들어오므로 길이로 기동을 막는다.
    public AuthProperties {
        if (jwtSecret == null
                || jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes");
        }
    }
}
