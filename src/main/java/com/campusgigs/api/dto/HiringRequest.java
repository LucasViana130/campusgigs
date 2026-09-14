package com.campusgigs.api.dto;

import jakarta.validation.constraints.NotNull;

public record HiringRequest(
        @NotNull(message = "gigId e obrigatorio")
        Long gigId
) {
}
