package com.legaldocanalyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "risk_clauses")
public class RiskClause {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "clause_type", nullable = false)
    private ClauseType clauseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "start_position")
    private Integer startPosition;

    @Column(name = "end_position")
    private Integer endPosition;

    // Constructors, getters, and setters
    public RiskClause() {}

    public RiskClause(ClauseType clauseType, RiskLevel riskLevel, String content, String explanation) {
        this.clauseType = clauseType;
        this.riskLevel = riskLevel;
        this.content = content;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AnalysisResult getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(AnalysisResult analysisResult) { this.analysisResult = analysisResult; }

    public ClauseType getClauseType() { return clauseType; }
    public void setClauseType(ClauseType clauseType) { this.clauseType = clauseType; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getStartPosition() { return startPosition; }
    public void setStartPosition(Integer startPosition) { this.startPosition = startPosition; }

    public Integer getEndPosition() { return endPosition; }
    public void setEndPosition(Integer endPosition) { this.endPosition = endPosition; }
}