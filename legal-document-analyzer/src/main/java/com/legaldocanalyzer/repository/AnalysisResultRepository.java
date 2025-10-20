package com.legaldocanalyzer.repository;

import com.legaldocanalyzer.model.AnalysisResult;
import com.legaldocanalyzer.model.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, String> {

    // Find analysis result by document ID
    Optional<AnalysisResult> findByDocumentId(String documentId);

    // Find analysis results by risk level
    List<AnalysisResult> findByOverallRiskLevel(RiskLevel riskLevel);

    // Find high-risk documents
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.riskScore >= :minScore")
    List<AnalysisResult> findHighRiskDocuments(@Param("minScore") BigDecimal minScore);

    // Find analysis results within date range
    List<AnalysisResult> findByAnalysisTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Get statistics query
    @Query("SELECT ar.overallRiskLevel, COUNT(ar) FROM AnalysisResult ar GROUP BY ar.overallRiskLevel")
    List<Object[]> getRiskLevelStatistics();
}