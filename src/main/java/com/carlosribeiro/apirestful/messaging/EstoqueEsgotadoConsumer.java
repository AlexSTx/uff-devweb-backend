package com.carlosribeiro.apirestful.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consome o {@link EstoqueEsgotadoEvent} da fila do RabbitMQ e o repassa, via
 * WebSocket (STOMP), para todos os clientes que estão com a página do produto
 * aberta — eles escutam o tópico {@code /topic/produtos/{id}}.
 *
 * Decidiu-se manter o publicador (PedidoService) e o consumidor (aqui)
 * separados para que o tamanho do fluxo fique explícito: domínio publica →
 * RabbitMQ entrega → WebSocket broadcaster envia → navegador atualiza.
 */
@Component
public class EstoqueEsgotadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEsgotadoConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public EstoqueEsgotadoConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ESTOQUE_ESGOTADO_WS)
    public void onEstoqueEsgotado(EstoqueEsgotadoEvent evento) {
        String destino = "/topic/produtos/" + evento.produtoId();
        log.info(
            "Broadcast EstoqueEsgotadoEvent via WebSocket para {}: qtdEstoqueFinal={}",
            destino, evento.qtdEstoqueFinal()
        );
        // Serializado para JSON pelo Jackson configurado no WebSocket.
        messagingTemplate.convertAndSend(destino, evento);
    }
}