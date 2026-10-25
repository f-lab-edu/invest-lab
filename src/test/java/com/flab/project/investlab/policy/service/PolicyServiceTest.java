package com.flab.project.investlab.policy.service;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.instrument.domain.Market;
import com.flab.project.investlab.instrument.repository.InstrumentRepository;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import com.flab.project.investlab.policy.domain.AllocationDraft;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyAllocation;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import com.flab.project.investlab.policy.dto.PolicyCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyDetailResponse;
import com.flab.project.investlab.policy.dto.PolicyListResponse;
import com.flab.project.investlab.policy.dto.PolicyVersionCreateRequest;
import com.flab.project.investlab.policy.dto.PolicyVersionCreateResponse;
import com.flab.project.investlab.policy.dto.PolicyVersionListResponse;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
import com.flab.project.investlab.policy.repository.PolicyRepository;
import com.flab.project.investlab.policy.repository.PolicyVersionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyServiceTest {

    private final PolicyRepository policyRepository = mock(PolicyRepository.class);
    private final PolicyVersionRepository policyVersionRepository = mock(PolicyVersionRepository.class);
    private final InstrumentRepository instrumentRepository = mock(InstrumentRepository.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final PolicyService policyService = new PolicyService(
            policyRepository,
            policyVersionRepository,
            instrumentRepository,
            memberRepository
    );

    private final Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "HS256")
            .subject("10")
            .build();
    private final Member member = new Member("member@example.com", "encoded-password", "investor");
    private final Instrument stockEtf = instrument(2L, "360750", "TIGER 미국S&P500");
    private final Instrument bondEtf = instrument(4L, "114260", "KODEX 국고채3년");

    @Test
    void 프리셋을_복사해_저장하면_프리셋_버전을_부모로_하는_첫_버전을_저장한다() {
        // given
        final PolicyVersion presetVersion = PolicyVersion.create(new Policy(null, "프리셋"), null, 1, 1, 500, true, 10_000, null, List.of());
        when(policyVersionRepository.findByIdAndPolicyMemberIsNull(1L)).thenReturn(Optional.of(presetVersion));
        when(instrumentRepository.findAllById(Set.of(2L, 4L))).thenReturn(List.of(stockEtf, bondEtf));
        when(memberRepository.getReferenceById(10L)).thenReturn(member);

        // when
        policyService.create(jwt, request(1L, List.of(
                new PolicyCreateRequest.Allocation("미국주식", 2L, 6000),
                new PolicyCreateRequest.Allocation("국내채권", 4L, 4000)
        )));

        // then
        final ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepository).save(policyCaptor.capture());
        assertThat(policyCaptor.getValue().getMember()).isSameAs(member);
        assertThat(policyCaptor.getValue().getName()).isEqualTo("내 첫 규칙");

        final ArgumentCaptor<PolicyVersion> versionCaptor = ArgumentCaptor.forClass(PolicyVersion.class);
        verify(policyVersionRepository).save(versionCaptor.capture());
        final PolicyVersion savedVersion = versionCaptor.getValue();
        assertThat(savedVersion.getPolicy()).isSameAs(policyCaptor.getValue());
        assertThat(savedVersion.getParentVersion()).isSameAs(presetVersion);
        assertThat(savedVersion.getVersionNo()).isEqualTo(1);
        assertThat(savedVersion.getAllocations())
                .extracting(PolicyAllocation::getAssetClass, PolicyAllocation::getInstrument, PolicyAllocation::getTargetBps)
                .containsExactly(
                        tuple("미국주식", stockEtf, 6000),
                        tuple("국내채권", bondEtf, 4000)
                );
    }

    @Test
    void 출처_없이_직접_만들면_부모_버전_없이_저장한다() {
        // given
        when(instrumentRepository.findAllById(Set.of(2L))).thenReturn(List.of(stockEtf));
        when(memberRepository.getReferenceById(10L)).thenReturn(member);

        // when
        policyService.create(jwt, request(null, List.of(
                new PolicyCreateRequest.Allocation("미국주식", 2L, 10_000)
        )));

        // then
        final ArgumentCaptor<PolicyVersion> versionCaptor = ArgumentCaptor.forClass(PolicyVersion.class);
        verify(policyVersionRepository).save(versionCaptor.capture());
        assertThat(versionCaptor.getValue().getParentVersion()).isNull();
    }

    @Test
    void 프리셋이_아닌_출처_버전이면_저장하지_않고_예외가_발생한다() {
        // given
        when(policyVersionRepository.findByIdAndPolicyMemberIsNull(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> policyService.create(jwt, request(99L, List.of(
                new PolicyCreateRequest.Allocation("미국주식", 2L, 10_000)
        ))))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.SOURCE_VERSION_NOT_FOUND.getCode());
        verify(policyRepository, never()).save(any());
        verify(policyVersionRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_ETF가_포함되면_저장하지_않고_예외가_발생한다() {
        // given
        when(instrumentRepository.findAllById(Set.of(2L, 999L))).thenReturn(List.of(stockEtf));

        // when & then
        assertThatThrownBy(() -> policyService.create(jwt, request(null, List.of(
                new PolicyCreateRequest.Allocation("미국주식", 2L, 6000),
                new PolicyCreateRequest.Allocation("국내채권", 999L, 4000)
        ))))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.INSTRUMENT_NOT_FOUND.getCode());
        verify(policyRepository, never()).save(any());
        verify(policyVersionRepository, never()).save(any());
    }

    @Test
    void 최신_버전을_수정하면_기존_버전을_유지하고_같은_규칙의_다음_버전을_저장한다() {
        // given
        final Policy policy = new Policy(member, "내 규칙");
        ReflectionTestUtils.setField(policy, "id", 3L);
        final PolicyVersion baseVersion = PolicyVersion.create(policy, null, 1, 1, 500, true, 0, null, List.of(
                new AllocationDraft("미국주식", stockEtf, 10_000)
        ));
        ReflectionTestUtils.setField(baseVersion, "id", 11L);
        when(policyRepository.findMemberPolicyForUpdate(3L, 10L)).thenReturn(Optional.of(policy));
        when(policyVersionRepository.findByPolicyIdAndVersionNo(3L, 1)).thenReturn(Optional.of(baseVersion));
        when(instrumentRepository.findAllById(Set.of(2L, 4L))).thenReturn(List.of(stockEtf, bondEtf));
        when(policyVersionRepository.save(any())).thenAnswer(invocation -> {
            final PolicyVersion version = invocation.getArgument(0);
            ReflectionTestUtils.setField(version, "id", 12L);
            return version;
        });

        // when
        final PolicyVersionCreateResponse response = policyService.createVersion(jwt, 3L, versionRequest(11L, List.of(
                new PolicyVersionCreateRequest.Allocation("미국주식", 2L, 6000),
                new PolicyVersionCreateRequest.Allocation("국내채권", 4L, 4000)
        )));

        // then
        final ArgumentCaptor<PolicyVersion> versionCaptor = ArgumentCaptor.forClass(PolicyVersion.class);
        final InOrder inOrder = inOrder(policyRepository, policyVersionRepository);
        inOrder.verify(policyRepository).findMemberPolicyForUpdate(3L, 10L);
        inOrder.verify(policyVersionRepository).findByPolicyIdAndVersionNo(3L, 1);
        inOrder.verify(policyVersionRepository).save(versionCaptor.capture());
        final PolicyVersion savedVersion = versionCaptor.getValue();
        assertThat(savedVersion.getPolicy()).isSameAs(policy);
        assertThat(savedVersion.getParentVersion()).isSameAs(baseVersion);
        assertThat(savedVersion.getVersionNo()).isEqualTo(2);
        assertThat(policy.getLatestVersionNo()).isEqualTo(2);
        assertThat(response).isEqualTo(new PolicyVersionCreateResponse(3L, 12L, 2));
        assertThat(baseVersion.getVersionNo()).isEqualTo(1);
        assertThat(baseVersion.getChangeReason()).isNull();
        assertThat(baseVersion.getAllocations())
                .extracting(PolicyAllocation::getInstrument, PolicyAllocation::getTargetBps)
                .containsExactly(tuple(stockEtf, 10_000));
        assertThat(savedVersion.getAllocations())
                .extracting(PolicyAllocation::getInstrument)
                .containsExactly(stockEtf, bondEtf);
        verify(policyRepository, never()).save(any());
    }

    @Test
    void 수정할_내_규칙이_없으면_버전을_저장하지_않는다() {
        // given
        when(policyRepository.findMemberPolicyForUpdate(3L, 10L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> policyService.createVersion(jwt, 3L, versionRequest(11L, List.of(
                new PolicyVersionCreateRequest.Allocation("미국주식", 2L, 10_000)
        ))))
                .isInstanceOf(PolicyException.class)
                .satisfies(exception -> {
                    final PolicyException policyException = (PolicyException) exception;
                    assertThat(policyException.getErrorCode()).isEqualTo(PolicyErrorCode.POLICY_NOT_FOUND.getCode());
                    assertThat(policyException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
        verify(policyVersionRepository, never()).save(any());
    }

    @Test
    void 기준_버전이_최신이_아니면_충돌로_응답하고_저장하지_않는다() {
        // given
        final Policy policy = policy(3L, "내 규칙", 2);
        final PolicyVersion latest = PolicyVersion.create(policy, null, 2, 1, 500, true, 10_000, null, List.of());
        ReflectionTestUtils.setField(latest, "id", 12L);
        when(policyRepository.findMemberPolicyForUpdate(3L, 10L)).thenReturn(Optional.of(policy));
        when(policyVersionRepository.findByPolicyIdAndVersionNo(3L, 2)).thenReturn(Optional.of(latest));

        // when & then
        assertThatThrownBy(() -> policyService.createVersion(jwt, 3L, versionRequest(11L, List.of(
                new PolicyVersionCreateRequest.Allocation("미국주식", 2L, 10_000)
        ))))
                .isInstanceOf(PolicyException.class)
                .satisfies(exception -> {
                    final PolicyException policyException = (PolicyException) exception;
                    assertThat(policyException.getErrorCode()).isEqualTo(PolicyErrorCode.VERSION_CONFLICT.getCode());
                    assertThat(policyException.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
        assertThat(policy.getLatestVersionNo()).isEqualTo(2);
        verify(policyVersionRepository, never()).save(any());
        verify(instrumentRepository, never()).findAllById(any());
    }

    @Test
    void 수정_배분에_없는_ETF가_있으면_최신_번호를_바꾸거나_저장하지_않는다() {
        // given
        final Policy policy = policy(3L, "내 규칙", 1);
        final PolicyVersion baseVersion = PolicyVersion.create(policy, null, 1, 1, 500, true, 10_000, null, List.of());
        ReflectionTestUtils.setField(baseVersion, "id", 11L);
        when(policyRepository.findMemberPolicyForUpdate(3L, 10L)).thenReturn(Optional.of(policy));
        when(policyVersionRepository.findByPolicyIdAndVersionNo(3L, 1)).thenReturn(Optional.of(baseVersion));
        when(instrumentRepository.findAllById(Set.of(2L, 999L))).thenReturn(List.of(stockEtf));

        // when & then
        assertThatThrownBy(() -> policyService.createVersion(jwt, 3L, versionRequest(11L, List.of(
                new PolicyVersionCreateRequest.Allocation("미국주식", 2L, 6000),
                new PolicyVersionCreateRequest.Allocation("국내채권", 999L, 4000)
        ))))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.INSTRUMENT_NOT_FOUND.getCode());
        assertThat(policy.getLatestVersionNo()).isEqualTo(1);
        verify(policyVersionRepository, never()).save(any());
    }

    @Test
    void 수정_비중_합계가_잘못되면_최신_번호를_바꾸거나_저장하지_않는다() {
        // given
        final Policy policy = policy(3L, "내 규칙", 1);
        final PolicyVersion baseVersion = PolicyVersion.create(policy, null, 1, 1, 500, true, 10_000, null, List.of());
        ReflectionTestUtils.setField(baseVersion, "id", 11L);
        when(policyRepository.findMemberPolicyForUpdate(3L, 10L)).thenReturn(Optional.of(policy));
        when(policyVersionRepository.findByPolicyIdAndVersionNo(3L, 1)).thenReturn(Optional.of(baseVersion));
        when(instrumentRepository.findAllById(Set.of(2L))).thenReturn(List.of(stockEtf));

        // when & then
        assertThatThrownBy(() -> policyService.createVersion(jwt, 3L, versionRequest(11L, List.of(
                new PolicyVersionCreateRequest.Allocation("미국주식", 2L, 9999)
        ))))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.TARGET_SUM_INVALID.getCode());
        assertThat(policy.getLatestVersionNo()).isEqualTo(1);
        verify(policyVersionRepository, never()).save(any());
    }

    @Test
    void 내_규칙_목록을_조회하면_items로_감싸_반환한다() {
        // given
        final Policy second = policy(4L, "두번째 규칙", 1);
        final Policy first = policy(3L, "첫 규칙", 2);
        when(policyRepository.findAllByMemberIdOrderByIdDesc(10L)).thenReturn(List.of(second, first));

        // when
        final PolicyListResponse response = policyService.getMyPolicies(jwt);

        // then
        assertThat(response.items())
                .extracting(PolicyListResponse.Item::id, PolicyListResponse.Item::name, PolicyListResponse.Item::latestVersionNo)
                .containsExactly(
                        tuple(4L, "두번째 규칙", 1),
                        tuple(3L, "첫 규칙", 2)
                );
    }

    @Test
    void 내_규칙을_조회하면_최신_버전을_응답으로_반환한다() {
        // given
        final Policy policy = new Policy(member, "내 규칙");
        final PolicyVersion version = PolicyVersion.create(policy, null, 2, 3, 300, false, 1000, null, List.of(
                new AllocationDraft("미국주식", stockEtf, 9000)
        ));
        when(policyVersionRepository.findLatestMemberVersion(3L, 10L)).thenReturn(Optional.of(version));

        // when
        final PolicyDetailResponse response = policyService.getMyPolicy(jwt, 3L);

        // then
        assertThat(response.name()).isEqualTo("내 규칙");
        assertThat(response.versionNo()).isEqualTo(2);
        assertThat(response.reviewIntervalMonths()).isEqualTo(3);
        assertThat(response.thresholdBps()).isEqualTo(300);
        assertThat(response.allowSell()).isFalse();
        assertThat(response.cashTargetBps()).isEqualTo(1000);
        assertThat(response.allocations())
                .extracting(PolicyDetailResponse.Allocation::assetClass,
                        PolicyDetailResponse.Allocation::instrumentId,
                        PolicyDetailResponse.Allocation::targetBps)
                .containsExactly(tuple("미국주식", 2L, 9000));
    }

    @Test
    void 내_규칙으로_조회되지_않으면_예외가_발생한다() {
        // given
        when(policyVersionRepository.findLatestMemberVersion(3L, 10L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> policyService.getMyPolicy(jwt, 3L))
                .isInstanceOf(PolicyException.class)
                .satisfies(exception -> {
                    final PolicyException policyException = (PolicyException) exception;
                    assertThat(policyException.getErrorCode()).isEqualTo(PolicyErrorCode.POLICY_NOT_FOUND.getCode());
                    assertThat(policyException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    void 버전_목록은_부모_연결과_첫_버전의_null_수정_이유를_반환한다() {
        // given
        final Policy policy = new Policy(member, "내 규칙");
        final PolicyVersion first = PolicyVersion.create(policy, null, 1, 1, 500, true, 10_000, null, List.of());
        ReflectionTestUtils.setField(first, "id", 11L);
        final PolicyVersion second = PolicyVersion.create(policy, first, 2, 1, 500, true, 10_000, "점검 주기를 검토했습니다.", List.of());
        ReflectionTestUtils.setField(second, "id", 12L);
        final Instant createdAt = Instant.parse("2026-10-24T10:00:00Z");
        ReflectionTestUtils.setField(second, "createdAt", createdAt);
        when(policyVersionRepository.findMemberVersions(3L, 10L)).thenReturn(List.of(second, first));

        // when
        final PolicyVersionListResponse response = policyService.getMyPolicyVersions(jwt, 3L);

        // then
        assertThat(response.items())
                .extracting(PolicyVersionListResponse.Item::versionId,
                        PolicyVersionListResponse.Item::versionNo,
                        PolicyVersionListResponse.Item::parentVersionId)
                .containsExactly(
                        tuple(12L, 2, 11L),
                        tuple(11L, 1, null)
                );
        assertThat(response.items().get(0).changeReason()).isEqualTo(second.getChangeReason());
        assertThat(response.items().get(0).createdAt()).isEqualTo(createdAt);
        assertThat(response.items().get(1).changeReason()).isNull();
    }

    @Test
    void 조회할_내_규칙의_버전_목록이_없으면_404로_처리한다() {
        // given
        when(policyVersionRepository.findMemberVersions(3L, 10L)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> policyService.getMyPolicyVersions(jwt, 3L))
                .isInstanceOf(PolicyException.class)
                .satisfies(exception -> {
                    final PolicyException policyException = (PolicyException) exception;
                    assertThat(policyException.getErrorCode()).isEqualTo(PolicyErrorCode.POLICY_NOT_FOUND.getCode());
                    assertThat(policyException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    private Policy policy(final Long id, final String name, final int latestVersionNo) {
        final Policy policy = new Policy(member, name);
        PolicyVersion.create(policy, null, latestVersionNo, 1, 500, true, 10_000, null, List.of());
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }

    private PolicyCreateRequest request(final Long sourceVersionId, final List<PolicyCreateRequest.Allocation> allocations) {
        return new PolicyCreateRequest("내 첫 규칙", sourceVersionId, 1, 500, true, 0, allocations);
    }

    private PolicyVersionCreateRequest versionRequest(
            final Long baseVersionId,
            final List<PolicyVersionCreateRequest.Allocation> allocations
    ) {
        return new PolicyVersionCreateRequest(baseVersionId, "채권 비중을 늘려 비교합니다.", 1, 500, true, 0, allocations);
    }

    private Instrument instrument(final Long id, final String symbol, final String name) {
        final Instrument instrument = new Instrument(Market.KRX, symbol, name);
        ReflectionTestUtils.setField(instrument, "id", id);
        return instrument;
    }
}
