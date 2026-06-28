package com.carlosribeiro.apirestful.messaging;

import java.time.LocalDateTime;

/**
 * Evento publicado no RabbitMQ quando o estoque físico de um produto volta a
 * ficar disponível (ex.: pedido pendente foi cancelado e a reserva voltou ao
 * estoque). O frontend que estava com a página do produto aberta deve sair do
 * estado "Esgotado" e habilitar novamente o botão de adicionar ao carrinho.
 */
public record EstoqueRepostoEvent(
    Long produtoId,
    String nome,
    int qtdEstoqueAtual,
    LocalDateTime ocorridoEm
) {
}