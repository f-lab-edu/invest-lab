package com.flab.project.investlab.policy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PolicyVersionCreateRequest(
        @NotNull
        Long baseVersionId,

        @NotBlank
        @Size(max = 300)
        String changeReason,

        @NotNull
        @Min(1)
        @Max(12)
        Integer reviewIntervalMonths,

        @NotNull
        @Min(1)
        @Max(10_000)
        Integer thresholdBps,

        @NotNull
        Boolean allowSell,

        @NotNull
        @Min(0)
        @Max(10_000)
        Integer cashTargetBps,

        @NotEmpty
        List<@Valid @NotNull Allocation> allocations
) {

    public record Allocation(
            @NotBlank
            @Size(max = 30)
            String assetClass,

            @NotNull
            Long instrumentId,

            @NotNull
            @Min(0)
            @Max(10_000)
            Integer targetBps
    ) {
    }
}
