package com.flab.project.investlab.policy.service;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.instrument.domain.Market;
import com.flab.project.investlab.policy.domain.Policy;
import com.flab.project.investlab.policy.domain.PolicyVersion;
import com.flab.project.investlab.policy.dto.PresetDetailResponse;
import com.flab.project.investlab.policy.exception.PolicyException;
import com.flab.project.investlab.policy.repository.PolicyRepository;
import com.flab.project.investlab.policy.repository.PolicyVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PresetServiceTest {

    private final PolicyRepository policyRepository = mock(PolicyRepository.class);
    private final PolicyVersionRepository policyVersionRepository = mock(PolicyVersionRepository.class);
    private final PresetService presetService = new PresetService(policyRepository, policyVersionRepository);

    @Test
    void 프리셋_ID로_조회하면_최신_버전을_응답으로_반환한다() {
        // given
        final Policy preset = new Policy(null, "주식 60 · 채권 40");
        final PolicyVersion version = new PolicyVersion(preset, 1, 1, 500, true, 0);
        version.addAllocation("미국주식", new Instrument(Market.KRX, "360750", "TIGER 미국S&P500"), 6000);
        version.addAllocation("국내채권", new Instrument(Market.KRX, "114260", "KODEX 국고채3년"), 4000);
        when(policyVersionRepository.findLatestPresetVersion(1L)).thenReturn(Optional.of(version));

        // when
        final PresetDetailResponse response = presetService.getPreset(1L);

        // then
        assertThat(response.name()).isEqualTo("주식 60 · 채권 40");
        assertThat(response.reviewIntervalMonths()).isEqualTo(1);
        assertThat(response.thresholdBps()).isEqualTo(500);
        assertThat(response.allowSell()).isTrue();
        assertThat(response.cashTargetBps()).isZero();
        assertThat(response.allocations())
                .extracting(PresetDetailResponse.Allocation::assetClass,
                        PresetDetailResponse.Allocation::symbol,
                        PresetDetailResponse.Allocation::targetBps)
                .containsExactly(
                        tuple("미국주식", "360750", 6000),
                        tuple("국내채권", "114260", 4000)
                );
    }

    @Test
    void 존재하지_않는_프리셋을_조회하면_예외가_발생한다() {
        // given
        when(policyVersionRepository.findLatestPresetVersion(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> presetService.getPreset(99L))
                .isInstanceOf(PolicyException.class)
                .satisfies(exception -> {
                    final PolicyException policyException = (PolicyException) exception;
                    assertThat(policyException.getErrorCode()).isEqualTo("POLICY_PRESET_NOT_FOUND");
                    assertThat(policyException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }
}
