package com.bgv.platform.repository;

import com.bgv.platform.model.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, Long> {
    Optional<DocumentExtraction> findByDocumentId(Long documentId);
}
