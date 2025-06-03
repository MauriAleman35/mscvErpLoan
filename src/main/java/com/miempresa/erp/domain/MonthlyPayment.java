package com.miempresa.erp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A MonthlyPayment.
 */
@Entity
@Table(name = "monthly_payment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MonthlyPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @NotNull
    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;

    @NotNull
    @Column(name = "borrow_verified", nullable = false)
    private Boolean borrowVerified;

    @NotNull
    @Column(name = "partner_verified", nullable = false)
    private Boolean partnerVerified;

    @NotNull
    @Column(name = "comprobant_file", nullable = false)
    private String comprobantFile;

    @NotNull
    @Column(name = "days_late", nullable = false)
    private Integer daysLate;

    @NotNull
    @Column(name = "penalty_amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal penaltyAmount;

    @NotNull
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "offer" }, allowSetters = true)
    private Loan loan;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MonthlyPayment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDueDate() {
        return this.dueDate;
    }

    public MonthlyPayment dueDate(Instant dueDate) {
        this.setDueDate(dueDate);
        return this;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getPaymentDate() {
        return this.paymentDate;
    }

    public MonthlyPayment paymentDate(Instant paymentDate) {
        this.setPaymentDate(paymentDate);
        return this;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Boolean getBorrowVerified() {
        return this.borrowVerified;
    }

    public MonthlyPayment borrowVerified(Boolean borrowVerified) {
        this.setBorrowVerified(borrowVerified);
        return this;
    }

    public void setBorrowVerified(Boolean borrowVerified) {
        this.borrowVerified = borrowVerified;
    }

    public Boolean getPartnerVerified() {
        return this.partnerVerified;
    }

    public MonthlyPayment partnerVerified(Boolean partnerVerified) {
        this.setPartnerVerified(partnerVerified);
        return this;
    }

    public void setPartnerVerified(Boolean partnerVerified) {
        this.partnerVerified = partnerVerified;
    }

    public String getComprobantFile() {
        return this.comprobantFile;
    }

    public MonthlyPayment comprobantFile(String comprobantFile) {
        this.setComprobantFile(comprobantFile);
        return this;
    }

    public void setComprobantFile(String comprobantFile) {
        this.comprobantFile = comprobantFile;
    }

    public Integer getDaysLate() {
        return this.daysLate;
    }

    public MonthlyPayment daysLate(Integer daysLate) {
        this.setDaysLate(daysLate);
        return this;
    }

    public void setDaysLate(Integer daysLate) {
        this.daysLate = daysLate;
    }

    public BigDecimal getPenaltyAmount() {
        return this.penaltyAmount;
    }

    public MonthlyPayment penaltyAmount(BigDecimal penaltyAmount) {
        this.setPenaltyAmount(penaltyAmount);
        return this;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public MonthlyPayment paymentStatus(String paymentStatus) {
        this.setPaymentStatus(paymentStatus);
        return this;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public MonthlyPayment loan(Loan loan) {
        this.setLoan(loan);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MonthlyPayment)) {
            return false;
        }
        return getId() != null && getId().equals(((MonthlyPayment) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MonthlyPayment{" +
            "id=" + getId() +
            ", dueDate='" + getDueDate() + "'" +
            ", paymentDate='" + getPaymentDate() + "'" +
            ", borrowVerified='" + getBorrowVerified() + "'" +
            ", partnerVerified='" + getPartnerVerified() + "'" +
            ", comprobantFile='" + getComprobantFile() + "'" +
            ", daysLate=" + getDaysLate() +
            ", penaltyAmount=" + getPenaltyAmount() +
            ", paymentStatus='" + getPaymentStatus() + "'" +
            "}";
    }
}
