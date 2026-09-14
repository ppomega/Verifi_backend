package com.bgv.platform.controller;

import com.bgv.platform.dto.CandidateChatRequest;
import com.bgv.platform.dto.CandidateChatResponse;
import com.bgv.platform.service.CandidateChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates/{candidateId}/support/chat")
public class CandidateChatController {

    private final CandidateChatService candidateChatService;

    public CandidateChatController(CandidateChatService candidateChatService) {
        this.candidateChatService = candidateChatService;
    }

    @PostMapping
    public CandidateChatResponse chat(@PathVariable Long candidateId,
                                      @Valid @RequestBody CandidateChatRequest request) {
        return candidateChatService.reply(candidateId, request.message());
    }
}
