package com.bgv.platform.controller;

import com.bgv.platform.model.DocumentExtraction;
import com.bgv.platform.service.GeminiDocumentExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents/{documentId}/extraction")
public class DocumentExtractionController {

    private final GeminiDocumentExtractionService extractionService;

    public DocumentExtractionController(GeminiDocumentExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public DocumentExtraction extract(@PathVariable Long documentId) {
        return extractionService.extract(documentId);
    }

    @GetMapping
    public DocumentExtraction get(@PathVariable Long documentId) {
        return extractionService.getExtraction(documentId);
    }
}
