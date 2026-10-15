package com.flab.project.investlab.instrument.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String market;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    protected Instrument() {
    }

    public Instrument(final String market, final String symbol, final String name) {
        this.market = market;
        this.symbol = symbol;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getMarket() {
        return market;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}
