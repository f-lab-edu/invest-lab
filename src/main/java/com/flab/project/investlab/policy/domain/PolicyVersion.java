package com.flab.project.investlab.policy.domain;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "policy_versions")
public class PolicyVersion {

    private static final int TOTAL_TARGET_BPS = 10_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_version_id")
    private PolicyVersion parentVersion;

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

    @OneToMany(mappedBy = "policyVersion", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<PolicyAllocation> allocations = new ArrayList<>();

    protected PolicyVersion() {
    }

    private PolicyVersion(
            final Policy policy,
            final PolicyVersion parentVersion,
            final int versionNo,
            final int reviewIntervalMonths,
            final int thresholdBps,
            final boolean allowSell,
            final int cashTargetBps
    ) {
        this.policy = policy;
        this.parentVersion = parentVersion;
        this.versionNo = versionNo;
        this.reviewIntervalMonths = reviewIntervalMonths;
        this.thresholdBps = thresholdBps;
        this.allowSell = allowSell;
        this.cashTargetBps = cashTargetBps;
    }

    public static PolicyVersion create(
            final Policy policy,
            final PolicyVersion parentVersion,
            final int versionNo,
            final int reviewIntervalMonths,
            final int thresholdBps,
            final boolean allowSell,
            final int cashTargetBps,
            final List<AllocationDraft> drafts
    ) {
        validateNoDuplicateAssetClass(drafts);
        validateNoDuplicateInstrument(drafts);
        validateTargetSum(drafts, cashTargetBps);

        final PolicyVersion version = new PolicyVersion(
                policy,
                parentVersion,
                versionNo,
                reviewIntervalMonths,
                thresholdBps,
                allowSell,
                cashTargetBps
        );
        for (final AllocationDraft draft : drafts) {
            version.addAllocation(draft.assetClass().strip(), draft.instrument(), draft.targetBps());
        }
        return version;
    }

    private static void validateNoDuplicateAssetClass(final List<AllocationDraft> drafts) {
        final Set<String> assetClasses = new HashSet<>();
        for (final AllocationDraft draft : drafts) {
            if (!assetClasses.add(draft.assetClass().strip())) {
                throw new PolicyException(PolicyErrorCode.ASSET_CLASS_DUPLICATED);
            }
        }
    }

    private static void validateNoDuplicateInstrument(final List<AllocationDraft> drafts) {
        final Set<Instrument> instruments = new HashSet<>();
        for (final AllocationDraft draft : drafts) {
            if (!instruments.add(draft.instrument())) {
                throw new PolicyException(PolicyErrorCode.INSTRUMENT_DUPLICATED);
            }
        }
    }

    private static void validateTargetSum(final List<AllocationDraft> drafts, final int cashTargetBps) {
        int totalBps = cashTargetBps;
        for (final AllocationDraft draft : drafts) {
            totalBps += draft.targetBps();
        }
        if (totalBps != TOTAL_TARGET_BPS) {
            throw new PolicyException(PolicyErrorCode.TARGET_SUM_INVALID);
        }
    }

    private void addAllocation(final String assetClass, final Instrument instrument, final int targetBps) {
        allocations.add(new PolicyAllocation(this, assetClass, instrument, targetBps));
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public PolicyVersion getParentVersion() {
        return parentVersion;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public int getReviewIntervalMonths() {
        return reviewIntervalMonths;
    }

    public int getThresholdBps() {
        return thresholdBps;
    }

    public boolean isAllowSell() {
        return allowSell;
    }

    public int getCashTargetBps() {
        return cashTargetBps;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PolicyAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }
}
