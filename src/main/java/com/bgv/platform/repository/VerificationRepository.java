package com.bgv.platform.repository;

import com.bgv.platform.model.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    List<Verification> findByCandidateId(Long candidateId);
}
