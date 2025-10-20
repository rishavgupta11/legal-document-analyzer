package com.legaldocanalyzer.repository;

import com.legaldocanalyzer.model.Document;
import com.legaldocanalyzer.model.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    // Find documents by user ID
    Page<Document> findByUserIdOrderByUploadTimeDesc(String userId, Pageable pageable);

    // Find documents by status
    List<Document> findByStatus(DocumentStatus status);

    // Find documents by user and status
    List<Document> findByUserIdAndStatus(String userId, DocumentStatus status);

    // Find documents uploaded within a date range
    List<Document> findByUploadTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find documents by filename pattern
    List<Document> findByFilenameContainingIgnoreCase(String filename);

    // Custom query to find documents with analysis results
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.analysisResult WHERE d.userId = :userId")
    List<Document> findByUserIdWithAnalysisResults(@Param("userId") String userId);

    // Find documents that need processing
    @Query("SELECT d FROM Document d WHERE d.status = :status AND d.uploadTime < :before")
    List<Document> findStaleDocumentsForProcessing(@Param("status") DocumentStatus status,
                                                   @Param("before") LocalDateTime before);
}
