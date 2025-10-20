package com.legaldocanalyzer.dto;

import com.legaldocanalyzer.model.RecommendationType;
import com.legaldocanalyzer.model.Priority;

public class RecommendationDTO {
    private String id;
    private RecommendationType type;
    private Priority priority;
    private String title;
    private String description;
    private String suggestedAction;

    public RecommendationDTO() {}

    public RecommendationDTO(RecommendationType type, Priority priority, String title,
                             String description, String suggestedAction) {
        this.type = type;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.suggestedAction = suggestedAction;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RecommendationType getType() { return type; }
    public void setType(RecommendationType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
}