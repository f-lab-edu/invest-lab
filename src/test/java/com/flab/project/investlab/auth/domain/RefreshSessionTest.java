package com.flab.project.investlab.auth.domain;

import com.flab.project.investlab.member.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshSessionTest {

    private final Member member = new Member("member@example.com", "encoded-password", "investor");

    @Test
    void 다시_로그인하면_토큰과_만료시간을_교체한다() {
        // given
        final RefreshSession refreshSession = session("old-token-hash");
        final Instant newExpiresAt = Instant.parse("2026-10-13T00:00:00Z");

        // when
        refreshSession.replace("new-token-hash", newExpiresAt);

        // then
        assertThat(refreshSession.getTokenHash()).isEqualTo("new-token-hash");
        assertThat(refreshSession.getExpiresAt()).isEqualTo(newExpiresAt);
    }

    @Test
    void 만료시각과_현재시각이_같으면_만료된_세션이다() {
        // given
        final RefreshSession refreshSession = session("token-hash");

        // when & then
        assertThat(refreshSession.isExpired(Instant.parse("2026-10-12T00:00:00Z"))).isTrue();
        assertThat(refreshSession.isExpired(Instant.parse("2026-10-11T23:59:59Z"))).isFalse();
    }

    private RefreshSession session(String tokenHash) {
        return new RefreshSession(member, tokenHash, Instant.parse("2026-10-12T00:00:00Z"));
    }
}
