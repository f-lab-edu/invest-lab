package com.flab.project.investlab.policy.dto;

public record PolicyVersionCreateResponse(
        Long policyId,
        Long versionId,
        int versionNo
) {
}
