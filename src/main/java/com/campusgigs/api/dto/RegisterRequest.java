package com.campusgigs.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter no maximo 120 caracteres")
        String name,

        @NotBlank(message = "e-mail e obrigatorio")
        @Email(message = "e-mail invalido")
        @Size(max = 180, message = "e-mail deve ter no maximo 180 caracteres")
        String email,

        @NotBlank(message = "senha e obrigatoria")
        @Size(min = 6, max = 100, message = "senha deve ter entre 6 e 100 caracteres")
        String password,

        @NotBlank(message = "CEP e obrigatorio")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 digitos numericos (sem hifen)")
        String cep
) {
}
