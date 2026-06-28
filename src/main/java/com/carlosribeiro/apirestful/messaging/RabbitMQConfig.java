package com.carlosribeiro.apirestful.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a exchange de eventos de estoque e a fila que o próprio backend
 * consome para relayer as notificações via WebSocket aos clientes.
 *
 * Usamos uma FanoutExchange porque o "EstoqueEsgotadoEvent" é um evento
 * global/broadcast: não há chave de roteamento relevante, todos os
 * assinantes da exchange recebem a mensagem.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ESTOQUE = "estoque.events";
    public static final String FILA_ESTOQUE_ESGOTADO_WS = "estoque-esgotado.ws";
    public static final String FILA_ESTOQUE_REPOSTO_WS = "estoque-reposto.ws";

    @Bean
    public FanoutExchange exchangeEstoque() {
        // durable=true: a exchange sobrevive a reinícios do broker.
        return new FanoutExchange(EXCHANGE_ESTOQUE, true, false);
    }

    @Bean
    public Queue filaEstoqueEsgotadoWs() {
        // durable=true: a fila é recriada automaticamente após reinício do broker.
        return new Queue(FILA_ESTOQUE_ESGOTADO_WS, true);
    }

    @Bean
    public Queue filaEstoqueRepostoWs() {
        return new Queue(FILA_ESTOQUE_REPOSTO_WS, true);
    }

    @Bean
    public Binding bindingEstoqueEsgotadoWs(
        Queue filaEstoqueEsgotadoWs,
        FanoutExchange exchangeEstoque
    ) {
        // Fanout ignora a routing key; toda mensagem da exchange vai para a fila.
        return BindingBuilder.bind(filaEstoqueEsgotadoWs).to(exchangeEstoque);
    }

    @Bean
    public Binding bindingEstoqueRepostoWs(
        Queue filaEstoqueRepostoWs,
        FanoutExchange exchangeEstoque
    ) {
        return BindingBuilder.bind(filaEstoqueRepostoWs).to(exchangeEstoque);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter() {
        // Serializa/deserializa os records como JSON em vez de Serializable
        // nativo do Java — fica mais legível no management UI do RabbitMQ e
        // independente de classe entre produtor e consumidor.
        return new JacksonJsonMessageConverter();
    }
}