package com.flab.project.investlab.instrument.dto;

import com.flab.project.investlab.instrument.domain.Instrument;

public record InstrumentResponse(
        Long id,
        String market,
        String symbol,
        String name
) {

    public static InstrumentResponse from(final Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getMarket(),
                instrument.getSymbol(),
                instrument.getName()
        );
    }
}
