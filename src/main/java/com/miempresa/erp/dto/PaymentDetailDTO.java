package com.miempresa.erp.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class PaymentDetailDTO implements Serializable {

    private Long id;
    private Instant dueDate;
    private Instant paymentDate;
    private String paymentStatus;
    private String comprobantFile;
    private BigDecimal penaltyAmount;
    private BigDecimal expectedPayment;
    private Integer cuotaNumber;
    private Integer totalCuotas;

    // Constructor vacío
    public PaymentDetailDTO() {}

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getComprobantFile() {
        return comprobantFile;
    }

    public void setComprobantFile(String comprobantFile) {
        this.comprobantFile = comprobantFile;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public BigDecimal getExpectedPayment() {
        return expectedPayment;
    }

    public void setExpectedPayment(BigDecimal expectedPayment) {
        this.expectedPayment = expectedPayment;
    }

    public Integer getCuotaNumber() {
        return cuotaNumber;
    }

    public void setCuotaNumber(Integer cuotaNumber) {
        this.cuotaNumber = cuotaNumber;
    }

    public Integer getTotalCuotas() {
        return totalCuotas;
    }

    public void setTotalCuotas(Integer totalCuotas) {
        this.totalCuotas = totalCuotas;
    }
}
