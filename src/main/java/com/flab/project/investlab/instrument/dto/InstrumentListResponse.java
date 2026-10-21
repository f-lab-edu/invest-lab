package com.flab.project.investlab.instrument.dto;

import java.util.List;

public record InstrumentListResponse(
        List<Item> items
) {

    public record Item(
            Long id,
            String market,
            String symbol,
            String name
    ) {
    }
}
