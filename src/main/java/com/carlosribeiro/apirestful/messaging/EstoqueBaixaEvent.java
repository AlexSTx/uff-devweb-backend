package com.carlosribeiro.apirestful.messaging;

import java.time.LocalDateTime;

/**
 * Evento publicado sempre que o estoque físico de um produto DIMINUI — seja
 * porque uma compra zerou o produto (esgotou), seja porque uma reserva apenas
 * reduziu a quantidade disponível (baixa parcial). Carrega o estoque atual
 * ({@code qtdEstoqueAtual}) para que o frontend (página de produto, carrinho e
 * checkout) reavalie em tempo real se a quantidade que está no carrinho ainda
 * cabe no estoque — e, se não couber, peça para o usuário diminuir.
 *
 * Vai para a fila {@code estoque-baixa.ws} via routing key
 * {@code estoque.baixa}.
 */
public record EstoqueBaixaEvent(
    Long produtoId,
    String nome,
    int qtdEstoqueAtual,
    LocalDateTime ocorridoEm
) {
}
