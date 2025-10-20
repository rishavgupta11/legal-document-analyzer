package com.legaldocanalyzer.dto;

import com.legaldocanalyzer.model.ClauseType;
import com.legaldocanalyzer.model.RiskLevel;

public class RiskClauseDTO {
    private String id;
    private ClauseType clauseType;
    private RiskLevel riskLevel;
    private String content;
    private String explanation;
    private Integer startPosition;
    private Integer endPosition;

    public RiskClauseDTO() {}

    public RiskClauseDTO(ClauseType clauseType, RiskLevel riskLevel, String content, String explanation) {
        this.clauseType = clauseType;
        this.riskLevel = riskLevel;
        this.content = content;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

