package com.legaldocanalyzer.controller;

import com.legaldocanalyzer.dto.AnalysisReportDTO;
import com.legaldocanalyzer.dto.DocumentUploadResponse;
import com.legaldocanalyzer.dto.ErrorResponse;
import com.legaldocanalyzer.dto.RecommendationDTO;
import com.legaldocanalyzer.service.DocumentAnalyzerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/legal-documents")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LegalDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(LegalDocumentController.class);

    @Autowired
    private DocumentAnalyzerService analyzerService;

     // Upload and analyze legal document

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest request) {

        try {
            String userId = getUserIdFromAuth(authentication);
            logger.info("Document upload request from user: {}", userId);

            DocumentUploadResponse response = analyzerService.analyzeDocument(file, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Document upload failed", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    "UPLOAD_FAILED",
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get analysis report for a document
     */
    @GetMapping("/report/{docId}")
    public ResponseEntity<?> getAnalysisReport(
            @PathVariable("docId") String docId,
            Authentication authentication,
            HttpServletRequest request) {

        try {
            String userId = getUserIdFromAuth(authentication);
            logger.info("Analysis report request for document: {} by user: {}", docId, userId);

            AnalysisReportDTO report = analyzerService.getAnalysisReport(docId);
            return ResponseEntity.ok(report);

        } catch (Exception e) {
            logger.error("Failed to retrieve analysis report for document: {}", docId, e);
            ErrorResponse errorResponse = new ErrorResponse(
                    "REPORT_RETRIEVAL_FAILED",
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.value(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get recommendations for a document
     */
    @GetMapping("/recommendations/{docId}")
    public ResponseEntity<?> getRecommendations(
            @PathVariable("docId") String docId,
            Authentication authentication,
            HttpServletRequest request) {

        try {
            String userId = getUserIdFromAuth(authentication);
            logger.info("Recommendations request for document: {} by user: {}", docId, userId);

            List<RecommendationDTO> recommendations = analyzerService.getRecommendations(docId);
            return ResponseEntity.ok(Map.of("recommendations", recommendations));

        } catch (Exception e) {
            logger.error("Failed to retrieve recommendations for document: {}", docId, e);
            ErrorResponse errorResponse = new ErrorResponse(
                    "RECOMMENDATIONS_RETRIEVAL_FAILED",
                    e.getMessage(),
                    HttpStatus.NOT_FOUND.value(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get processing status of a document
     */
    @GetMapping("/status/{docId}")
    public ResponseEntity<?> getProcessingStatus(
            @PathVariable("docId") String docId,
            Authentication authentication) {

        String userId = getUserIdFromAuth(authentication);
        logger.info("Status request for document: {} by user: {}", docId, userId);

        Map<String, Object> status = analyzerService.getProcessingStatus(docId);

        if (status.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Get user's documents
     */
    @GetMapping("/user/documents")
    public ResponseEntity<Map<String, Object>> getUserDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userId = getUserIdFromAuth(authentication);
        logger.info("User documents request from user: {}, page: {}, size: {}", userId, page, size);

        List<Map<String, Object>> documents = analyzerService.getUserDocuments(userId, page, size);

        Map<String, Object> response = Map.of(
                "documents", documents,
                "page", page,
                "size", size,
                "total", documents.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a document
     */
    @DeleteMapping("/{docId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable("docId") String docId,
            Authentication authentication,
            HttpServletRequest request) {

        try {
            String userId = getUserIdFromAuth(authentication);
            logger.info("Delete request for document: {} by user: {}", docId, userId);

            analyzerService.deleteDocument(docId, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Document deleted successfully",
                    "documentId", docId
            ));

        } catch (Exception e) {
            logger.error("Failed to delete document: {}", docId, e);
            ErrorResponse errorResponse = new ErrorResponse(
                    "DELETE_FAILED",
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Reanalyze a document
     */
    @PostMapping("/{docId}/reanalyze")
    public ResponseEntity<?> reanalyzeDocument(
            @PathVariable("docId") String docId,
            Authentication authentication,
            HttpServletRequest request) {

        try {
            String userId = getUserIdFromAuth(authentication);
            logger.info("Reanalysis request for document: {} by user: {}", docId, userId);

            analyzerService.reanalyzeDocument(docId);

            return ResponseEntity.accepted().body(Map.of(
                    "message", "Document reanalysis started",
                    "documentId", docId,
                    "status", "PROCESSING"
            ));

        } catch (Exception e) {
            logger.error("Failed to reanalyze document: {}", docId, e);
            ErrorResponse errorResponse = new ErrorResponse(
                    "REANALYSIS_FAILED",
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value(),
                    request.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "Legal Document Analyzer",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(health);
    }

    // Helper method to extract user ID from authentication
    private String getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous"; // For testing purposes - should be removed in production
    }
}