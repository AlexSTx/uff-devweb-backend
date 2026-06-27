package com.carlosribeiro.apirestful.service;

import com.carlosribeiro.apirestful.auth.model.Usuario;
import com.carlosribeiro.apirestful.auth.repository.UsuarioRepository;
import com.carlosribeiro.apirestful.dto.ItemCarrinhoRequest;
import com.carlosribeiro.apirestful.dto.ItemCarrinhoResponse;
import com.carlosribeiro.apirestful.exception.EntidadeNaoEncontradaException;
import com.carlosribeiro.apirestful.model.ItemCarrinho;
import com.carlosribeiro.apirestful.model.Produto;
import com.carlosribeiro.apirestful.repository.ItemCarrinhoRepository;
import com.carlosribeiro.apirestful.repository.ProdutoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarrinhoService {

    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarrinhoService(ItemCarrinhoRepository itemCarrinhoRepository,
                           ProdutoRepository produtoRepository,
                           UsuarioRepository usuarioRepository) {
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ItemCarrinhoResponse> recuperarCarrinho() {
        Long usuarioId = getUsuarioIdAutenticado();
        return itemCarrinhoRepository.findByUsuarioId(usuarioId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public ItemCarrinhoResponse adicionarItem(ItemCarrinhoRequest request) {
        Long usuarioId = getUsuarioIdAutenticado();
        Produto produto = produtoRepository.findById(request.produtoId())
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Produto com id = " + request.produtoId() + " não encontrado."));

        // Se já existe um item deste produto no carrinho, somamos as quantidades
        // e mantemos o preço registrado no momento da primeira adição.
        Optional<ItemCarrinho> existente = itemCarrinhoRepository
            .findByUsuarioIdAndProdutoId(usuarioId, request.produtoId());
        ItemCarrinho item;
        if (existente.isPresent()) {
            item = existente.get();
            item.setQuantidade(item.getQuantidade() + request.quantidade());
        } else {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                    "Usuário com id = " + usuarioId + " não encontrado."));
            item = new ItemCarrinho(
                usuario,
                produto,
                produto.getPreco(),      // preço no momento da adição
                request.quantidade());
            item.setDataAdicao(LocalDateTime.now());
        }
        itemCarrinhoRepository.save(item);
        return toResponse(item);
    }

    public ItemCarrinhoResponse alterarQuantidade(Long itemId, int novaQuantidade) {
        Long usuarioId = getUsuarioIdAutenticado();
        ItemCarrinho item = itemCarrinhoRepository.findById(itemId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Item de carrinho com id = " + itemId + " não encontrado."));
        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw new EntidadeNaoEncontradaException(
                "Item de carrinho com id = " + itemId + " não encontrado.");
        }
        item.setQuantidade(novaQuantidade);
        itemCarrinhoRepository.save(item);
        return toResponse(item);
    }

    public void removerItem(Long itemId) {
        Long usuarioId = getUsuarioIdAutenticado();
        ItemCarrinho item = itemCarrinhoRepository.findById(itemId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Item de carrinho com id = " + itemId + " não encontrado."));
        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw new EntidadeNaoEncontradaException(
                "Item de carrinho com id = " + itemId + " não encontrado.");
        }
        itemCarrinhoRepository.deleteById(itemId);
    }

    public void limparCarrinho() {
        Long usuarioId = getUsuarioIdAutenticado();
        itemCarrinhoRepository.deleteByUsuarioId(usuarioId);
    }

    private Long getUsuarioIdAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }

    private ItemCarrinhoResponse toResponse(ItemCarrinho item) {
        Produto p = item.getProduto();
        BigDecimal subtotal = item.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
        return new ItemCarrinhoResponse(
            item.getId(),
            p.getId(),
            p.getNome(),
            p.getDescricao(),
            p.getImagem(),
            item.getPreco(),
            item.getQuantidade(),
            subtotal,
            item.getDataAdicao()
        );
    }
}