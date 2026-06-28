package com.carlosribeiro.apirestful.dto;

import com.carlosribeiro.apirestful.model.FormaPagamento;
import com.carlosribeiro.apirestful.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    LocalDateTime dataPedido,
    BigDecimal valorTotal,
    StatusPedido status,
    FormaPagamento formaPagamento,
    List<ItemPedidoResponse> itens
) {
}
