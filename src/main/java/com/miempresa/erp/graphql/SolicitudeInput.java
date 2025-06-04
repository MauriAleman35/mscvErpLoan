package com.miempresa.erp.graphql;

import java.math.BigDecimal;

public class SolicitudeInput {

    private BigDecimal loanAmount;
    private String status;
    private Long borrowerId;

    // Getters and setters
    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }
}
