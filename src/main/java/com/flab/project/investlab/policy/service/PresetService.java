package com.flab.project.investlab.policy.service;

import com.flab.project.investlab.policy.domain.PolicyVersion;
import com.flab.project.investlab.policy.dto.PresetDetailResponse;
import com.flab.project.investlab.policy.dto.PresetListResponse;
import com.flab.project.investlab.policy.exception.PolicyErrorCode;
import com.flab.project.investlab.policy.exception.PolicyException;
import com.flab.project.investlab.policy.repository.PolicyRepository;
import com.flab.project.investlab.policy.repository.PolicyVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresetService {

    private final PolicyRepository policyRepository;
    private final PolicyVersionRepository policyVersionRepository;

    @Transactional(readOnly = true)
    public PresetListResponse getPresets() {
        final List<PresetListResponse.Item> items = policyRepository.findAllByMemberIsNullOrderByIdAsc().stream()
                .map(policy -> new PresetListResponse.Item(policy.getId(), policy.getName()))
                .toList();
        return new PresetListResponse(items);
    }

    @Transactional(readOnly = true)
    public PresetDetailResponse getPreset(final Long presetId) {
        final PolicyVersion version = policyVersionRepository.findLatestPresetVersion(presetId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.PRESET_NOT_FOUND));
        return PresetDetailResponse.from(version);
    }
}
