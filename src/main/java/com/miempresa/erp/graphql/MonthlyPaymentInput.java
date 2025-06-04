package com.miempresa.erp.graphql;

import java.math.BigDecimal;
import java.time.Instant;

public class MonthlyPaymentInput {

    private Instant dueDate;
    private Instant paymentDate;
    private Boolean borrowVerified;
    private Boolean partnerVerified;
    private String comprobantFile;
    private Integer daysLate;
    private BigDecimal penaltyAmount;
    private String paymentStatus;
    private Long loanId;

    // Getters and setters
    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Boolean getBorrowVerified() {
        return borrowVerified;
    }

    public void setBorrowVerified(Boolean borrowVerified) {
        this.borrowVerified = borrowVerified;
    }

    public Boolean getPartnerVerified() {
        return partnerVerified;
    }

    public void setPartnerVerified(Boolean partnerVerified) {
        this.partnerVerified = partnerVerified;
    }

    public String getComprobantFile() {
        return comprobantFile;
    }

    public void setComprobantFile(String comprobantFile) {
        this.comprobantFile = comprobantFile;
    }

    public Integer getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(Integer daysLate) {
        this.daysLate = daysLate;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
}
