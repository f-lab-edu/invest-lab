package com.flab.project.investlab.policy.dto;

import com.flab.project.investlab.policy.domain.Policy;

public record PresetSummaryResponse(
        Long id,
        String name
) {

    public static PresetSummaryResponse from(final Policy policy) {
        return new PresetSummaryResponse(policy.getId(), policy.getName());
    }
}
