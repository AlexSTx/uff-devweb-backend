package com.carlosribeiro.apirestful.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica os eventos de estoque na exchange (direct) de eventos do RabbitMQ.
 * É chamado pelo domínio (PedidoService) a cada alteração de estoque físico,
 * mantendo o código de negócio isolado dos detalhes do broker.
 *
 * A routing key separa as duas filas: baixa (estoque diminuiu) e alta
 * (estoque aumentou).
 */
@Component
public class EstoqueEventProducer {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public EstoqueEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEstoqueBaixa(EstoqueBaixaEvent evento) {
        log.info(
            "Publicando EstoqueBaixaEvent: produtoId={} nome={} qtdEstoqueAtual={}",
            evento.produtoId(), evento.nome(), evento.qtdEstoqueAtual()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_ESTOQUE,
            RabbitMQConfig.RK_ESTOQUE_BAIXA,
            evento
        );
    }

    public void publicarEstoqueAlta(EstoqueAltaEvent evento) {
        log.info(
            "Publicando EstoqueAltaEvent: produtoId={} nome={} qtdEstoqueAtual={}",
            evento.produtoId(), evento.nome(), evento.qtdEstoqueAtual()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_ESTOQUE,
            RabbitMQConfig.RK_ESTOQUE_ALTA,
            evento
        );
    }
}
