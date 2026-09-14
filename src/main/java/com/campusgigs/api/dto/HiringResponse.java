package com.campusgigs.api.dto;

import com.campusgigs.api.domain.Hiring;
import com.campusgigs.api.domain.HiringStatus;

import java.time.LocalDateTime;

public record HiringResponse(
        Long id,
        Long gigId,
        String gigTitle,
        Long hirerId,
        String hirerName,
        HiringStatus status,
        LocalDateTime createdAt
) {
    public static HiringResponse from(Hiring hiring) {
        return new HiringResponse(
                hiring.getId(),
                hiring.getGig().getId(),
                hiring.getGig().getTitle(),
                hiring.getHirer().getId(),
                hiring.getHirer().getName(),
                hiring.getStatus(),
                hiring.getCreatedAt()
        );
    }
}
