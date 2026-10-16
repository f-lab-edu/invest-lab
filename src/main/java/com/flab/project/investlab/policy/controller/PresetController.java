package com.flab.project.investlab.policy.controller;

import com.flab.project.investlab.policy.dto.PresetDetailResponse;
import com.flab.project.investlab.policy.dto.PresetSummaryResponse;
import com.flab.project.investlab.policy.service.PresetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/presets")
public class PresetController {

    private final PresetService presetService;

    public PresetController(final PresetService presetService) {
        this.presetService = presetService;
    }

    @GetMapping
    public List<PresetSummaryResponse> getPresets() {
        return presetService.getPresets();
    }

    @GetMapping("/{presetId}")
    public PresetDetailResponse getPreset(@PathVariable final Long presetId) {
        return presetService.getPreset(presetId);
    }
}
