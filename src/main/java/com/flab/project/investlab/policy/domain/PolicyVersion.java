package com.flab.project.investlab.policy.domain;

import com.flab.project.investlab.instrument.domain.Instrument;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(name = "policy_versions")
public class PolicyVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "review_interval_months", nullable = false)
    private int reviewIntervalMonths;

    @Column(name = "threshold_bps", nullable = false)
    private int thresholdBps;

    @Column(name = "allow_sell", nullable = false)
    private boolean allowSell;

    @Column(name = "cash_target_bps", nullable = false)
    private int cashTargetBps;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "policyVersion", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<PolicyAllocation> allocations = new ArrayList<>();

    protected PolicyVersion() {
    }

    public PolicyVersion(
            final Policy policy,
            final int versionNo,
            final int reviewIntervalMonths,
            final int thresholdBps,
            final boolean allowSell,
            final int cashTargetBps
    ) {
        this.policy = policy;
        this.versionNo = versionNo;
        this.reviewIntervalMonths = reviewIntervalMonths;
        this.thresholdBps = thresholdBps;
        this.allowSell = allowSell;
        this.cashTargetBps = cashTargetBps;
    }

    public void addAllocation(final String assetClass, final Instrument instrument, final int targetBps) {
        allocations.add(new PolicyAllocation(this, assetClass, instrument, targetBps));
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public List<PolicyAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }
}
