package com.miempresa.erp.graphql;

import java.time.Instant;

public class PaymentInput {

    private Long monthlyPaymentId;
    private String comprobantFile;
    private Instant paymentDate;

    public String getComprobantFile() {
        return comprobantFile;
    }

    public void setComprobantFile(String comprobantFile) {
        this.comprobantFile = comprobantFile;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getMonthlyPaymentId() {
        return monthlyPaymentId;
    }

    public void setMonthlyPaymentId(Long monthlyPaymentId) {
        this.monthlyPaymentId = monthlyPaymentId;
    }
}
