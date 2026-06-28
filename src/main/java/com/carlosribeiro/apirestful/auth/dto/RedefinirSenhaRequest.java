package com.carlosribeiro.apirestful.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record RedefinirSenhaRequest(
    @NotEmpty(message = "O 'Email' deve ser informado.")
    @Email(message = "Email inválido.")
    String email,

    @NotEmpty(message = "A 'Senha' deve ser informada.")
    String novaSenha
) {
}
