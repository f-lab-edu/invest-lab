package com.flab.project.investlab.policy.domain;

import com.flab.project.investlab.instrument.domain.Instrument;

public record AllocationDraft(
        String assetClass,
        Instrument instrument,
        int targetBps
) {
}
