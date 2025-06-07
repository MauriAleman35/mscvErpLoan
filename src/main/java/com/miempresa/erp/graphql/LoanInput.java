package com.miempresa.erp.graphql;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

public class LoanInput {

    private BigDecimal loanAmount;
    private java.sql.Timestamp startDate;
    private java.sql.Timestamp endDate;
    private String hashBlockchain;
    private String currentStatus;
    private Integer latePaymentCount;
    private Instant lastStatusUpdate;
    private Long offerId;

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public java.sql.Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(java.sql.Timestamp startDate) {
        this.startDate = startDate;
    }

    public java.sql.Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(java.sql.Timestamp endDate) {
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
