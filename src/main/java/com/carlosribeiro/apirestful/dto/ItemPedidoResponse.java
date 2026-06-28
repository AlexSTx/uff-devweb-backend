package com.carlosribeiro.apirestful.dto;

import java.math.BigDecimal;

public record ItemPedidoResponse(
    Long id,
    Long produtoId,
    String nome,
    String imagem,
    BigDecimal precoUnitario,
    int quantidade,
    BigDecimal subtotal
) {
}
