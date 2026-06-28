package com.carlosribeiro.apirestful.dto;

import com.carlosribeiro.apirestful.model.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
    @NotNull(message = "A 'formaPagamento' deve ser informada.")
    FormaPagamento formaPagamento
) {
}
