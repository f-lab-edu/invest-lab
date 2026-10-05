package com.flab.project.investlab.auth.cookie;

import com.flab.project.investlab.auth.config.AuthProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieManager {

    public static final String COOKIE_NAME = "refreshToken";

    private final AuthProperties authProperties;

    public RefreshCookieManager(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public ResponseCookie create(String refreshToken) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(authProperties.refreshCookieSecure())
                .sameSite(authProperties.refreshCookieSameSite())
                .path("/api/v1/auth")
                .maxAge(authProperties.refreshTokenTtl())
                .build();
    }

    public ResponseCookie expire() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authProperties.refreshCookieSecure())
                .sameSite(authProperties.refreshCookieSameSite())
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }
}
