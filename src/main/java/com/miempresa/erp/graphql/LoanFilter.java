package com.miempresa.erp.graphql;

import java.math.BigDecimal;
import java.time.Instant;

public class LoanFilter {

    private Long id;
    private String currentStatus;
    private Instant startDateFrom;
    private Instant startDateTo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Instant getStartDateFrom() {
        return startDateFrom;
    }

    public void setStartDateFrom(Instant startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public Instant getStartDateTo() {
        return startDateTo;
    }

    public void setStartDateTo(Instant startDateTo) {
        this.startDateTo = startDateTo;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
}
