package com.flab.project.investlab.instrument.repository;

import com.flab.project.investlab.instrument.domain.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    List<Instrument> findAllByOrderByNameAsc();
}
