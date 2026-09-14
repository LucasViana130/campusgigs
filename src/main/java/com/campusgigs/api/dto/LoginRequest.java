package com.campusgigs.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "e-mail e obrigatorio")
        @Email(message = "e-mail invalido")
        String email,

        @NotBlank(message = "senha e obrigatoria")
        String password
) {
}
