package com.flab.project.investlab.instrument.service;

import com.flab.project.investlab.instrument.dto.InstrumentListResponse;
import com.flab.project.investlab.instrument.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    @Transactional(readOnly = true)
    public InstrumentListResponse getAll() {
        final List<InstrumentListResponse.Item> items = instrumentRepository.findAllByOrderByNameAsc().stream()
                .map(instrument -> new InstrumentListResponse.Item(
                        instrument.getId(),
                        instrument.getMarket().name(),
                        instrument.getSymbol(),
                        instrument.getName()
                ))
                .toList();
        return new InstrumentListResponse(items);
    }
}
