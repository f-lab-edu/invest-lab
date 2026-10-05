package com.flab.project.investlab.auth.token;

import com.flab.project.investlab.auth.config.AuthProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class AccessTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;
    private final Clock clock;

    public AccessTokenProvider(JwtEncoder jwtEncoder, AuthProperties authProperties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.authProperties = authProperties;
        this.clock = clock;
    }

    public String create(Long memberId) {
        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("investlab")
                .subject(memberId.toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(authProperties.accessTokenTtl()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return authProperties.accessTokenTtl().toSeconds();
    }
}
