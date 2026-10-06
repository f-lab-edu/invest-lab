package com.flab.project.investlab.auth.service;

import com.flab.project.investlab.auth.domain.RefreshSession;
import com.flab.project.investlab.auth.exception.AuthErrorCode;
import com.flab.project.investlab.auth.exception.AuthException;
import com.flab.project.investlab.auth.repository.RefreshSessionRepository;
import com.flab.project.investlab.auth.token.AccessTokenProvider;
import com.flab.project.investlab.auth.token.RefreshTokenProvider;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;

    public AuthService(
            MemberRepository memberRepository,
            RefreshSessionRepository refreshSessionRepository,
            PasswordEncoder passwordEncoder,
            AccessTokenProvider accessTokenProvider,
            RefreshTokenProvider refreshTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenProvider = accessTokenProvider;
        this.refreshTokenProvider = refreshTokenProvider;
    }

    @Transactional
    public LoginResult login(String email, String rawPassword) {
        final String normalizedEmail = email.toLowerCase(Locale.ROOT);
        final Member member = memberRepository.findByEmail(normalizedEmail)
                .filter(foundMember -> passwordEncoder.matches(rawPassword, foundMember.getPasswordHash()))
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        final String accessToken = accessTokenProvider.create(member.getId());
        final String refreshToken = refreshTokenProvider.create();
        final String refreshTokenHash = refreshTokenProvider.hash(refreshToken);
        final Instant expiresAt = refreshTokenProvider.expiresAt();

        final RefreshSession refreshSession = refreshSessionRepository.findByMemberId(member.getId())
                .orElseGet(() -> new RefreshSession(member, refreshTokenHash, expiresAt));
        refreshSession.replace(refreshTokenHash, expiresAt);
        refreshSessionRepository.save(refreshSession);

        return new LoginResult(
                accessToken,
                refreshToken,
                accessTokenProvider.expiresInSeconds()
        );
    }

    @Transactional(readOnly = true)
    public AccessTokenResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        final String tokenHash = refreshTokenProvider.hash(refreshToken);
        final RefreshSession refreshSession = refreshSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshSession.isExpired(refreshTokenProvider.now())) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return new AccessTokenResult(
                accessTokenProvider.create(refreshSession.getMember().getId()),
                accessTokenProvider.expiresInSeconds()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        final String tokenHash = refreshTokenProvider.hash(refreshToken);
        final Optional<RefreshSession> refreshSession = refreshSessionRepository.findByTokenHash(tokenHash);
        refreshSession.ifPresent(refreshSessionRepository::delete);
    }
}
