package com.legaldocanalyzer.dto;

import com.legaldocanalyzer.model.DocumentStatus;
import java.time.LocalDateTime;

public class DocumentUploadResponse {
    private String documentId;
    private String filename;
    private DocumentStatus status;
    private String message;
    private LocalDateTime uploadTime;

    public DocumentUploadResponse() {}

    public DocumentUploadResponse(String documentId, String filename, DocumentStatus status, String message) {
        this.documentId = documentId;
        this.filename = filename;
        this.status = status;
        this.message = message;
        this.uploadTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}
