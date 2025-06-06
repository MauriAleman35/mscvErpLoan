package com.miempresa.erp.graphql;

import java.math.BigDecimal;

public class OfferInput {

    private Long partnerId;
    private BigDecimal interest;
    private Integer loanTerm;
    private BigDecimal monthlyPayment;
    private BigDecimal totalRepaymentAmount;
    private String status;
    private Long solicitudeId;

    // Getters and setters
    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public Integer getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(Integer loanTerm) {
        this.loanTerm = loanTerm;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public BigDecimal getTotalRepaymentAmount() {
        return totalRepaymentAmount;
    }

    public void setTotalRepaymentAmount(BigDecimal totalRepaymentAmount) {
        this.totalRepaymentAmount = totalRepaymentAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSolicitudeId() {
        return solicitudeId;
    }

    public void setSolicitudeId(Long solicitudeId) {
        this.solicitudeId = solicitudeId;
    }
}
