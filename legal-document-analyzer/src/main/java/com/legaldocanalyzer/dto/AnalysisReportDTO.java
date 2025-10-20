package com.legaldocanalyzer.dto;

import com.legaldocanalyzer.model.RiskLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AnalysisReportDTO {
    private String documentId;
    private String filename;
    private BigDecimal riskScore;
    private Integer totalClauses;
    private Integer riskyClauses;
    private BigDecimal complianceScore;
    private RiskLevel overallRiskLevel;
    private String summary;
    private LocalDateTime analysisTime;
    private List<RiskClauseDTO> riskClauses;
    private List<RecommendationDTO> recommendations;

    public AnalysisReportDTO() {}

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public Integer getTotalClauses() { return totalClauses; }
    public void setTotalClauses(Integer totalClauses) { this.totalClauses = totalClauses; }

    public Integer getRiskyClauses() { return riskyClauses; }
    public void setRiskyClauses(Integer riskyClauses) { this.riskyClauses = riskyClauses; }

    public BigDecimal getComplianceScore() { return complianceScore; }
    public void setComplianceScore(BigDecimal complianceScore) { this.complianceScore = complianceScore; }

    public RiskLevel getOverallRiskLevel() { return overallRiskLevel; }
    public void setOverallRiskLevel(RiskLevel overallRiskLevel) { this.overallRiskLevel = overallRiskLevel; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public void setAnalysisTime(LocalDateTime analysisTime) { this.analysisTime = analysisTime; }

    public List<RiskClauseDTO> getRiskClauses() { return riskClauses; }
    public void setRiskClauses(List<RiskClauseDTO> riskClauses) { this.riskClauses = riskClauses; }

    public List<RecommendationDTO> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationDTO> recommendations) { this.recommendations = recommendations; }
}