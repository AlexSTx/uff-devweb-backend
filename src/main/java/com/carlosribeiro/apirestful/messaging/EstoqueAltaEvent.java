package com.carlosribeiro.apirestful.messaging;

import java.time.LocalDateTime;

/**
 * Evento publicado sempre que o estoque físico de um produto AUMENTA — seja
 * porque um produto esgotado voltou a ter unidades (reestocado), seja porque
 * uma reserva foi devolvida e a quantidade disponível subiu (alta parcial).
 * Acontece quando um pedido pendente é cancelado ou expira e a reserva volta
 * ao estoque. Carrega o estoque atual ({@code qtdEstoqueAtual}) para que o
 * frontend destrave/atualize carrinho, checkout e página de produto na hora.
 *
 * Vai para a fila {@code estoque-alta.ws} via routing key
 * {@code estoque.alta}.
 */
public record EstoqueAltaEvent(
    Long produtoId,
    String nome,
    int qtdEstoqueAtual,
    LocalDateTime ocorridoEm
) {
}
