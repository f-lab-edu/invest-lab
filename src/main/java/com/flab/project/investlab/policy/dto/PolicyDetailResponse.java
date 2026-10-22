package com.flab.project.investlab.policy.dto;

import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyAllocation;
import com.flab.project.investlab.policy.domain.PolicyVersion;

import java.util.List;

public record PolicyDetailResponse(
        Long id,
        String name,
        Long versionId,
        int versionNo,
        int reviewIntervalMonths,
        int thresholdBps,
        boolean allowSell,
        int cashTargetBps,
        List<Allocation> allocations
) {

    public static PolicyDetailResponse from(final PolicyVersion version) {
        final Policy policy = version.getPolicy();
        return new PolicyDetailResponse(
                policy.getId(),
                policy.getName(),
                version.getId(),
                version.getVersionNo(),
                version.getReviewIntervalMonths(),
                version.getThresholdBps(),
                version.isAllowSell(),
                version.getCashTargetBps(),
                version.getAllocations().stream()
                        .map(Allocation::from)
                        .toList()
        );
    }

    public record Allocation(
            String assetClass,
            Long instrumentId,
            String symbol,
            String instrumentName,
            int targetBps
    ) {

        static Allocation from(final PolicyAllocation allocation) {
            return new Allocation(
                    allocation.getAssetClass(),
                    allocation.getInstrument().getId(),
                    allocation.getInstrument().getSymbol(),
                    allocation.getInstrument().getName(),
                    allocation.getTargetBps()
            );
        }
    }
}
