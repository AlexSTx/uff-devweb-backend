package com.carlosribeiro.apirestful.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCarrinhoRequest(
    @NotNull(message = "O 'produtoId' deve ser informado.")
    Long produtoId,
    @NotNull(message = "A 'quantidade' deve ser informada.")
    @Min(value = 1, message = "A 'quantidade' deve ser >= 1.")
    Integer quantidade
) {
}