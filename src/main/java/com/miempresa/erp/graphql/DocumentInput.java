package com.miempresa.erp.graphql;

import java.time.Instant;
import org.springframework.web.multipart.MultipartFile;

public class DocumentInput {

    private String documentType; // Tipo de documento: ID_FRONT, ID_BACK, SELFIE, UTILITY_BILL
    private MultipartFile file; // Archivo a subir
    private String urlFile; // URL alternativa si ya tienes una
    private Instant uploadDate;
    private Boolean verified;
    private Long userId;

    // Getters y Setters
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
