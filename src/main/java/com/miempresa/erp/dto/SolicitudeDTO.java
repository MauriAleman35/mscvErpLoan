package com.miempresa.erp.dto;

import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.domain.User;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class SolicitudeDTO {

    private Long id;
    private BigDecimal loanAmount;
    private String status;
    private OffsetDateTime createdAt; // Usando OffsetDateTime en lugar de Timestamp
    private User borrower;

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }
}
