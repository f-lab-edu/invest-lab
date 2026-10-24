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
import com.flab.project.investlab.policy.dto.PolicyListResponse;
import com.flab.project.investlab.policy.dto.PolicyVersionCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyVersionCreateResponse;
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
                null,
                drafts
        );

        policyRepository.save(policy);
        policyVersionRepository.save(version);
        return new PolicyCreateResponse(policy.getId(), version.getId());
    }

    @Transactional
    public PolicyVersionCreateResponse createVersion(
            final Jwt jwt,
            final Long policyId,
            final PolicyVersionCreateRequest request
    ) {
        final Long memberId = memberIdOf(jwt);
        final Policy policy = policyRepository.findMemberPolicyForUpdate(policyId, memberId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));
        final PolicyVersion baseVersion = policyVersionRepository.findByPolicyIdAndVersionNo(policyId, policy.getLatestVersionNo())
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));
        validateBaseVersion(baseVersion.getId(), request.baseVersionId());

        final List<AllocationDraft> drafts = toVersionDrafts(request.allocations());
        final PolicyVersion version = PolicyVersion.create(
                policy,
                baseVersion,
                policy.getLatestVersionNo() + 1,
                request.reviewIntervalMonths(),
                request.thresholdBps(),
                request.allowSell(),
                request.cashTargetBps(),
                request.changeReason(),
                drafts
        );

        policyVersionRepository.save(version);
        return new PolicyVersionCreateResponse(policy.getId(), version.getId(), version.getVersionNo());
    }

    @Transactional(readOnly = true)
    public PolicyListResponse getMyPolicies(final Jwt jwt) {
        final List<PolicyListResponse.Item> items = policyRepository.findAllByMemberIdOrderByIdDesc(memberIdOf(jwt)).stream()
                .map(policy -> new PolicyListResponse.Item(
                        policy.getId(),
                        policy.getName(),
                        policy.getLatestVersionNo(),
                        policy.getCreatedAt()
                ))
                .toList();
        return new PolicyListResponse(items);
    }

    @Transactional(readOnly = true)
    public PolicyDetailResponse getMyPolicy(final Jwt jwt, final Long policyId) {
        final PolicyVersion version = policyVersionRepository.findLatestMemberVersion(policyId, memberIdOf(jwt))
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));
        return PolicyDetailResponse.from(version);
    }

    private void validateBaseVersion(final Long latestVersionId, final Long baseVersionId) {
        if (!latestVersionId.equals(baseVersionId)) {
            throw new PolicyException(PolicyErrorCode.VERSION_CONFLICT);
        }
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
        final Map<Long, Instrument> instruments = findInstruments(instrumentIds);

        return allocations.stream()
                .map(allocation -> new AllocationDraft(
                        allocation.assetClass(),
                        instruments.get(allocation.instrumentId()),
                        allocation.targetBps()
                ))
                .toList();
    }

    private List<AllocationDraft> toVersionDrafts(final List<PolicyVersionCreateRequest.Allocation> allocations) {
        final Set<Long> instrumentIds = allocations.stream()
                .map(PolicyVersionCreateRequest.Allocation::instrumentId)
                .collect(Collectors.toSet());
        final Map<Long, Instrument> instruments = findInstruments(instrumentIds);

        return allocations.stream()
                .map(allocation -> new AllocationDraft(
                        allocation.assetClass(),
                        instruments.get(allocation.instrumentId()),
                        allocation.targetBps()
                ))
                .toList();
    }

    private Map<Long, Instrument> findInstruments(final Set<Long> instrumentIds) {
        final Map<Long, Instrument> instruments = instrumentRepository.findAllById(instrumentIds).stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        if (instruments.size() != instrumentIds.size()) {
            throw new PolicyException(PolicyErrorCode.INSTRUMENT_NOT_FOUND);
        }
        return instruments;
    }
}
