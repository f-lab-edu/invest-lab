package com.flab.project.investlab.policy.dto;

import java.time.Instant;

public record PolicySummaryResponse(
        Long id,
        String name,
        int latestVersionNo,
        Instant createdAt
) {
}
