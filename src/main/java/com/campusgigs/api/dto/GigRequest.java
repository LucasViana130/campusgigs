package com.campusgigs.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GigRequest(

        @NotBlank(message = "titulo e obrigatorio")
        @Size(max = 150, message = "titulo deve ter no maximo 150 caracteres")
        String title,

        @NotBlank(message = "descricao e obrigatoria")
        String description,

        @NotBlank(message = "categoria e obrigatoria")
        @Size(max = 80, message = "categoria deve ter no maximo 80 caracteres")
        String category,

        @NotNull(message = "preco e obrigatorio")
        @DecimalMin(value = "0.01", message = "preco deve ser maior que zero")
        BigDecimal price
) {
}
