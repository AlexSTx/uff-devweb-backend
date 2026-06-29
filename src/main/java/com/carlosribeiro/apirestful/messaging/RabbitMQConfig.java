package com.carlosribeiro.apirestful.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a exchange de eventos de estoque e as DUAS filas que o próprio
 * backend consome para repassar as notificações via WebSocket aos clientes.
 *
 * Usamos uma {@link DirectExchange} (com routing key), e NÃO um fanout. Esse é
 * o ponto importante: com fanout, toda mensagem ia para TODAS as filas, então a
 * fila de "baixa" também recebia eventos de "alta" e vice-versa — e, como o
 * Jackson 3 ignora campos desconhecidos, o evento do tipo errado era lido com
 * o estoque faltando = 0, gerando "eventos fantasma". Com routing key, cada
 * evento é entregue a exatamente UMA fila, então dá para manter duas filas
 * distintas sem fantasma:
 *
 *   estoque.baixa  ->  estoque-baixa.ws  (estoque diminuiu: esgotou ou caiu)
 *   estoque.alta   ->  estoque-alta.ws   (estoque aumentou: reestocou ou subiu)
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ESTOQUE = "estoque.events";

    public static final String FILA_ESTOQUE_BAIXA_WS = "estoque-baixa.ws";
    public static final String FILA_ESTOQUE_ALTA_WS = "estoque-alta.ws";

    public static final String RK_ESTOQUE_BAIXA = "estoque.baixa";
    public static final String RK_ESTOQUE_ALTA = "estoque.alta";

    @Bean
    public DirectExchange exchangeEstoque() {
        // durable=true: a exchange sobrevive a reinícios do broker.
        return new DirectExchange(EXCHANGE_ESTOQUE, true, false);
    }

    @Bean
    public Queue filaEstoqueBaixaWs() {
        // durable=true: a fila é recriada automaticamente após reinício do broker.
        return new Queue(FILA_ESTOQUE_BAIXA_WS, true);
    }

    @Bean
    public Queue filaEstoqueAltaWs() {
        return new Queue(FILA_ESTOQUE_ALTA_WS, true);
    }

    @Bean
    public Binding bindingEstoqueBaixaWs(
        Queue filaEstoqueBaixaWs,
        DirectExchange exchangeEstoque
    ) {
        return BindingBuilder.bind(filaEstoqueBaixaWs)
            .to(exchangeEstoque)
            .with(RK_ESTOQUE_BAIXA);
    }

    @Bean
    public Binding bindingEstoqueAltaWs(
        Queue filaEstoqueAltaWs,
        DirectExchange exchangeEstoque
    ) {
        return BindingBuilder.bind(filaEstoqueAltaWs)
            .to(exchangeEstoque)
            .with(RK_ESTOQUE_ALTA);
    }

    @Bean
    public MessageConverter jacksonJsonMessageConverter() {
        // Serializa/deserializa os records como JSON em vez de Serializable
        // nativo do Java — fica mais legível no management UI do RabbitMQ e
        // independente de classe entre produtor e consumidor.
        return new JacksonJsonMessageConverter();
    }
}
