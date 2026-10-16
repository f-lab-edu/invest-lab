package com.flab.project.investlab.policy.domain;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class PolicyVersionTest {

    private final Policy policy = new Policy(null, "내 규칙");
    private final Instrument stockEtf = new Instrument("KRX", "360750", "TIGER 미국S&P500");
    private final Instrument bondEtf = new Instrument("KRX", "114260", "KODEX 국고채3년");

    @Test
    void 배분과_현금_합계가_10000이면_버전을_생성한다() {
        // given
        final PolicyVersion parent = PolicyVersion.create(policy, null, 1, 1, 500, true, 10_000, List.of());

        // when
        final PolicyVersion version = PolicyVersion.create(policy, parent, 2, 3, 300, false, 0, List.of(
                new AllocationDraft("  미국주식 ", stockEtf, 6000),
                new AllocationDraft("국내채권", bondEtf, 4000)
        ));

        // then
        assertThat(version.getParentVersion()).isSameAs(parent);
        assertThat(version.getVersionNo()).isEqualTo(2);
        assertThat(version.getReviewIntervalMonths()).isEqualTo(3);
        assertThat(version.getThresholdBps()).isEqualTo(300);
        assertThat(version.isAllowSell()).isFalse();
        assertThat(version.getAllocations())
                .extracting(PolicyAllocation::getAssetClass, PolicyAllocation::getInstrument, PolicyAllocation::getTargetBps)
                .containsExactly(
                        tuple("미국주식", stockEtf, 6000),
                        tuple("국내채권", bondEtf, 4000)
                );
        assertThat(version.getAllocations())
                .allSatisfy(allocation -> assertThat(allocation.getPolicyVersion()).isSameAs(version));
    }

    @Test
    void 현금을_포함해_합계가_10000이면_버전을_생성한다() {
        // when
        final PolicyVersion version = PolicyVersion.create(policy, null, 1, 1, 500, true, 1000, List.of(
                new AllocationDraft("미국주식", stockEtf, 5000),
                new AllocationDraft("국내채권", bondEtf, 4000)
        ));

        // then
        assertThat(version.getCashTargetBps()).isEqualTo(1000);
        assertThat(version.getAllocations()).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {9999, 10_001})
    void 배분과_현금_합계가_10000이_아니면_예외가_발생한다(final int totalBps) {
        // when & then
        assertThatThrownBy(() -> PolicyVersion.create(policy, null, 1, 1, 500, true, 0, List.of(
                new AllocationDraft("미국주식", stockEtf, 6000),
                new AllocationDraft("국내채권", bondEtf, totalBps - 6000)
        )))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.TARGET_SUM_INVALID.getCode());
    }

    @Test
    void 앞뒤_공백만_다른_자산군은_중복으로_판단한다() {
        // when & then
        assertThatThrownBy(() -> PolicyVersion.create(policy, null, 1, 1, 500, true, 0, List.of(
                new AllocationDraft("미국주식", stockEtf, 6000),
                new AllocationDraft(" 미국주식 ", bondEtf, 4000)
        )))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.ASSET_CLASS_DUPLICATED.getCode());
    }

    @Test
    void 같은_ETF를_두_자산군에_배분하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PolicyVersion.create(policy, null, 1, 1, 500, true, 0, List.of(
                new AllocationDraft("미국주식", stockEtf, 6000),
                new AllocationDraft("해외주식", stockEtf, 4000)
        )))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getErrorCode())
                .isEqualTo(PolicyErrorCode.INSTRUMENT_DUPLICATED.getCode());
    }
}
