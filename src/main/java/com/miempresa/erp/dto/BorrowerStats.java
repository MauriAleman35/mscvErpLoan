package com.miempresa.erp.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class BorrowerStats {

    @Id
    private Integer id; // Campo ficticio para cumplir con JPA

    private Integer adressVerified;
    private Integer identityVerified;
    private Integer loanCount;
    private Integer latePaymentCount;
    private BigDecimal avgDaysLate;
    private BigDecimal totalPenalty;
    private BigDecimal paymentCompletionRatio;
    private Integer hasNoLatePayments;
    private Integer hasPenalty;
    private BigDecimal loansAlDiaRatio;
    private BigDecimal daysLatePerLoan;

    // Constructor sin argumentos requerido por JPA
    public BorrowerStats() {}

    // Constructor para el mapeo de resultados
    public BorrowerStats(
        Integer adressVerified,
        Integer identityVerified,
        Integer loanCount,
        Integer latePaymentCount,
        BigDecimal avgDaysLate,
        BigDecimal totalPenalty,
        BigDecimal paymentCompletionRatio,
        Integer hasNoLatePayments,
        Integer hasPenalty,
        BigDecimal loansAlDiaRatio,
        BigDecimal daysLatePerLoan
    ) {
        this.adressVerified = adressVerified;
        this.identityVerified = identityVerified;
        this.loanCount = loanCount;
        this.latePaymentCount = latePaymentCount;
        this.avgDaysLate = avgDaysLate;
        this.totalPenalty = totalPenalty;
        this.paymentCompletionRatio = paymentCompletionRatio;
        this.hasNoLatePayments = hasNoLatePayments;
        this.hasPenalty = hasPenalty;
        this.loansAlDiaRatio = loansAlDiaRatio;
        this.daysLatePerLoan = daysLatePerLoan;
    }

    // Getters y setters
    public Integer getAdressVerified() {
        return adressVerified;
    }

    public void setAdressVerified(Integer adressVerified) {
        this.adressVerified = adressVerified;
    }

    public Integer getIdentityVerified() {
        return identityVerified;
    }

    public void setIdentityVerified(Integer identityVerified) {
        this.identityVerified = identityVerified;
    }

    public Integer getLoanCount() {
        return loanCount;
    }

    public void setLoanCount(Integer loanCount) {
        this.loanCount = loanCount;
    }

    public Integer getLatePaymentCount() {
        return latePaymentCount;
    }

    public void setLatePaymentCount(Integer latePaymentCount) {
        this.latePaymentCount = latePaymentCount;
    }

    public BigDecimal getAvgDaysLate() {
        return avgDaysLate;
    }

    public void setAvgDaysLate(BigDecimal avgDaysLate) {
        this.avgDaysLate = avgDaysLate;
    }

    public BigDecimal getTotalPenalty() {
        return totalPenalty;
    }

    public void setTotalPenalty(BigDecimal totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    public BigDecimal getPaymentCompletionRatio() {
        return paymentCompletionRatio;
    }

    public void setPaymentCompletionRatio(BigDecimal paymentCompletionRatio) {
        this.paymentCompletionRatio = paymentCompletionRatio;
    }

    public Integer getHasNoLatePayments() {
        return hasNoLatePayments;
    }

    public void setHasNoLatePayments(Integer hasNoLatePayments) {
        this.hasNoLatePayments = hasNoLatePayments;
    }

    public Integer getHasPenalty() {
        return hasPenalty;
    }

    public void setHasPenalty(Integer hasPenalty) {
        this.hasPenalty = hasPenalty;
    }

    public BigDecimal getLoansAlDiaRatio() {
        return loansAlDiaRatio;
    }

    public void setLoansAlDiaRatio(BigDecimal loansAlDiaRatio) {
        this.loansAlDiaRatio = loansAlDiaRatio;
    }

    public BigDecimal getDaysLatePerLoan() {
        return daysLatePerLoan;
    }

    public void setDaysLatePerLoan(BigDecimal daysLatePerLoan) {
        this.daysLatePerLoan = daysLatePerLoan;
    }
}
