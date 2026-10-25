package com.flab.project.investlab.policy.controller;

import com.flab.project.investlab.policy.dto.PolicyCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyCreateResponse;
import com.flab.project.investlab.policy.dto.PolicyDetailResponse;
import com.flab.project.investlab.policy.dto.PolicyListResponse;
import com.flab.project.investlab.policy.dto.PolicyVersionCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyVersionCreateResponse;
import com.flab.project.investlab.policy.dto.PolicyVersionListResponse;
import com.flab.project.investlab.policy.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyCreateResponse create(
            @AuthenticationPrincipal final Jwt jwt,
            @Valid @RequestBody final PolicyCreateRequest request
    ) {
        return policyService.create(jwt, request);
    }

    @PostMapping("/{policyId}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyVersionCreateResponse createVersion(
            @AuthenticationPrincipal final Jwt jwt,
            @PathVariable final Long policyId,
            @Valid @RequestBody final PolicyVersionCreateRequest request
    ) {
        return policyService.createVersion(jwt, policyId, request);
    }

    @GetMapping
    public PolicyListResponse getMyPolicies(@AuthenticationPrincipal final Jwt jwt) {
        return policyService.getMyPolicies(jwt);
    }

    @GetMapping("/{policyId}")
    public PolicyDetailResponse getMyPolicy(
            @AuthenticationPrincipal final Jwt jwt,
            @PathVariable final Long policyId
    ) {
        return policyService.getMyPolicy(jwt, policyId);
    }

    @GetMapping("/{policyId}/versions")
    public PolicyVersionListResponse getMyPolicyVersions(
            @AuthenticationPrincipal final Jwt jwt,
            @PathVariable final Long policyId
    ) {
        return policyService.getMyPolicyVersions(jwt, policyId);
    }
}
