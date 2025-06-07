package com.miempresa.erp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A Offer.
 */
@Entity
@Table(name = "offer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Offer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    // Este campo se mantendrá para compatibilidad con el código existente
    @Column(name = "partner_id", nullable = false, insertable = false, updatable = false)
    private Long partnerId;

    // Agregamos la relación con el usuario prestamista
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    @JsonIgnoreProperties(value = { "roles" }, allowSetters = true)
    private User partner;

    @Column(name = "interest", precision = 21, scale = 2, nullable = false)
    private BigDecimal interest;

    @Column(name = "loan_term", nullable = false)
    private Integer loanTerm;

    @Column(name = "monthly_payment", precision = 21, scale = 2, nullable = false)
    private BigDecimal monthlyPayment;

    @Column(name = "total_repayment_amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal totalRepaymentAmount;

    @Column(name = "status", nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private java.sql.Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitude")
    @JsonIgnoreProperties(value = { "borrower" }, allowSetters = true)
    private Solicitude solicitude;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Offer id(Long id) {
        this.setId(id);
        return this;
    }

    public User getPartner() {
        return this.partner;
    }

    public void setPartner(User partner) {
        this.partner = partner;
        if (partner != null) {
            this.partnerId = partner.getId();
        }
    }

    public Offer partner(User partner) {
        this.setPartner(partner);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartnerId() {
        return this.partnerId;
    }

    public Offer partnerId(Long partnerId) {
        this.setPartnerId(partnerId);
        return this;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public Offer interest(BigDecimal interest) {
        this.setInterest(interest);
        return this;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public Integer getLoanTerm() {
        return this.loanTerm;
    }

    public Offer loanTerm(Integer loanTerm) {
        this.setLoanTerm(loanTerm);
        return this;
    }

    public void setLoanTerm(Integer loanTerm) {
        this.loanTerm = loanTerm;
    }

    public BigDecimal getMonthlyPayment() {
        return this.monthlyPayment;
    }

    public Offer monthlyPayment(BigDecimal monthlyPayment) {
        this.setMonthlyPayment(monthlyPayment);
        return this;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public BigDecimal getTotalRepaymentAmount() {
        return this.totalRepaymentAmount;
    }

    public Offer totalRepaymentAmount(BigDecimal totalRepaymentAmount) {
        this.setTotalRepaymentAmount(totalRepaymentAmount);
        return this;
    }

    public void setTotalRepaymentAmount(BigDecimal totalRepaymentAmount) {
        this.totalRepaymentAmount = totalRepaymentAmount;
    }

    public String getStatus() {
        return this.status;
    }

    public Offer status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.sql.Timestamp getCreatedAt() {
        return this.createdAt;
    }

    public Offer createdAt(java.sql.Timestamp createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Solicitude getSolicitude() {
        return this.solicitude;
    }

    public void setSolicitude(Solicitude solicitude) {
        this.solicitude = solicitude;
    }

    public Offer solicitude(Solicitude solicitude) {
        this.setSolicitude(solicitude);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Offer)) {
            return false;
        }
        return getId() != null && getId().equals(((Offer) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Offer{" +
            "id=" + getId() +
            ", partnerId=" + getPartnerId() +
            ", interest=" + getInterest() +
            ", loanTerm=" + getLoanTerm() +
            ", monthlyPayment=" + getMonthlyPayment() +
            ", totalRepaymentAmount=" + getTotalRepaymentAmount() +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
