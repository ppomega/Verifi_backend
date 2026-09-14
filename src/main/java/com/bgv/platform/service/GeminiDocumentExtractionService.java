package com.bgv.platform.service;

import com.bgv.platform.dto.DocumentExtractionResult;
import com.bgv.platform.exception.AiServiceUnavailableException;
import com.bgv.platform.exception.InvalidFileException;
import com.bgv.platform.model.Document;
import com.bgv.platform.model.DocumentExtraction;
import com.bgv.platform.repository.DocumentExtractionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.time.LocalDateTime;

@Service
public class GeminiDocumentExtractionService {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final DocumentExtractionRepository extractionRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public GeminiDocumentExtractionService(DocumentService documentService,
                                           FileStorageService fileStorageService,
                                           DocumentExtractionRepository extractionRepository,
                                           ChatClient.Builder chatClientBuilder,
                                           ObjectMapper objectMapper) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.extractionRepository = extractionRepository;
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DocumentExtraction extract(Long documentId) {
        Document document = documentService.findById(documentId);
        MimeType mimeType = supportedMimeType(document.getContentType());
        Resource resource = fileStorageService.loadAsResource(document.getFilePath());
        DocumentExtraction extraction = extractionRepository.findByDocumentId(documentId)
                .orElseGet(DocumentExtraction::new);
        extraction.setDocument(document);
        extraction.setStatus(DocumentExtraction.Status.PROCESSING);
        extraction.setErrorMessage(null);
        extractionRepository.save(extraction);

        try {
            DocumentExtractionResult result = chatClient.prompt()
                    .system("You extract fields from background-verification documents. Return only factual values visible in the document. "
                            + "Use null when a field is not present; never guess or infer a value.")
                    .user(user -> user.text(extractionPrompt(document))
                            .media(mimeType, resource))
                    .call()
                    .entity(DocumentExtractionResult.class);

            extraction.setExtractedJson(objectMapper.writeValueAsString(result));
            extraction.setConfidence(result.confidence());
            extraction.setStatus(DocumentExtraction.Status.COMPLETED);
            extraction.setProcessedAt(LocalDateTime.now());
            return extractionRepository.save(extraction);
        } catch (JsonProcessingException e) {
            throw new AiServiceUnavailableException("Could not save the AI extraction result.");
        } catch (RuntimeException e) {
            extraction.setStatus(DocumentExtraction.Status.FAILED);
            extraction.setErrorMessage("Document extraction could not be completed.");
            extraction.setProcessedAt(LocalDateTime.now());
            extractionRepository.save(extraction);
            throw new AiServiceUnavailableException("Document extraction could not be completed. Check the Gemini configuration and retry.");
        }
    }

    public DocumentExtraction getExtraction(Long documentId) {
        documentService.findById(documentId);
        return extractionRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new com.bgv.platform.exception.ResourceNotFoundException(
                        "No extraction found for document " + documentId));
    }

    private String extractionPrompt(Document document) {
        return "Extract fields from this " + document.getDocumentType() + " document. "
                + "Set documentType to " + document.getDocumentType() + ". "
                + "Map PAN, Aadhaar, passport, or other identifier to documentNumber. "
                + "Dates must use YYYY-MM-DD where a full date is visible. "
                + "confidence must be a number from 0 to 1. "
                + "notes may briefly state unreadable or missing information.";
    }

    private MimeType supportedMimeType(String contentType) {
        if (MediaTypeNames.JPEG.equals(contentType) || MediaTypeNames.PNG.equals(contentType)
                || MediaTypeNames.PDF.equals(contentType)) {
            return MimeType.valueOf(contentType);
        }
        throw new InvalidFileException("AI extraction currently supports PDF, JPG, JPEG, and PNG documents only.");
    }

    private static final class MediaTypeNames {
        private static final String JPEG = MimeTypeUtils.IMAGE_JPEG_VALUE;
        private static final String PNG = MimeTypeUtils.IMAGE_PNG_VALUE;
        private static final String PDF = "application/pdf";
    }
}
