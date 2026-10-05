package com.flab.project.investlab.security;

import com.flab.project.investlab.auth.config.AuthProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

@Configuration
public class TokenConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    JwtEncoder jwtEncoder(AuthProperties authProperties) {
        SecretKey secretKey = new SecretKeySpec(
                authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }
}
