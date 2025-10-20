package com.legaldocanalyzer.repository;

import com.legaldocanalyzer.model.Recommendation;
import com.legaldocanalyzer.model.RecommendationType;
import com.legaldocanalyzer.model.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, String> {

    // Find recommendations by analysis result ID
    List<Recommendation> findByAnalysisResultId(String analysisResultId);

    // Find recommendations by priority
    List<Recommendation> findByPriorityOrderByPriorityDesc(Priority priority);

    // Find recommendations by type
    List<Recommendation> findByType(RecommendationType type);

    // Find high-priority recommendations
    List<Recommendation> findByPriorityInOrderByPriorityDesc(List<Priority> priorities);

    // Find recommendations by document ID through analysis result
    @Query("SELECT r FROM Recommendation r JOIN r.analysisResult ar WHERE ar.document.id = :documentId")
    List<Recommendation> findByDocumentId(@Param("documentId") String documentId);

    // Get recommendation statistics
    @Query("SELECT r.type, r.priority, COUNT(r) FROM Recommendation r GROUP BY r.type, r.priority")
    List<Object[]> getRecommendationStatistics();
}