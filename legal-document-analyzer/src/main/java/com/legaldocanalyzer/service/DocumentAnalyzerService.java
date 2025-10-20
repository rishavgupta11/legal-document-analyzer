package com.legaldocanalyzer.service;

import com.legaldocanalyzer.dto.AnalysisReportDTO;
import com.legaldocanalyzer.dto.DocumentUploadResponse;
import com.legaldocanalyzer.dto.RecommendationDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface DocumentAnalyzerService {

    /**
     * Analyze uploaded document
     */
    DocumentUploadResponse analyzeDocument(MultipartFile file, String userId) throws Exception;

    /**
     * Get analysis report by document ID
     */
    AnalysisReportDTO getAnalysisReport(String documentId) throws Exception;

    /**
     * Get recommendations for a document
     */
    List<RecommendationDTO> getRecommendations(String documentId) throws Exception;

    /**
     * Get processing status of a document
     */
    Map<String, Object> getProcessingStatus(String documentId);

    /**
     * Get user's documents
     */
    List<Map<String, Object>> getUserDocuments(String userId, int page, int size);

    /**
     * Delete document and analysis results
     */
    void deleteDocument(String documentId, String userId) throws Exception;

    /**
     * Reanalyze existing document
     */
    void reanalyzeDocument(String documentId) throws Exception;
}
