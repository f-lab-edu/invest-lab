package com.flab.project.investlab.auth.token;

import com.flab.project.investlab.auth.config.AuthProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenProviderTest {

    @Test
    void 액세스_토큰은_회원_ID와_30분_만료시간을_포함한다() {
        // Given
        final String secret = "investlab-local-jwt-secret-key-32bytes";
        final SecretKey secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        final Instant issuedAt = Instant.parse("2026-10-05T00:00:00Z");
        final AuthProperties properties = new AuthProperties(
                secret,
                "https://api.investlab.local",
                Duration.ofMinutes(30),
                Duration.ofDays(7),
                true,
                "Lax"
        );
        final Clock fixedClock = Clock.fixed(issuedAt, ZoneOffset.UTC);
        final AccessTokenProvider tokenProvider = new AccessTokenProvider(
                new NimbusJwtEncoder(new ImmutableSecret<>(secretKey)),
                properties,
                fixedClock
        );
        final NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        final JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
        timestampValidator.setClock(fixedClock);
        jwtDecoder.setJwtValidator(timestampValidator);

        // When
        final Jwt jwt = jwtDecoder.decode(tokenProvider.create(1L));

        // Then
        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://api.investlab.local");
        assertThat(jwt.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(jwt.getExpiresAt()).isEqualTo(issuedAt.plus(Duration.ofMinutes(30)));
        assertThat(tokenProvider.expiresInSeconds()).isEqualTo(1800L);
    }
}
