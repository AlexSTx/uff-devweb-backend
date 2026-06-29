package com.carlosribeiro.apirestful.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consome os eventos de estoque das filas do RabbitMQ e os repassa, via
 * WebSocket (STOMP), para todos os clientes inscritos no tópico do produto
 * ({@code /topic/produtos/{id}}) — páginas de produto, carrinho e checkout.
 *
 * São DUAS filas distintas, uma por direção da mudança de estoque. Como a
 * exchange é direct (routing key), cada listener recebe só o seu tipo de
 * evento — sem entrega cruzada e sem eventos fantasma. Ambos repassam para o
 * mesmo tópico por produto; o frontend reage ao novo {@code qtdEstoqueAtual}.
 */
@Component
public class EstoqueEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEventConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public EstoqueEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ESTOQUE_BAIXA_WS)
    public void onEstoqueBaixa(EstoqueBaixaEvent evento) {
        String destino = "/topic/produtos/" + evento.produtoId();
        log.info(
            "Broadcast EstoqueBaixaEvent via WebSocket para {}: qtdEstoqueAtual={}",
            destino, evento.qtdEstoqueAtual()
        );
        messagingTemplate.convertAndSend(destino, evento);
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ESTOQUE_ALTA_WS)
    public void onEstoqueAlta(EstoqueAltaEvent evento) {
        String destino = "/topic/produtos/" + evento.produtoId();
        log.info(
            "Broadcast EstoqueAltaEvent via WebSocket para {}: qtdEstoqueAtual={}",
            destino, evento.qtdEstoqueAtual()
        );
        messagingTemplate.convertAndSend(destino, evento);
    }
}
