package com.flab.project.investlab.auth.domain;

import com.flab.project.investlab.member.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshSessionTest {

    private final Member member = new Member("member@example.com", "encoded-password", "investor");

    @Test
    void 리프레시_토큰을_회전하면_기존_해시를_이전_토큰으로_보관한다() {
        // Given
        final RefreshSession refreshSession = session("current-token-hash");

        // When
        refreshSession.rotate("new-token-hash", Instant.parse("2026-10-13T00:00:00Z"));

        // Then
        assertThat(refreshSession.getPreviousTokenHash()).isEqualTo("current-token-hash");
        assertThat(refreshSession.getTokenHash()).isEqualTo("new-token-hash");
        assertThat(refreshSession.getExpiresAt())
                .isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
    }

    @Test
    void 다시_로그인하면_이전_토큰_해시를_초기화한다() {
        // Given
        final RefreshSession refreshSession = session("first-token-hash");
        refreshSession.rotate("second-token-hash", Instant.parse("2026-10-12T00:00:00Z"));

        // When
        refreshSession.replaceForLogin("login-token-hash", Instant.parse("2026-10-13T00:00:00Z"));

        // Then
        assertThat(refreshSession.getPreviousTokenHash()).isNull();
        assertThat(refreshSession.getTokenHash()).isEqualTo("login-token-hash");
    }

    @Test
    void 만료시각과_현재시각이_같으면_만료된_세션이다() {
        // Given
        final RefreshSession refreshSession = session("token-hash");

        // When & Then
        assertThat(refreshSession.isExpired(Instant.parse("2026-10-12T00:00:00Z"))).isTrue();
        assertThat(refreshSession.isExpired(Instant.parse("2026-10-11T23:59:59Z"))).isFalse();
    }

    private RefreshSession session(String tokenHash) {
        return new RefreshSession(member, tokenHash, Instant.parse("2026-10-12T00:00:00Z"));
    }
}
