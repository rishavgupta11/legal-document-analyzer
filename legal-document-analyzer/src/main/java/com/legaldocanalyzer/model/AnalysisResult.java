package com.legaldocanalyzer.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "total_clauses")
    private Integer totalClauses;

    @Column(name = "risky_clauses")
    private Integer riskyClauses;

    @Column(name = "compliance_score", precision = 5, scale = 2)
    private BigDecimal complianceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk_level")
    private RiskLevel overallRiskLevel;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @CreationTimestamp
    @Column(name = "analysis_time")
    private LocalDateTime analysisTime;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RiskClause> riskClauses;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recommendation> recommendations;

    // Constructors, getters, and setters
    public AnalysisResult() {}

    // Getters and Setters (similar pattern as Document)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

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

    public List<RiskClause> getRiskClauses() { return riskClauses; }
    public void setRiskClauses(List<RiskClause> riskClauses) { this.riskClauses = riskClauses; }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }
}