package com.legaldocanalyzer.utils;

import org.springframework.web.multipart.MultipartFile;

public class ValidationUtils {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "doc", "docx"};
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    public static void validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d bytes", MAX_FILE_SIZE)
            );
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String extension = FileUtils.getFileExtension(fileName);
        boolean validExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }

        // Check content type
        String contentType = file.getContentType();
        boolean validContentType = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                validContentType = true;
                break;
            }
        }

        if (!validContentType) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: PDF, DOC, DOCX"
            );
        }
    }

    public static void validateDocumentId(String documentId) throws IllegalArgumentException {
        if (documentId == null || documentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be empty");
        }

        // Add more validation rules as needed
        if (documentId.length() < 8) {
            throw new IllegalArgumentException("Invalid document ID format");
        }
    }

    public static void validateUserId(String userId) throws IllegalArgumentException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
    }

    public static void validatePaginationParams(int page, int size) throws IllegalArgumentException {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }
}