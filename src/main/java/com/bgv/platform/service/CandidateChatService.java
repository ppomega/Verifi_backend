package com.bgv.platform.service;

import com.bgv.platform.dto.CandidateChatResponse;
import com.bgv.platform.model.Candidate;
import com.bgv.platform.model.Document;
import com.bgv.platform.model.Verification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CandidateChatService {

    private final CandidateService candidateService;
    private final DocumentService documentService;
    private final VerificationService verificationService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public CandidateChatService(CandidateService candidateService,
                                DocumentService documentService,
                                VerificationService verificationService,
                                ChatClient.Builder chatClientBuilder,
                                ObjectMapper objectMapper) {
        this.candidateService = candidateService;
        this.documentService = documentService;
        this.verificationService = verificationService;
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public CandidateChatResponse reply(Long candidateId, String question) {
        Candidate candidate = candidateService.findById(candidateId);
        try {
            String caseContext = objectMapper.writeValueAsString(Map.of(
                    "candidateStatus", String.valueOf(candidate.getStatus()),
                    "documents", publicDocuments(candidateId),
                    "verifications", publicVerifications(candidateId)));
            String answer = chatClient.prompt()
                    .system("You are a candidate-support assistant for a background-verification platform. "
                            + "Answer only from the supplied case context. Do not expose internal reviewer remarks, risk scores, "
                            + "or decisions. If the answer is not present, ask the candidate to contact support. Keep answers concise.")
                    .user("Candidate question: " + question + "\n\nCase context: " + caseContext)
                    .call()
                    .content();
            return new CandidateChatResponse(answer);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not prepare the candidate chat context.");
        } catch (RuntimeException e) {
            throw new com.bgv.platform.exception.AiServiceUnavailableException(
                    "Gemini chat request failed. Verify model access and API quota, then retry.");
        }
    }

    private List<Map<String, String>> publicDocuments(Long candidateId) {
        return documentService.findByCandidate(candidateId).stream()
                .map(document -> Map.of("type", String.valueOf(document.getDocumentType())))
                .toList();
    }

    private List<Map<String, String>> publicVerifications(Long candidateId) {
        return verificationService.findByCandidate(candidateId).stream()
                .map(verification -> Map.of(
                        "type", String.valueOf(verification.getType()),
                        "status", String.valueOf(verification.getStatus())))
                .toList();
    }

}
