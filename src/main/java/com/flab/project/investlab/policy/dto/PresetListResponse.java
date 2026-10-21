package com.flab.project.investlab.policy.dto;

import java.util.List;

public record PresetListResponse(
        List<Item> items
) {

    public record Item(
            Long id,
            String name
    ) {
    }
}
