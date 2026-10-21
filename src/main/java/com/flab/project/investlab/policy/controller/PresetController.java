package com.flab.project.investlab.policy.controller;

import com.flab.project.investlab.policy.dto.PresetDetailResponse;
import com.flab.project.investlab.policy.dto.PresetListResponse;
import com.flab.project.investlab.policy.service.PresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presets")
public class PresetController {

    private final PresetService presetService;

    @GetMapping
    public PresetListResponse getPresets() {
        return presetService.getPresets();
    }

    @GetMapping("/{presetId}")
    public PresetDetailResponse getPreset(@PathVariable final Long presetId) {
        return presetService.getPreset(presetId);
    }
}
