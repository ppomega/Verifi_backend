package com.bgv.platform.controller;

import com.bgv.platform.dto.VerificationRequest;
import com.bgv.platform.model.Verification;
import com.bgv.platform.service.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/api/candidates/{candidateId}/verifications")
    public ResponseEntity<Verification> create(@PathVariable Long candidateId, @RequestBody VerificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.create(candidateId, request));
    }

    @GetMapping("/api/candidates/{candidateId}/verifications")
    public List<Verification> listForCandidate(@PathVariable Long candidateId) {
        return verificationService.findByCandidate(candidateId);
    }

    @GetMapping("/api/verifications/{id}")
    public Verification getById(@PathVariable Long id) {
        return verificationService.findById(id);
    }

    @PutMapping("/api/verifications/{id}")
    public Verification update(@PathVariable Long id, @RequestBody VerificationRequest request) {
        return verificationService.update(id, request);
    }

    @DeleteMapping("/api/verifications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        verificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
