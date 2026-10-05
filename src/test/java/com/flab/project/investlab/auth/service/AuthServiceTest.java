package com.flab.project.investlab.auth.service;

import com.flab.project.investlab.auth.domain.RefreshSession;
import com.flab.project.investlab.auth.exception.AuthException;
import com.flab.project.investlab.auth.repository.RefreshSessionRepository;
import com.flab.project.investlab.auth.token.AccessTokenProvider;
import com.flab.project.investlab.auth.token.RefreshTokenProvider;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

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
    void 로그인하면_액세스_토큰과_리프레시_세션을_생성한다() {
        // Given
        Member member = member(1L, "encoded-password");
        Instant expiresAt = Instant.parse("2026-10-12T00:00:00Z");
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1234", "encoded-password")).thenReturn(true);
        when(accessTokenProvider.create(1L)).thenReturn("access-token");
        when(accessTokenProvider.expiresInSeconds()).thenReturn(1800L);
        when(refreshTokenProvider.create()).thenReturn("refresh-token");
        when(refreshTokenProvider.hash("refresh-token")).thenReturn("refresh-token-hash");
        when(refreshTokenProvider.expiresAt()).thenReturn(expiresAt);
        when(refreshSessionRepository.findByMemberId(1L)).thenReturn(Optional.empty());

        // When
        LoginResult result = authService.login("MEMBER@example.com", "password1234");

        // Then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.accessTokenExpiresIn()).isEqualTo(1800L);

        ArgumentCaptor<RefreshSession> captor = ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getMember()).isSameAs(member);
        assertThat(captor.getValue().getTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void 다시_로그인하면_기존_리프레시_세션을_교체한다() {
        // Given
        Member member = member(1L, "encoded-password");
        RefreshSession refreshSession = mock(RefreshSession.class);
        Instant expiresAt = Instant.parse("2026-10-12T00:00:00Z");
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1234", "encoded-password")).thenReturn(true);
        when(accessTokenProvider.create(1L)).thenReturn("access-token");
        when(refreshTokenProvider.create()).thenReturn("new-refresh-token");
        when(refreshTokenProvider.hash("new-refresh-token")).thenReturn("new-refresh-token-hash");
        when(refreshTokenProvider.expiresAt()).thenReturn(expiresAt);
        when(refreshSessionRepository.findByMemberId(1L)).thenReturn(Optional.of(refreshSession));

        // When
        authService.login("member@example.com", "password1234");

        // Then
        verify(refreshSession).replaceForLogin("new-refresh-token-hash", expiresAt);
        verify(refreshSessionRepository).save(refreshSession);
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_동일한_로그인_실패_예외가_발생한다() {
        // Given
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertLoginFailed(() -> authService.login("member@example.com", "password1234"));
    }

    @Test
    void 잘못된_비밀번호로_로그인하면_동일한_로그인_실패_예외가_발생한다() {
        // Given
        Member member = member(1L, "encoded-password");
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // When & Then
        assertLoginFailed(() -> authService.login("member@example.com", "wrong-password"));
    }

    private Member member(Long id, String passwordHash) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getPasswordHash()).thenReturn(passwordHash);
        return member;
    }

    private void assertLoginFailed(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo("AUTH_LOGIN_FAILED");
                    assertThat(authException.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }
}
