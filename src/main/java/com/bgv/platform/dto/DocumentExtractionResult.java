package com.bgv.platform.dto;

public record DocumentExtractionResult(
        String documentType,
        String fullName,
        String dateOfBirth,
        String documentNumber,
        String employer,
        String institute,
        String issueDate,
        String expiryDate,
        Double confidence,
        String notes) {
}
