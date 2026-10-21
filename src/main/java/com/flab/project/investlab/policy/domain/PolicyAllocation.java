package com.flab.project.investlab.policy.domain;

import com.flab.project.investlab.instrument.domain.Instrument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "policy_allocations")
public class PolicyAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_version_id", nullable = false)
    private PolicyVersion policyVersion;

    @Column(name = "asset_class", nullable = false, length = 30)
    private String assetClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "target_bps", nullable = false)
    private int targetBps;

    protected PolicyAllocation() {
    }

    PolicyAllocation(
            final PolicyVersion policyVersion,
            final String assetClass,
            final Instrument instrument,
            final int targetBps
    ) {
        this.policyVersion = policyVersion;
        this.assetClass = assetClass;
        this.instrument = instrument;
        this.targetBps = targetBps;
    }
}
