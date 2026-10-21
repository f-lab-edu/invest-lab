package com.flab.project.investlab.instrument.controller;

import com.flab.project.investlab.instrument.dto.InstrumentListResponse;
import com.flab.project.investlab.instrument.service.InstrumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(final InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public InstrumentListResponse getAll() {
        return instrumentService.getAll();
    }
}
