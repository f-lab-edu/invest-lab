package com.flab.project.investlab.policy.service;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.instrument.repository.InstrumentRepository;
import com.flab.project.investlab.member.domain.Member;
import com.flab.project.investlab.member.repository.MemberRepository;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyAllocation;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import com.flab.project.investlab.policy.dto.PolicyCreateRequest;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
import com.flab.project.investlab.policy.repository.PolicyRepository;
import com.flab.project.investlab.policy.repository.PolicyVersionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    private final Member member = new Member("member@example.com", "encoded-password", "investor");
    private final Instrument stockEtf = instrument(2L, "360750", "TIGER 미국S&P500");
    private final Instrument bondEtf = instrument(4L, "114260", "KODEX 국고채3년");

    @Test
    void 프리셋을_복사해_저장하면_프리셋_버전을_부모로_하는_첫_버전을_저장한다() {
        // given
        final PolicyVersion presetVersion = PolicyVersion.create(new Policy(null, "프리셋"), null, 1, 1, 500, true, 10_000, List.of());
        when(policyVersionRepository.findByIdAndPolicyMemberIsNull(1L)).thenReturn(Optional.of(presetVersion));
        when(instrumentRepository.findAllById(Set.of(2L, 4L))).thenReturn(List.of(stockEtf, bondEtf));
        when(memberRepository.getReferenceById(10L)).thenReturn(member);

        // when
        policyService.create(10L, request(1L, List.of(
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
        policyService.create(10L, request(null, List.of(
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
        assertThatThrownBy(() -> policyService.create(10L, request(99L, List.of(
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
        assertThatThrownBy(() -> policyService.create(10L, request(null, List.of(
                new PolicyCreateRequest.Allocation("미국주식", 2L, 6000),
                new PolicyCreateRequest.Allocation("국내채권", 999L, 4000)
        ))))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.INSTRUMENT_NOT_FOUND.getCode());
        verify(policyRepository, never()).save(any());
        verify(policyVersionRepository, never()).save(any());
    }

    private PolicyCreateRequest request(final Long sourceVersionId, final List<PolicyCreateRequest.Allocation> allocations) {
        return new PolicyCreateRequest("내 첫 규칙", sourceVersionId, 1, 500, true, 0, allocations);
    }

    private Instrument instrument(final Long id, final String symbol, final String name) {
        final Instrument instrument = new Instrument("KRX", symbol, name);
        ReflectionTestUtils.setField(instrument, "id", id);
        return instrument;
    }
}
