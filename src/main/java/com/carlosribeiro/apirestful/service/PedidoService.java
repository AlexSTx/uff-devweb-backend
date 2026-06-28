package com.carlosribeiro.apirestful.service;

import com.carlosribeiro.apirestful.auth.model.Usuario;
import com.carlosribeiro.apirestful.auth.repository.UsuarioRepository;
import com.carlosribeiro.apirestful.dto.ItemPedidoResponse;
import com.carlosribeiro.apirestful.dto.PedidoRequest;
import com.carlosribeiro.apirestful.dto.PedidoResponse;
import com.carlosribeiro.apirestful.exception.EntidadeNaoEncontradaException;
import com.carlosribeiro.apirestful.messaging.EstoqueEsgotadoEvent;
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

        Pedido pedido = new Pedido(
            usuario,
            LocalDateTime.now(),
            BigDecimal.ZERO,
            StatusPedido.PENDENTE,
            request.formaPagamento());

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ItemCarrinho itemCarrinho : itensCarrinho) {
            int quantidade = itemCarrinho.getQuantidade();
            // Reserva a quantidade comprada no estoque do produto: decrementa
            // o estoque físico e incrementa a quantidade reservada.
            Produto produto = itemCarrinho.getProduto();
            produto.setQtdEstoque(produto.getQtdEstoque() - quantidade);
            produto.setQtdReservado(produto.getQtdReservado() + quantidade);
            produtoRepository.save(produto);

            // O estoque físico visível aos demais clientes (estoque - reserva)
            // já é atualizado aqui. Se zerou, é neste momento que o produto
            // deixa de ser comprável - não no pagamento. Caso contrário, dois
            // clientes poderiam reservar a mesma última unidade.
            if (produto.getQtdEstoque() == 0) {
                estoqueEventProducer.publicarEstoqueEsgotado(new EstoqueEsgotadoEvent(
                    produto.getId(),
                    produto.getNome(),
                    0,
                    LocalDateTime.now()
                ));
            }

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
        // Não republicamos EstoqueEsgotadoEvent aqui: o evento foi emitido
        // (se aplicável) na criação do pedido, que é quando o estoque
        // visível aos clientes zerou.
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
            produto.setQtdEstoque(produto.getQtdEstoque() + quantidade);
            produto.setQtdReservado(produto.getQtdReservado() - quantidade);
            produtoRepository.save(produto);
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
