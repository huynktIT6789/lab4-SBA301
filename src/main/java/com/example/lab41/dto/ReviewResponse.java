package com.example.lab41.dto;

import java.time.Instant;

public record ReviewResponse(
        String userId,
        int rating,
        String comment,
        Instant createdAt
) {
}
