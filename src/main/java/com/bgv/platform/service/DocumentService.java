package com.bgv.platform.service;

import com.bgv.platform.model.Candidate;
import com.bgv.platform.model.Document;
import com.bgv.platform.model.enums.DocumentType;
import com.bgv.platform.exception.ResourceNotFoundException;
import com.bgv.platform.repository.DocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final CandidateService candidateService;

    public DocumentService(DocumentRepository documentRepository,
                            FileStorageService fileStorageService,
                            CandidateService candidateService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.candidateService = candidateService;
    }

    @Transactional
    public Document upload(Long candidateId, DocumentType documentType, MultipartFile file) {
        Candidate candidate = candidateService.findById(candidateId);

        FileStorageService.StoredFile stored = fileStorageService.store(file, candidateId);

        Document document = new Document();
        document.setCandidate(candidate);
        document.setDocumentType(documentType);
        document.setOriginalFileName(stored.originalFileName());
        document.setStoredFileName(stored.storedFileName());
        document.setFilePath(stored.relativePath());
        document.setContentType(stored.contentType());
        document.setFileSizeBytes(stored.size());

        return documentRepository.save(document);
    }

    public List<Document> findByCandidate(Long candidateId) {
        candidateService.findById(candidateId);
        return documentRepository.findByCandidateId(candidateId);
    }

    public Document findById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + documentId));
    }

    public Resource loadFile(Long documentId) {
        Document document = findById(documentId);
        return fileStorageService.loadAsResource(document.getFilePath());
    }

    @Transactional
    public void delete(Long documentId) {
        Document document = findById(documentId);
        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }
}
