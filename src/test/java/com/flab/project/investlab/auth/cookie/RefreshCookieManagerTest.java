package com.flab.project.investlab.auth.cookie;

import com.flab.project.investlab.auth.config.AuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshCookieManagerTest {

    @Test
    void 리프레시_쿠키는_HTTP_ONLY와_보안_속성을_사용한다() {
        // Given
        AuthProperties properties = new AuthProperties(
                "investlab-local-jwt-secret-key-32bytes",
                "https://api.investlab.local",
                Duration.ofMinutes(30),
                Duration.ofDays(7),
                true,
                "Lax"
        );
        RefreshCookieManager cookieManager = new RefreshCookieManager(properties);

        // When
        ResponseCookie cookie = cookieManager.create("refresh-token");

        // Then
        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(7));
    }
}
