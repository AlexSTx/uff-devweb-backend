package com.carlosribeiro.apirestful.messaging;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Ponte entre o domínio (PedidoService) e o broker: o serviço publica os eventos
 * de estoque como ApplicationEvents do Spring DENTRO da transação, e este
 * forwarder só os envia ao RabbitMQ {@code AFTER_COMMIT}.
 *
 * Por que isso importa: o estoque físico é decrementado na MESMA transação que
 * cria o pedido. Se o evento fosse enviado ao broker antes do commit (como era
 * feito antes), a notificação WebSocket chegava aos outros clientes e o GET
 * /carrinho que eles disparavam em resposta ainda lia o estoque ANTIGO (a tx do
 * comprador não havia commitado, e a linha estava travada com SELECT ... FOR
 * UPDATE). Resultado: o carrinho não marcava "esgotado" — ou só marcava no
 * próximo refetch, com atraso. Adiar para AFTER_COMMIT garante leitura
 * consistente. Como bônus, um rollback deixa de gerar evento fantasma.
 */
@Component
public class EstoqueEventTransactionalForwarder {

    private final EstoqueEventProducer estoqueEventProducer;

    public EstoqueEventTransactionalForwarder(EstoqueEventProducer estoqueEventProducer) {
        this.estoqueEventProducer = estoqueEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConfirmarBaixa(EstoqueBaixaEvent evento) {
        estoqueEventProducer.publicarEstoqueBaixa(evento);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConfirmarAlta(EstoqueAltaEvent evento) {
        estoqueEventProducer.publicarEstoqueAlta(evento);
    }
}
