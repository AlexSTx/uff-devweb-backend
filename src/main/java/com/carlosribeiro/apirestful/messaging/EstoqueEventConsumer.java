package com.carlosribeiro.apirestful.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consome os eventos de estoque da fila do RabbitMQ e os repassa, via
 * WebSocket (STOMP), para todos os clientes que estão com a página do
 * produto aberta — eles escutam o tópico {@code /topic/produtos/{id}}.
 *
 * Decidiu-se manter o publicador (PedidoService) e o consumidor (aqui)
 * separados para que o tamanho do fluxo fique explícito: domínio publica →
 * RabbitMQ entrega → WebSocket broadcaster envia → navegador atualiza.
 * Ambos os eventos (esgotado e reposto) são roteados para o mesmo tópico
 * por produto; o frontend distingue pelo campo "tipo" implícito no payload.
 */
@Component
public class EstoqueEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEventConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public EstoqueEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ESTOQUE_ESGOTADO_WS)
    public void onEstoqueEsgotado(EstoqueEsgotadoEvent evento) {
        String destino = "/topic/produtos/" + evento.produtoId();
        log.info(
            "Broadcast EstoqueEsgotadoEvent via WebSocket para {}: qtdEstoqueFinal={}",
            destino, evento.qtdEstoqueFinal()
        );
        messagingTemplate.convertAndSend(destino, evento);
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ESTOQUE_REPOSTO_WS)
    public void onEstoqueReposto(EstoqueRepostoEvent evento) {
        String destino = "/topic/produtos/" + evento.produtoId();
        log.info(
            "Broadcast EstoqueRepostoEvent via WebSocket para {}: qtdEstoqueAtual={}",
            destino, evento.qtdEstoqueAtual()
        );
        messagingTemplate.convertAndSend(destino, evento);
    }
}