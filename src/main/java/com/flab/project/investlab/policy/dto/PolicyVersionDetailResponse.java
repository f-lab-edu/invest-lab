package com.flab.project.investlab.policy.dto;

import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyAllocation;
import com.flab.project.investlab.policy.domain.PolicyVersion;

import java.time.Instant;
import java.util.List;

public record PolicyVersionDetailResponse(
        Long policyId,
        String name,
        Long versionId,
        int versionNo,
        Long parentVersionId,
        String changeReason,
        Instant createdAt,
        int reviewIntervalMonths,
        int thresholdBps,
        boolean allowSell,
        int cashTargetBps,
        List<Allocation> allocations
) {

    public static PolicyVersionDetailResponse from(final PolicyVersion version) {
        final Policy policy = version.getPolicy();
        final PolicyVersion parentVersion = version.getParentVersion();
        return new PolicyVersionDetailResponse(
                policy.getId(),
                policy.getName(),
                version.getId(),
                version.getVersionNo(),
                parentVersion == null ? null : parentVersion.getId(),
                version.getChangeReason(),
                version.getCreatedAt(),
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
