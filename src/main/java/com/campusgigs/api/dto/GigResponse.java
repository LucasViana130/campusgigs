package com.campusgigs.api.dto;

import com.campusgigs.api.domain.Gig;
import com.campusgigs.api.domain.GigStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GigResponse(
        Long id,
        Long providerId,
        String providerName,
        String title,
        String description,
        String category,
        BigDecimal price,
        GigStatus status,
        LocalDateTime createdAt
) {
    public static GigResponse from(Gig gig) {
        return new GigResponse(
                gig.getId(),
                gig.getProvider().getId(),
                gig.getProvider().getName(),
                gig.getTitle(),
                gig.getDescription(),
                gig.getCategory(),
                gig.getPrice(),
                gig.getStatus(),
                gig.getCreatedAt()
        );
    }
}
