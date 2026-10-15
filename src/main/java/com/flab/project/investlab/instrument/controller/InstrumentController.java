package com.flab.project.investlab.instrument.controller;

import com.flab.project.investlab.instrument.dto.InstrumentResponse;
import com.flab.project.investlab.instrument.service.InstrumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(final InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public List<InstrumentResponse> getAll() {
        return instrumentService.getAll().stream()
                .map(InstrumentResponse::from)
                .toList();
    }
}
