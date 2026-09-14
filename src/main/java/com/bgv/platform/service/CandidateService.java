package com.bgv.platform.service;

import com.bgv.platform.dto.CandidateRequest;
import com.bgv.platform.exception.ResourceNotFoundException;
import com.bgv.platform.model.Candidate;
import com.bgv.platform.model.enums.CandidateStatus;
import com.bgv.platform.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public Candidate create(CandidateRequest request) {
        if (candidateRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A candidate with this email already exists");
        }
        Candidate candidate = new Candidate();
        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setCurrentAddress(request.getCurrentAddress());
        candidate.setPositionAppliedFor(request.getPositionAppliedFor());
        candidate.setStatus(CandidateStatus.REGISTERED);
        return candidateRepository.save(candidate);
    }

    public List<Candidate> findAll() {
        return candidateRepository.findAll();
    }

    public Candidate findById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id " + id));
    }

    @Transactional
    public Candidate update(Long id, CandidateRequest request) {
        Candidate candidate = findById(id);
        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setCurrentAddress(request.getCurrentAddress());
        candidate.setPositionAppliedFor(request.getPositionAppliedFor());
        return candidateRepository.save(candidate);
    }

    @Transactional
    public Candidate updateStatus(Long id, CandidateStatus status) {
        Candidate candidate = findById(id);
        candidate.setStatus(status);
        return candidateRepository.save(candidate);
    }

    @Transactional
    public void delete(Long id) {
        Candidate candidate = findById(id);
        candidateRepository.delete(candidate);
    }
}
