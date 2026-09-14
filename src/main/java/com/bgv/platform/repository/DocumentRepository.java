package com.bgv.platform.repository;

import com.bgv.platform.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCandidateId(Long candidateId);
}
