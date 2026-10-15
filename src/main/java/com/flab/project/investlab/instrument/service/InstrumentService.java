package com.flab.project.investlab.instrument.service;

import com.flab.project.investlab.instrument.domain.Instrument;
import com.flab.project.investlab.instrument.repository.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(final InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional(readOnly = true)
    public List<Instrument> getAll() {
        return instrumentRepository.findAllByOrderByNameAsc();
    }
}
