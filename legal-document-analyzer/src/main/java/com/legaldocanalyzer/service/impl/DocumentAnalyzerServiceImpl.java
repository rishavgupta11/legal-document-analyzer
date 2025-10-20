package com.legaldocanalyzer.service.impl;

import com.legaldocanalyzer.dto.*;
import com.legaldocanalyzer.model.*;
import com.legaldocanalyzer.repository.*;
import com.legaldocanalyzer.service.DocumentAnalyzerService;
import com.legaldocanalyzer.service.FileStorageService;
import com.legaldocanalyzer.service.TextExtractionService;
import com.legaldocanalyzer.service.RiskAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentAnalyzerServiceImpl implements DocumentAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalyzerServiceImpl.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private RiskClauseRepository riskClauseRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TextExtractionService textExtractionService;

    @Autowired
    private RiskAnalysisService riskAnalysisService;

    @Override
    public DocumentUploadResponse analyzeDocument(MultipartFile file, String userId) throws Exception {
        logger.info("Starting document analysis for user: {}", userId);

        // Validate file
        validateFile(file);

        // Store file
        String filePath = fileStorageService.storeFile(file, userId);

        // Create document entity
        Document document = new Document(
                generateUniqueFilename(file.getOriginalFilename()),
                file.getOriginalFilename(),
                filePath,
                file.getSize(),
                file.getContentType(),
                userId
        );
        document.setStatus(DocumentStatus.PROCESSING);

        // Save document
        document = documentRepository.save(document);

        try {
            // Extract text from document
            String extractedText = textExtractionService.extractText(filePath, file.getContentType());

            // Perform risk analysis
            Map<String, Object> analysisResults = riskAnalysisService.analyzeText(extractedText);

            // Create and save analysis result
            AnalysisResult analysisResult = createAnalysisResult(document, analysisResults, extractedText);
            analysisResultRepository.save(analysisResult);

            // Update document status
            document.setStatus(DocumentStatus.ANALYZED);
            documentRepository.save(document);

            logger.info("Document analysis completed successfully for document: {}", document.getId());

            return new DocumentUploadResponse(
                    document.getId(),
                    document.getOriginalFilename(),
                    DocumentStatus.ANALYZED,
                    "Document analyzed successfully"
            );

        } catch (Exception e) {
            logger.error("Analysis failed for document: {}", document.getId(), e);

            // Update document status to failed
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);

            throw new Exception("Document analysis failed: " + e.getMessage());
        }
    }

    @Override
    public AnalysisReportDTO getAnalysisReport(String documentId) throws Exception {
        logger.info("Retrieving analysis report for document: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found: " + documentId));

        AnalysisResult analysisResult = analysisResultRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new Exception("Analysis result not found for document: " + documentId));

        // Convert to DTO
        return convertToAnalysisReportDTO(document, analysisResult);
    }

    @Override
    public List<RecommendationDTO> getRecommendations(String documentId) throws Exception {
        logger.info("Retrieving recommendations for document: {}", documentId);

        List<Recommendation> recommendations = recommendationRepository.findByDocumentId(documentId);

        return recommendations.stream()
                .map(this::convertToRecommendationDTO)
                .sorted(Comparator.comparing(RecommendationDTO::getPriority).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProcessingStatus(String documentId) {
        Optional<Document> documentOpt = documentRepository.findById(documentId);

        if (documentOpt.isEmpty()) {
            return Map.of("error", "Document not found");
        }

        Document document = documentOpt.get();
        Map<String, Object> status = new HashMap<>();
        status.put("documentId", documentId);
        status.put("filename", document.getOriginalFilename());
        status.put("status", document.getStatus());
        status.put("uploadTime", document.getUploadTime());
        status.put("lastUpdated", document.getUpdatedTime());

        // Add progress information based on status
        switch (document.getStatus()) {
            case UPLOADED:
                status.put("progress", 10);
                status.put("message", "Document uploaded, waiting for processing");
                break;
            case PROCESSING:
                status.put("progress", 50);
                status.put("message", "Analyzing document content");
                break;
            case ANALYZED:
                status.put("progress", 100);
                status.put("message", "Analysis completed successfully");

                // Add analysis summary
                analysisResultRepository.findByDocumentId(documentId)
                        .ifPresent(result -> {
                            status.put("riskScore", result.getRiskScore());
                            status.put("overallRiskLevel", result.getOverallRiskLevel());
                            status.put("totalClauses", result.getTotalClauses());
                            status.put("riskyClauses", result.getRiskyClauses());
                        });
                break;
            case FAILED:
                status.put("progress", 0);
                status.put("message", "Document analysis failed");
                break;
            default:
                status.put("progress", 0);
                status.put("message", "Unknown status");
        }

        return status;
    }

    @Override
    public List<Map<String, Object>> getUserDocuments(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentRepository.findByUserIdOrderByUploadTimeDesc(userId, pageable);

        return documents.getContent().stream()
                .map(this::convertDocumentToMap)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(String documentId, String userId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found: " + documentId));

        // Verify ownership
        if (!document.getUserId().equals(userId)) {
            throw new Exception("Unauthorized access to document");
        }

        // Delete file from storage
        fileStorageService.deleteFile(document.getFilePath());

        // Delete from database (cascade will handle related entities)
        documentRepository.delete(document);

        logger.info("Document deleted successfully: {}", documentId);
    }

    @Override
    public void reanalyzeDocument(String documentId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found: " + documentId));

        // Delete existing analysis result
        analysisResultRepository.findByDocumentId(documentId)
                .ifPresent(analysisResultRepository::delete);

        // Set status to processing
        document.setStatus(DocumentStatus.PROCESSING);
        documentRepository.save(document);

        // Re-analyze the document
        try {
            String extractedText = textExtractionService.extractText(document.getFilePath(), document.getContentType());
            Map<String, Object> analysisResults = riskAnalysisService.analyzeText(extractedText);

            AnalysisResult analysisResult = createAnalysisResult(document, analysisResults, extractedText);
            analysisResultRepository.save(analysisResult);

            document.setStatus(DocumentStatus.ANALYZED);
            documentRepository.save(document);

        } catch (Exception e) {
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
            throw e;
        }
    }

    // Private helper methods
    private void validateFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("File cannot be empty");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        if (!allowedTypes.contains(contentType)) {
            throw new Exception("Invalid file type. Only PDF, DOC, DOCX files are allowed");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new Exception("File size exceeds maximum limit of 10MB");
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private AnalysisResult createAnalysisResult(Document document, Map<String, Object> analysisResults, String extractedText) {
        AnalysisResult result = new AnalysisResult();
        result.setDocument(document);

        // Extract analysis data
        result.setRiskScore((BigDecimal) analysisResults.get("riskScore"));
        result.setTotalClauses((Integer) analysisResults.get("totalClauses"));
        result.setRiskyClauses((Integer) analysisResults.get("riskyClauses"));
        result.setComplianceScore((BigDecimal) analysisResults.get("complianceScore"));
        result.setOverallRiskLevel((RiskLevel) analysisResults.get("overallRiskLevel"));
        result.setSummary((String) analysisResults.get("summary"));

        // Create risk clauses
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> riskClausesData = (List<Map<String, Object>>) analysisResults.get("riskClauses");
        List<RiskClause> riskClauses = riskClausesData.stream()
                .map(data -> createRiskClause(result, data))
                .collect(Collectors.toList());
        result.setRiskClauses(riskClauses);

        // Create recommendations
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recommendationsData = (List<Map<String, Object>>) analysisResults.get("recommendations");
        List<Recommendation> recommendations = recommendationsData.stream()
                .map(data -> createRecommendation(result, data))
                .collect(Collectors.toList());
        result.setRecommendations(recommendations);

        return result;
    }

    private RiskClause createRiskClause(AnalysisResult analysisResult, Map<String, Object> data) {
        RiskClause clause = new RiskClause();
        clause.setAnalysisResult(analysisResult);
        clause.setClauseType((ClauseType) data.get("clauseType"));
        clause.setRiskLevel((RiskLevel) data.get("riskLevel"));
        clause.setContent((String) data.get("content"));
        clause.setExplanation((String) data.get("explanation"));
        clause.setStartPosition((Integer) data.get("startPosition"));
        clause.setEndPosition((Integer) data.get("endPosition"));
        return clause;
    }

    private Recommendation createRecommendation(AnalysisResult analysisResult, Map<String, Object> data) {
        Recommendation recommendation = new Recommendation();
        recommendation.setAnalysisResult(analysisResult);
        recommendation.setType((RecommendationType) data.get("type"));
        recommendation.setPriority((Priority) data.get("priority"));
        recommendation.setTitle((String) data.get("title"));
        recommendation.setDescription((String) data.get("description"));
        recommendation.setSuggestedAction((String) data.get("suggestedAction"));
        return recommendation;
    }

    private AnalysisReportDTO convertToAnalysisReportDTO(Document document, AnalysisResult analysisResult) {
        AnalysisReportDTO dto = new AnalysisReportDTO();
        dto.setDocumentId(document.getId());
        dto.setFilename(document.getOriginalFilename());
        dto.setRiskScore(analysisResult.getRiskScore());
        dto.setTotalClauses(analysisResult.getTotalClauses());
        dto.setRiskyClauses(analysisResult.getRiskyClauses());
        dto.setComplianceScore(analysisResult.getComplianceScore());
        dto.setOverallRiskLevel(analysisResult.getOverallRiskLevel());
        dto.setSummary(analysisResult.getSummary());
        dto.setAnalysisTime(analysisResult.getAnalysisTime());

        // Convert risk clauses
        List<RiskClauseDTO> riskClauseDTOs = analysisResult.getRiskClauses().stream()
                .map(this::convertToRiskClauseDTO)
                .collect(Collectors.toList());
        dto.setRiskClauses(riskClauseDTOs);

        // Convert recommendations
        List<RecommendationDTO> recommendationDTOs = analysisResult.getRecommendations().stream()
                .map(this::convertToRecommendationDTO)
                .collect(Collectors.toList());
        dto.setRecommendations(recommendationDTOs);

        return dto;
    }

    private RiskClauseDTO convertToRiskClauseDTO(RiskClause riskClause) {
        RiskClauseDTO dto = new RiskClauseDTO();
        dto.setId(riskClause.getId());
        dto.setClauseType(riskClause.getClauseType());
        dto.setRiskLevel(riskClause.getRiskLevel());
        dto.setContent(riskClause.getContent());
        dto.setExplanation(riskClause.getExplanation());
        dto.setStartPosition(riskClause.getStartPosition());
        dto.setEndPosition(riskClause.getEndPosition());
        return dto;
    }

    private RecommendationDTO convertToRecommendationDTO(Recommendation recommendation) {
        RecommendationDTO dto = new RecommendationDTO();
        dto.setId(recommendation.getId());
        dto.setType(recommendation.getType());
        dto.setPriority(recommendation.getPriority());
        dto.setTitle(recommendation.getTitle());
        dto.setDescription(recommendation.getDescription());
        dto.setSuggestedAction(recommendation.getSuggestedAction());
        return dto;
    }

    private Map<String, Object> convertDocumentToMap(Document document) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", document.getId());
        map.put("filename", document.getOriginalFilename());
        map.put("status", document.getStatus());
        map.put("uploadTime", document.getUploadTime());
        map.put("fileSize", document.getFileSize());

        // Add analysis summary if available
        if (document.getAnalysisResult() != null) {
            AnalysisResult result = document.getAnalysisResult();
            map.put("riskScore", result.getRiskScore());
            map.put("overallRiskLevel", result.getOverallRiskLevel());
            map.put("totalClauses", result.getTotalClauses());
            map.put("riskyClauses", result.getRiskyClauses());
        }

        return map;
    }
}