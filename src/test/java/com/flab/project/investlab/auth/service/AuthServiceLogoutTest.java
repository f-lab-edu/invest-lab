package com.flab.project.investlab.auth.service;

import com.flab.project.investlab.auth.domain.RefreshSession;
import com.flab.project.investlab.auth.repository.RefreshSessionRepository;
import com.flab.project.investlab.auth.token.AccessTokenProvider;
import com.flab.project.investlab.auth.token.RefreshTokenProvider;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthServiceLogoutTest {

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
    void 로그아웃하면_현재_리프레시_세션을_삭제한다() {
        // Given
        final RefreshSession refreshSession = mock(RefreshSession.class);
        when(refreshTokenProvider.hash("refresh-token")).thenReturn("refresh-token-hash");
        when(refreshSessionRepository.findByTokenHash("refresh-token-hash"))
                .thenReturn(Optional.of(refreshSession));

        // When
        authService.logout("refresh-token");

        // Then
        verify(refreshSessionRepository).delete(refreshSession);
    }

    @Test
    void 직전_리프레시_토큰으로_로그아웃해도_현재_세션을_삭제한다() {
        // Given
        final RefreshSession refreshSession = mock(RefreshSession.class);
        when(refreshTokenProvider.hash("previous-token")).thenReturn("previous-token-hash");
        when(refreshSessionRepository.findByTokenHash("previous-token-hash"))
                .thenReturn(Optional.empty());
        when(refreshSessionRepository.findByPreviousTokenHash("previous-token-hash"))
                .thenReturn(Optional.of(refreshSession));

        // When
        authService.logout("previous-token");

        // Then
        verify(refreshSessionRepository).delete(refreshSession);
    }

    @Test
    void 리프레시_토큰이_없는_로그아웃도_성공한다() {
        // When
        authService.logout(null);

        // Then
        verifyNoInteractions(refreshTokenProvider, refreshSessionRepository);
    }
}
