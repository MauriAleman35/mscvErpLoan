package com.miempresa.erp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A User.
 */
@Entity
@Table(name = "jhi_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotNull
    @Column(name = "ci", nullable = false)
    private String ci;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Column(name = "score", nullable = false)
    private Integer score;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    @NotNull
    @Column(name = "user_type", nullable = false)
    private String userType;

    @NotNull
    @Column(name = "address_verified", nullable = false)
    private Boolean addressVerified;

    @NotNull
    @Column(name = "identity_verified", nullable = false)
    private Boolean identityVerified;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_jhi_user__roles",
        joinColumns = @JoinColumn(name = "jhi_user_id"),
        inverseJoinColumns = @JoinColumn(name = "roles_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "users" }, allowSetters = true)
    private Set<Role> roles = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public User id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public User name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return this.lastName;
    }

    public User lastName(String lastName) {
        this.setLastName(lastName);
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public User email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public User phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCi() {
        return this.ci;
    }

    public User ci(String ci) {
        this.setCi(ci);
        return this;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getPassword() {
        return this.password;
    }

    public User password(String password) {
        this.setPassword(password);
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getScore() {
        return this.score;
    }

    public User score(Integer score) {
        this.setScore(score);
        return this;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return this.status;
    }

    public User status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserType() {
        return this.userType;
    }

    public User userType(String userType) {
        this.setUserType(userType);
        return this;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Boolean getAddressVerified() {
        return this.addressVerified;
    }

    public User addressVerified(Boolean addressVerified) {
        this.setAddressVerified(addressVerified);
        return this;
    }

    public void setAddressVerified(Boolean addressVerified) {
        this.addressVerified = addressVerified;
    }

    public Boolean getIdentityVerified() {
        return this.identityVerified;
    }

    public User identityVerified(Boolean identityVerified) {
        this.setIdentityVerified(identityVerified);
        return this;
    }

    public void setIdentityVerified(Boolean identityVerified) {
        this.identityVerified = identityVerified;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public User roles(Set<Role> roles) {
        this.setRoles(roles);
        return this;
    }

    public User addRoles(Role role) {
        this.roles.add(role);
        return this;
    }

    public User removeRoles(Role role) {
        this.roles.remove(role);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return getId() != null && getId().equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "User{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", email='" + getEmail() + "'" +
            ", phone='" + getPhone() + "'" +
            ", ci='" + getCi() + "'" +
            ", password='" + getPassword() + "'" +
            ", score=" + getScore() +
            ", status='" + getStatus() + "'" +
            ", userType='" + getUserType() + "'" +
            ", addressVerified='" + getAddressVerified() + "'" +
            ", identityVerified='" + getIdentityVerified() + "'" +
            "}";
    }
}
