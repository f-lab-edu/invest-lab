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
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Member member = memberRepository.findByEmail(normalizedEmail)
                .filter(foundMember -> passwordEncoder.matches(rawPassword, foundMember.getPasswordHash()))
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        String accessToken = accessTokenProvider.create(member.getId());
        String refreshToken = refreshTokenProvider.create();
        String refreshTokenHash = refreshTokenProvider.hash(refreshToken);
        Instant expiresAt = refreshTokenProvider.expiresAt();

        RefreshSession refreshSession = refreshSessionRepository.findByMemberId(member.getId())
                .orElseGet(() -> new RefreshSession(member, refreshTokenHash, expiresAt));
        refreshSession.replaceForLogin(refreshTokenHash, expiresAt);
        refreshSessionRepository.save(refreshSession);

        return new LoginResult(
                accessToken,
                refreshToken,
                accessTokenProvider.expiresInSeconds()
        );
    }

    @Transactional(noRollbackFor = AuthException.class)
    public LoginResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        String tokenHash = refreshTokenProvider.hash(refreshToken);
        RefreshSession refreshSession = refreshSessionRepository.findByTokenHash(tokenHash)
                .orElseGet(() -> handleInvalidRefreshToken(tokenHash));

        if (refreshSession.isExpired(refreshTokenProvider.now())) {
            refreshSessionRepository.delete(refreshSession);
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String newAccessToken = accessTokenProvider.create(refreshSession.getMember().getId());
        String newRefreshToken = refreshTokenProvider.create();
        String newRefreshTokenHash = refreshTokenProvider.hash(newRefreshToken);
        refreshSession.rotate(newRefreshTokenHash, refreshTokenProvider.expiresAt());
        refreshSessionRepository.save(refreshSession);

        return new LoginResult(
                newAccessToken,
                newRefreshToken,
                accessTokenProvider.expiresInSeconds()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenHash = refreshTokenProvider.hash(refreshToken);
        Optional<RefreshSession> refreshSession = refreshSessionRepository.findByTokenHash(tokenHash)
                .or(() -> refreshSessionRepository.findByPreviousTokenHash(tokenHash));
        refreshSession.ifPresent(refreshSessionRepository::delete);
    }

    private RefreshSession handleInvalidRefreshToken(String tokenHash) {
        Optional<RefreshSession> reusedSession =
                refreshSessionRepository.findByPreviousTokenHash(tokenHash);
        if (reusedSession.isPresent()) {
            refreshSessionRepository.delete(reusedSession.get());
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REUSED);
        }
        throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }
}
