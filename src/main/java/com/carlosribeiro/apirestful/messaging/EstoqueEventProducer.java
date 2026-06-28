package com.carlosribeiro.apirestful.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica o {@link EstoqueEsgotadoEvent} na exchange de eventos de estoque
 * do RabbitMQ. É chamado pelo domínio (PedidoService) quando o pagamento
 * zera o estoque físico de um produto, mantendo o código de negócio isolado
 * dos detalhes do broker.
 */
@Component
public class EstoqueEventProducer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public EstoqueEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEstoqueEsgotado(EstoqueEsgotadoEvent evento) {
        log.info(
            "Publicando EstoqueEsgotadoEvent: produtoId={} nome={} qtdEstoqueFinal={}",
            evento.produtoId(), evento.nome(), evento.qtdEstoqueFinal()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_ESTOQUE,
            "", // Fanout: routing key ignorada
            evento
        );
    }

    public void publicarEstoqueReposto(EstoqueRepostoEvent evento) {
        log.info(
            "Publicando EstoqueRepostoEvent: produtoId={} nome={} qtdEstoqueAtual={}",
            evento.produtoId(), evento.nome(), evento.qtdEstoqueAtual()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_ESTOQUE,
            "",
            evento
        );
    }
}