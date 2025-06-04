package com.miempresa.erp.graphql;

import java.math.BigDecimal;
import java.time.Instant;

public class LoanInput {

    private BigDecimal loanAmount;
    private Instant startDate;
    private Instant endDate;
    private String hashBlockchain;
    private String currentStatus;
    private Integer latePaymentCount;
    private Instant lastStatusUpdate;
    private Long offerId;

    // Getters and setters
    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getHashBlockchain() {
        return hashBlockchain;
    }

    public void setHashBlockchain(String hashBlockchain) {
        this.hashBlockchain = hashBlockchain;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getLatePaymentCount() {
        return latePaymentCount;
    }

    public void setLatePaymentCount(Integer latePaymentCount) {
        this.latePaymentCount = latePaymentCount;
    }

    public Instant getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public void setLastStatusUpdate(Instant lastStatusUpdate) {
        this.lastStatusUpdate = lastStatusUpdate;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }
}
