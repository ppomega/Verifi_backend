package com.bgv.platform.service;

import com.bgv.platform.dto.VerificationRequest;
import com.bgv.platform.exception.ResourceNotFoundException;
import com.bgv.platform.model.Candidate;
import com.bgv.platform.model.Verification;
import com.bgv.platform.model.enums.VerificationStatus;
import com.bgv.platform.repository.VerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final CandidateService candidateService;

    public VerificationService(VerificationRepository verificationRepository, CandidateService candidateService) {
        this.verificationRepository = verificationRepository;
        this.candidateService = candidateService;
    }

    @Transactional
    public Verification create(Long candidateId, VerificationRequest request) {
        Candidate candidate = candidateService.findById(candidateId);
        Verification verification = new Verification();
        verification.setCandidate(candidate);
        verification.setType(request.getType());
        verification.setStatus(request.getStatus() != null ? request.getStatus() : VerificationStatus.PENDING);
        verification.setRemarks(request.getRemarks());
        return verificationRepository.save(verification);
    }

    public List<Verification> findByCandidate(Long candidateId) {
        candidateService.findById(candidateId);
        return verificationRepository.findByCandidateId(candidateId);
    }

    public Verification findById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found with id " + id));
    }

    @Transactional
    public Verification update(Long id, VerificationRequest request) {
        Verification verification = findById(id);
        if (request.getType() != null) {
            verification.setType(request.getType());
        }
        if (request.getStatus() != null) {
            verification.setStatus(request.getStatus());
            if (request.getStatus() == VerificationStatus.VERIFIED
                    || request.getStatus() == VerificationStatus.REJECTED) {
                verification.setVerifiedAt(LocalDateTime.now());
            }
        }
        if (request.getRemarks() != null) {
            verification.setRemarks(request.getRemarks());
        }
        if (request.getVerifiedBy() != null) {
            verification.setVerifiedBy(request.getVerifiedBy());
        }
        return verificationRepository.save(verification);
    }

    @Transactional
    public void delete(Long id) {
        Verification verification = findById(id);
        verificationRepository.delete(verification);
    }
}
