package com.legaldocanalyzer.repository;

import com.legaldocanalyzer.model.RiskClause;
import com.legaldocanalyzer.model.ClauseType;
import com.legaldocanalyzer.model.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskClauseRepository extends JpaRepository<RiskClause, String> {

    // Find risk clauses by analysis result ID
    List<RiskClause> findByAnalysisResultId(String analysisResultId);

    // Find risk clauses by type and risk level
    List<RiskClause> findByClauseTypeAndRiskLevel(ClauseType clauseType, RiskLevel riskLevel);

    // Find high-risk clauses
    List<RiskClause> findByRiskLevelIn(List<RiskLevel> riskLevels);

    // Count clauses by type
    @Query("SELECT rc.clauseType, COUNT(rc) FROM RiskClause rc GROUP BY rc.clauseType")
    List<Object[]> getClauseTypeStatistics();

    // Find clauses by document ID through analysis result
    @Query("SELECT rc FROM RiskClause rc JOIN rc.analysisResult ar WHERE ar.document.id = :documentId")
    List<RiskClause> findByDocumentId(@Param("documentId") String documentId);
}
