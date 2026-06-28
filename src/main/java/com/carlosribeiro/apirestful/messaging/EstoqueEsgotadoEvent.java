package com.carlosribeiro.apirestful.messaging;

import java.time.LocalDateTime;

/**
 * Evento publicado no RabbitMQ quando o estoque físico de um produto zera
 * (após o pagamento de um pedido que consumiu a reserva da última unidade).
 *
 * É "global": qualquer consumidor interessado (no nosso caso, o broadcaster
 * WebSocket que avisa os clientes com a página do produto aberta) pode
 * assinar a exchange e reagir.
 */
public record EstoqueEsgotadoEvent(
    Long produtoId,
    String nome,
    int qtdEstoqueFinal,
    LocalDateTime ocorridoEm
) {
}