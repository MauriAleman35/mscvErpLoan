package com.miempresa.erp.graphql;

public class OfferFilter {

    private Long id;
    private String status;
    private Integer partnerId;
    private Long solicitudeId;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Long getSolicitudeId() {
        return solicitudeId;
    }

    public void setSolicitudeId(Long solicitudeId) {
        this.solicitudeId = solicitudeId;
    }
}
