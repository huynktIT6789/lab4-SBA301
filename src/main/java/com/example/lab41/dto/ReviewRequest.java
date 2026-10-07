package com.example.lab41.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(
        @NotBlank String userId,
        @Min(1) @Max(5) int rating,
        @NotBlank String comment
) {
}
