package com.bgv.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateChatRequest(
        @NotBlank @Size(max = 1000) String message) {
}
