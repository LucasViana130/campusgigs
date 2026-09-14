package com.campusgigs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCepRequest(
        @NotBlank(message = "CEP e obrigatorio")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 digitos numericos (sem hifen)")
        String cep
) {
}
