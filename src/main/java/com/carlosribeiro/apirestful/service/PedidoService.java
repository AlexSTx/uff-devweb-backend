package com.carlosribeiro.apirestful.service;

import com.carlosribeiro.apirestful.auth.model.Usuario;
import com.carlosribeiro.apirestful.auth.repository.UsuarioRepository;
import com.carlosribeiro.apirestful.dto.ItemPedidoResponse;
import com.carlosribeiro.apirestful.dto.PedidoRequest;
import com.carlosribeiro.apirestful.dto.PedidoResponse;
import com.carlosribeiro.apirestful.exception.EstoqueInsuficienteException;
import com.carlosribeiro.apirestful.exception.EntidadeNaoEncontradaException;
import com.carlosribeiro.apirestful.messaging.EstoqueAltaEvent;
import com.carlosribeiro.apirestful.messaging.EstoqueBaixaEvent;
import com.carlosribeiro.apirestful.messaging.EstoqueEventProducer;
import com.carlosribeiro.apirestful.model.FormaPagamento;
import com.carlosribeiro.apirestful.model.ItemCarrinho;
import com.carlosribeiro.apirestful.model.ItemPedido;
import com.carlosribeiro.apirestful.model.Pedido;
import com.carlosribeiro.apirestful.model.Produto;
import com.carlosribeiro.apirestful.model.StatusPedido;
import com.carlosribeiro.apirestful.repository.ItemCarrinhoRepository;
import com.carlosribeiro.apirestful.repository.PedidoRepository;
import com.carlosribeiro.apirestful.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueEventProducer estoqueEventProducer;

    public PedidoService(PedidoRepository pedidoRepository,
                         ItemCarrinhoRepository itemCarrinhoRepository,
                         UsuarioRepository usuarioRepository,
                         ProdutoRepository produtoRepository,
                         EstoqueEventProducer estoqueEventProducer) {
        this.pedidoRepository = pedidoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueEventProducer = estoqueEventProducer;
    }

    public PedidoResponse criarPedido(PedidoRequest request) {
        Long usuarioId = getUsuarioIdAutenticado();
        List<ItemCarrinho> itensCarrinho = itemCarrinhoRepository.findByUsuarioId(usuarioId);
        if (itensCarrinho.isEmpty()) {
            throw new IllegalStateException("Não é possível criar um pedido com o carrinho vazio.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Usuário com id = " + usuarioId + " não encontrado."));

        // ── 1. Pessimistic lock + validação ──────────────────────────────
        // Travar cada produto ANTES de qualquer mutação, para evitar condição
        // de corrida (dois checkouts lendo o mesmo estoque). Locks são
        // liberados no commit/rollback da transação @Transactional.
        List<Produto> produtosTravados = new ArrayList<>();
        List<EstoqueInsuficienteException.ItemProblema> problemas = new ArrayList<>();
        for (ItemCarrinho itemCarrinho : itensCarrinho) {
            int quantidade = itemCarrinho.getQuantidade();
            Long produtoId = itemCarrinho.getProduto().getId();

            // SELECT ... FOR UPDATE — bloqueia a linha até o fim da tx.
            Produto produto = produtoRepository.findByIdForUpdate(produtoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                    "Produto com id = " + produtoId + " não encontrado."));

            // Validação: a quantidade pedida não pode exceder o estoque físico
            // atual. (O estoque visível já é o físico — a reserva de pedidos
            // anteriores não conta como disponível porque já foiBaixada no
            // estoque físico na criação deles.)
            if (produto.getQtdEstoque() < quantidade) {
                problemas.add(new EstoqueInsuficienteException.ItemProblema(
                    produtoId,
                    produto.getNome(),
                    quantidade,
                    produto.getQtdEstoque()));
            }
            produtosTravados.add(produto);
        }

        // Se algum item falhou, NÃOONEY muta o estoque: aborta aqui.
        if (!problemas.isEmpty()) {
            throw new EstoqueInsuficienteException(problemas);
        }

        Pedido pedido = new Pedido(
            usuario,
            LocalDateTime.now(),
            BigDecimal.ZERO,
            StatusPedido.PENDENTE,
            request.formaPagamento());

        // ── 2. Mutação (estoque já validado e travado) ────────────────────
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (int i = 0; i < itensCarrinho.size(); i++) {
            ItemCarrinho itemCarrinho = itensCarrinho.get(i);
            Produto produto = produtosTravados.get(i);
            int quantidade = itemCarrinho.getQuantidade();

            // Reserva a quantidade comprada no estoque do produto: decrementa
            // o estoque físico e incrementa a quantidade reservada.
            produto.setQtdEstoque(produto.getQtdEstoque() - quantidade);
            produto.setQtdReservado(produto.getQtdReservado() + quantidade);
            produtoRepository.save(produto);

            // O estoque físico visível aos demais clientes já caiu aqui (na
            // criação do pedido, não no pagamento — senão dois clientes
            // reservariam a mesma última unidade). Publicamos a baixa a CADA
            // decremento, mesmo parcial (ex.: 10 -> 7), e não só quando zera:
            // assim o carrinho/checkout de outros usuários reavaliam em tempo
            // real se a quantidade deles ainda cabe no novo estoque (e, se não
            // couber, o frontend pede para diminuir).
            estoqueEventProducer.publicarEstoqueBaixa(new EstoqueBaixaEvent(
                produto.getId(),
                produto.getNome(),
                produto.getQtdEstoque(),
                LocalDateTime.now()
            ));

            ItemPedido itemPedido = new ItemPedido(
                pedido,
                produto,
                itemCarrinho.getPreco(),
                quantidade);
            pedido.getItens().add(itemPedido);
            valorTotal = valorTotal.add(
                itemCarrinho.getPreco().multiply(BigDecimal.valueOf(quantidade)));
        }
        pedido.setValorTotal(valorTotal);

        pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    public PedidoResponse pagarPedido(Long id) {
        Long usuarioId = getUsuarioIdAutenticado();
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Pedido com id = " + id + " não encontrado."));
        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new EntidadeNaoEncontradaException(
                "Pedido com id = " + id + " não encontrado.");
        }
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalStateException(
                "Pedido com id = " + id + " não está pendente (status atual: " + pedido.getStatus() + ").");
        }

        // Simula o processamento do pagamento pelo gateway.
        try {
            Thread.sleep(10_000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pedido.setStatus(StatusPedido.PAGO);
        pedidoRepository.save(pedido);

        // Uma vez pago, o carrinho do usuário é esvaziado.
        itemCarrinhoRepository.deleteByUsuarioId(usuarioId);

        // Ao confirmar o pagamento, a quantidade reservada deixa de ser
        // "reservada" e passa a ser simplesmente consumida do estoque: o
        // estoque físico já foi decrementado na criação do pedido, então
        // basta zerar a reserva, já que o produto foi efetivamente vendido.
        // Não republicamos evento de estoque aqui: a baixa já foi emitida na
        // criação do pedido, que é quando o estoque visível aos clientes caiu.
        // No pagamento só zeramos a reserva (o estoque físico não muda).
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setQtdReservado(produto.getQtdReservado() - item.getQuantidade());
            produtoRepository.save(produto);
        }

        return toResponse(pedido);
    }

    public PedidoResponse cancelarPedido(Long id) {
        Long usuarioId = getUsuarioIdAutenticado();
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Pedido com id = " + id + " não encontrado."));
        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new EntidadeNaoEncontradaException(
                "Pedido com id = " + id + " não encontrado.");
        }
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalStateException(
                "Pedido com id = " + id + " não está pendente (status atual: " + pedido.getStatus() + ").");
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        restaurarEstoque(pedido);
        pedidoRepository.save(pedido);
        return toResponse(pedido);
    }

    /**
     * Cancela os pedidos pendentes criados há mais de 10 minutos.
     * Executado periodicamente pelo PedidoScheduler.
     */
    public int cancelarPedidosExpirados() {
        LocalDateTime prazo = LocalDateTime.now().minusMinutes(10);
        List<Pedido> expirados = pedidoRepository
            .findByStatusAndDataPedidoBefore(StatusPedido.PENDENTE, prazo);
        for (Pedido pedido : expirados) {
            pedido.setStatus(StatusPedido.CANCELADO);
            restaurarEstoque(pedido);
            pedidoRepository.save(pedido);
        }
        return expirados.size();
    }

    private void restaurarEstoque(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            int quantidade = item.getQuantidade();
            // O estoque físico tinha sido decrementado na criação do pedido
            // e a reserva incrementada. Ao cancelar, devolvemos ambos.
            int estoqueAntes = produto.getQtdEstoque();
            produto.setQtdEstoque(estoqueAntes + quantidade);
            produto.setQtdReservado(produto.getQtdReservado() - quantidade);
            produtoRepository.save(produto);

            // Devolvemos estoque a CADA item (mesmo aumento parcial), avisando
            // em tempo real para destravar o botão "Esgotado" e atualizar os
            // limites de quantidade no carrinho/checkout de quem estava
            // esperando. Sem unidades devolvidas não há mudança a comunicar.
            if (quantidade > 0) {
                estoqueEventProducer.publicarEstoqueAlta(new EstoqueAltaEvent(
                    produto.getId(),
                    produto.getNome(),
                    produto.getQtdEstoque(),
                    LocalDateTime.now()
                ));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> recuperarPedidos() {
        Long usuarioId = getUsuarioIdAutenticado();
        return pedidoRepository.findByUsuarioIdOrderByDataPedidoDesc(usuarioId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse recuperarPedidoPorId(Long id) {
        Long usuarioId = getUsuarioIdAutenticado();
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Pedido com id = " + id + " não encontrado."));
        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new EntidadeNaoEncontradaException(
                "Pedido com id = " + id + " não encontrado.");
        }
        return toResponse(pedido);
    }

    private Long getUsuarioIdAutenticado() {
        return (Long) org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
            .map(i -> new ItemPedidoResponse(
                i.getId(),
                i.getProduto().getId(),
                i.getProduto().getNome(),
                i.getProduto().getImagem(),
                i.getPrecoUnitario(),
                i.getQuantidade(),
                i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))))
            .toList();
        return new PedidoResponse(
            pedido.getId(),
            pedido.getDataPedido(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getFormaPagamento(),
            itens
        );
    }
}
