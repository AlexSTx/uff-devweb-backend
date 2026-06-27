package com.carlosribeiro.apirestful.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemCarrinhoResponse(
    Long id,
    Long produtoId,
    String nome,
    String descricao,
    String imagem,
    BigDecimal precoUnitario,
    int quantidade,
    BigDecimal subtotal,
    LocalDateTime dataAdicao
) {
}