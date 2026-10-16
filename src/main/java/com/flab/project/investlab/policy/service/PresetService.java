package com.flab.project.investlab.policy.service;

import com.flab.project.investlab.policy.domain.PolicyVersion;
import com.flab.project.investlab.policy.dto.PresetDetailResponse;
import com.flab.project.investlab.policy.dto.PresetSummaryResponse;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
import com.flab.project.investlab.policy.repository.PolicyRepository;
import com.flab.project.investlab.policy.repository.PolicyVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PresetService {

    private final PolicyRepository policyRepository;
    private final PolicyVersionRepository policyVersionRepository;

    public PresetService(final PolicyRepository policyRepository,
                         final PolicyVersionRepository policyVersionRepository) {
        this.policyRepository = policyRepository;
        this.policyVersionRepository = policyVersionRepository;
    }

    @Transactional(readOnly = true)
    public List<PresetSummaryResponse> getPresets() {
        return policyRepository.findAllByMemberIsNullOrderByIdAsc().stream()
                .map(PresetSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PresetDetailResponse getPreset(final Long presetId) {
        final PolicyVersion version = policyVersionRepository.findLatestPresetVersion(presetId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.PRESET_NOT_FOUND));
        return PresetDetailResponse.from(version);
    }
}
