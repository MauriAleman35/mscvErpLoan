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
 * A Loan.
 */
@Entity
@Table(name = "loan")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "loan_amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "hash_blockchain", nullable = false)
    private String hashBlockchain;

    @Column(name = "current_status", nullable = false)
    private String currentStatus;

    @Column(name = "late_payment_count", nullable = false)
    private Integer latePaymentCount;

    @Column(name = "last_status_update", nullable = false)
    private Instant lastStatusUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offer")
    @JsonIgnoreProperties(value = { "solicitude" }, allowSetters = true)
    private Offer offer;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Loan id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getLoanAmount() {
        return this.loanAmount;
    }

    public Loan loanAmount(BigDecimal loanAmount) {
        this.setLoanAmount(loanAmount);
        return this;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public Loan startDate(Instant startDate) {
        this.setStartDate(startDate);
        return this;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public Loan endDate(Instant endDate) {
        this.setEndDate(endDate);
        return this;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getHashBlockchain() {
        return this.hashBlockchain;
    }

    public Loan hashBlockchain(String hashBlockchain) {
        this.setHashBlockchain(hashBlockchain);
        return this;
    }

    public void setHashBlockchain(String hashBlockchain) {
        this.hashBlockchain = hashBlockchain;
    }

    public String getCurrentStatus() {
        return this.currentStatus;
    }

    public Loan currentStatus(String currentStatus) {
        this.setCurrentStatus(currentStatus);
        return this;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getLatePaymentCount() {
        return this.latePaymentCount;
    }

    public Loan latePaymentCount(Integer latePaymentCount) {
        this.setLatePaymentCount(latePaymentCount);
        return this;
    }

    public void setLatePaymentCount(Integer latePaymentCount) {
        this.latePaymentCount = latePaymentCount;
    }

    public Instant getLastStatusUpdate() {
        return this.lastStatusUpdate;
    }

    public Loan lastStatusUpdate(Instant lastStatusUpdate) {
        this.setLastStatusUpdate(lastStatusUpdate);
        return this;
    }

    public void setLastStatusUpdate(Instant lastStatusUpdate) {
        this.lastStatusUpdate = lastStatusUpdate;
    }

    public Offer getOffer() {
        return this.offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Loan offer(Offer offer) {
        this.setOffer(offer);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Loan)) {
            return false;
        }
        return getId() != null && getId().equals(((Loan) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Loan{" +
            "id=" + getId() +
            ", loanAmount=" + getLoanAmount() +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", hashBlockchain='" + getHashBlockchain() + "'" +
            ", currentStatus='" + getCurrentStatus() + "'" +
            ", latePaymentCount=" + getLatePaymentCount() +
            ", lastStatusUpdate='" + getLastStatusUpdate() + "'" +
            "}";
    }
}
