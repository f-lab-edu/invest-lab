package com.flab.project.investlab.policy.dto;

import java.time.Instant;
import java.util.List;

public record PolicyListResponse(
        List<Item> items
) {

    public record Item(
            Long id,
            String name,
            int latestVersionNo,
            Instant createdAt
    ) {
    }
}
