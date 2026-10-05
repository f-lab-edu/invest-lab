package com.flab.project.investlab.auth.domain;

import com.flab.project.investlab.member.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshSessionTest {

    @Test
    void 리프레시_토큰을_회전하면_기존_해시를_이전_토큰으로_보관한다() {
        // Given
        Member member = new Member("member@example.com", "encoded-password", "investor");
        RefreshSession refreshSession = new RefreshSession(
                member,
                "current-token-hash",
                Instant.parse("2026-10-12T00:00:00Z")
        );

        // When
        refreshSession.rotate(
                "new-token-hash",
                Instant.parse("2026-10-13T00:00:00Z")
        );

        // Then
        assertThat(refreshSession.getPreviousTokenHash()).isEqualTo("current-token-hash");
        assertThat(refreshSession.getTokenHash()).isEqualTo("new-token-hash");
        assertThat(refreshSession.getExpiresAt())
                .isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
    }
}
