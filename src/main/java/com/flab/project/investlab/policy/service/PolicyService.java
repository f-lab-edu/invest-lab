package com.flab.project.investlab.policy.service;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.instrument.repository.InstrumentRepository;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import com.flab.project.investlab.policy.domain.AllocationDraft;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import com.flab.project.investlab.policy.dto.PolicyCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyCreateResponse;
import com.flab.project.investlab.policy.dto.PolicyDetailResponse;
import com.flab.project.investlab.policy.dto.PolicySummaryResponse;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
import com.flab.project.investlab.policy.repository.PolicyRepository;
import com.flab.project.investlab.policy.repository.PolicyVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private static final int FIRST_VERSION_NO = 1;

    private final PolicyRepository policyRepository;
    private final PolicyVersionRepository policyVersionRepository;
    private final InstrumentRepository instrumentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PolicyCreateResponse create(final Jwt jwt, final PolicyCreateRequest request) {
        final PolicyVersion sourceVersion = findSourceVersion(request.sourceVersionId());
        final List<AllocationDraft> drafts = toDrafts(request.allocations());

        final Member member = memberRepository.getReferenceById(memberIdOf(jwt));
        final Policy policy = new Policy(member, request.name());
        final PolicyVersion version = PolicyVersion.create(
                policy,
                sourceVersion,
                FIRST_VERSION_NO,
                request.reviewIntervalMonths(),
                request.thresholdBps(),
                request.allowSell(),
                request.cashTargetBps(),
                drafts
        );

        policyRepository.save(policy);
        policyVersionRepository.save(version);
        return new PolicyCreateResponse(policy.getId(), version.getId());
    }

    @Transactional(readOnly = true)
    public List<PolicySummaryResponse> getMyPolicies(final Jwt jwt) {
        return policyRepository.findAllByMemberIdOrderByIdDesc(memberIdOf(jwt)).stream()
                .map(policy -> new PolicySummaryResponse(
                        policy.getId(),
                        policy.getName(),
                        policy.getLatestVersionNo(),
                        policy.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PolicyDetailResponse getMyPolicy(final Jwt jwt, final Long policyId) {
        final PolicyVersion version = policyVersionRepository.findLatestMemberVersion(policyId, memberIdOf(jwt))
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));
        return PolicyDetailResponse.from(version);
    }

    private Long memberIdOf(final Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private PolicyVersion findSourceVersion(final Long sourceVersionId) {
        if (sourceVersionId == null) {
            return null;
        }
        return policyVersionRepository.findByIdAndPolicyMemberIsNull(sourceVersionId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.SOURCE_VERSION_NOT_FOUND));
    }

    private List<AllocationDraft> toDrafts(final List<PolicyCreateRequest.Allocation> allocations) {
        final Set<Long> instrumentIds = allocations.stream()
                .map(PolicyCreateRequest.Allocation::instrumentId)
                .collect(Collectors.toSet());
        final Map<Long, Instrument> instruments = instrumentRepository.findAllById(instrumentIds).stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        if (instruments.size() != instrumentIds.size()) {
            throw new PolicyException(PolicyErrorCode.INSTRUMENT_NOT_FOUND);
        }

        return allocations.stream()
                .map(allocation -> new AllocationDraft(
                        allocation.assetClass(),
                        instruments.get(allocation.instrumentId()),
                        allocation.targetBps()
                ))
                .toList();
    }
}
