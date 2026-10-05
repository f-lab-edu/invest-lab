package com.flab.project.investlab.auth.service;

import com.flab.project.investlab.auth.domain.RefreshSession;
import com.flab.project.investlab.auth.exception.AuthException;
import com.flab.project.investlab.auth.repository.RefreshSessionRepository;
import com.flab.project.investlab.auth.token.AccessTokenProvider;
import com.flab.project.investlab.auth.token.RefreshTokenProvider;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceRefreshTest {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final RefreshSessionRepository refreshSessionRepository = mock(RefreshSessionRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);
    private final RefreshTokenProvider refreshTokenProvider = mock(RefreshTokenProvider.class);
    private final AuthService authService = new AuthService(
            memberRepository,
            refreshSessionRepository,
            passwordEncoder,
            accessTokenProvider,
            refreshTokenProvider
    );

    @Test
    void 유효한_리프레시_토큰을_재발급하면_토큰을_회전한다() {
        // Given
        final Member member = mock(Member.class);
        final Instant now = Instant.parse("2026-10-05T00:00:00Z");
        final Instant expiresAt = Instant.parse("2026-10-12T00:00:00Z");
        final RefreshSession refreshSession = new RefreshSession(
                member,
                "refresh-token-hash",
                expiresAt
        );
        when(member.getId()).thenReturn(1L);
        when(refreshTokenProvider.hash("refresh-token")).thenReturn("refresh-token-hash");
        when(refreshSessionRepository.findByTokenHash("refresh-token-hash"))
                .thenReturn(Optional.of(refreshSession));
        when(refreshTokenProvider.now()).thenReturn(now);
        when(accessTokenProvider.create(1L)).thenReturn("new-access-token");
        when(accessTokenProvider.expiresInSeconds()).thenReturn(1800L);
        when(refreshTokenProvider.create()).thenReturn("new-refresh-token");
        when(refreshTokenProvider.hash("new-refresh-token")).thenReturn("new-refresh-token-hash");

        // When
        final LoginResult result = authService.refresh("refresh-token");

        // Then
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(refreshSession.getPreviousTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(refreshSession.getTokenHash()).isEqualTo("new-refresh-token-hash");
        assertThat(refreshSession.getExpiresAt()).isEqualTo(expiresAt);
        verify(refreshSessionRepository).save(refreshSession);
    }

    @Test
    void 만료된_리프레시_토큰은_세션을_삭제하고_재발급을_거부한다() {
        // Given
        final Member member = new Member("member@example.com", "encoded-password", "investor");
        final Instant now = Instant.parse("2026-10-05T00:00:00Z");
        final RefreshSession refreshSession = new RefreshSession(member, "expired-token-hash", now);
        when(refreshTokenProvider.hash("expired-token")).thenReturn("expired-token-hash");
        when(refreshSessionRepository.findByTokenHash("expired-token-hash"))
                .thenReturn(Optional.of(refreshSession));
        when(refreshTokenProvider.now()).thenReturn(now);

        // When & Then
        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_EXPIRED");
        verify(refreshSessionRepository).delete(refreshSession);
    }

    @Test
    void 이미_사용한_리프레시_토큰은_현재_세션을_삭제하고_재발급을_거부한다() {
        // Given
        final RefreshSession refreshSession = mock(RefreshSession.class);
        when(refreshTokenProvider.hash("reused-token")).thenReturn("reused-token-hash");
        when(refreshSessionRepository.findByTokenHash("reused-token-hash"))
                .thenReturn(Optional.empty());
        when(refreshSessionRepository.findByPreviousTokenHash("reused-token-hash"))
                .thenReturn(Optional.of(refreshSession));

        // When & Then
        assertThatThrownBy(() -> authService.refresh("reused-token"))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_REUSED");
        verify(refreshSessionRepository).delete(refreshSession);
    }

    @Test
    void 알_수_없는_리프레시_토큰은_재발급을_거부한다() {
        // Given
        when(refreshTokenProvider.hash("unknown-token")).thenReturn("unknown-token-hash");
        when(refreshSessionRepository.findByTokenHash("unknown-token-hash"))
                .thenReturn(Optional.empty());
        when(refreshSessionRepository.findByPreviousTokenHash("unknown-token-hash"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refresh("unknown-token"))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_INVALID");
    }

    @Test
    void 리프레시_토큰이_없으면_재발급을_거부한다() {
        // When & Then
        assertThatThrownBy(() -> authService.refresh(null))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_INVALID");
    }
}
