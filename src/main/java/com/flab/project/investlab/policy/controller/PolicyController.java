package com.flab.project.investlab.policy.controller;

import com.flab.project.investlab.policy.dto.PolicyCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyCreateResponse;
import com.flab.project.investlab.policy.dto.PolicySummaryResponse;
import com.flab.project.investlab.policy.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(final PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyCreateResponse create(
            @AuthenticationPrincipal final Jwt jwt,
            @Valid @RequestBody final PolicyCreateRequest request
    ) {
        return policyService.create(Long.valueOf(jwt.getSubject()), request);
    }

    @GetMapping
    public List<PolicySummaryResponse> getMyPolicies(@AuthenticationPrincipal final Jwt jwt) {
        return policyService.getMyPolicies(Long.valueOf(jwt.getSubject()));
    }
}
