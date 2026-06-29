package com.carlosribeiro.apirestful.service;

import com.carlosribeiro.apirestful.dto.ProdutoRequest;
import com.carlosribeiro.apirestful.dto.ProdutoResponse;
import com.carlosribeiro.apirestful.exception.EntidadeNaoEncontradaException;
import com.carlosribeiro.apirestful.mapper.ProdutoMapper;
import com.carlosribeiro.apirestful.model.ItemPedido;
import com.carlosribeiro.apirestful.model.Pedido;
import com.carlosribeiro.apirestful.model.Produto;
import com.carlosribeiro.apirestful.repository.ItemCarrinhoRepository;
import com.carlosribeiro.apirestful.repository.ItemPedidoRepository;
import com.carlosribeiro.apirestful.repository.PedidoRepository;
import com.carlosribeiro.apirestful.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProdutoService {
    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoMapper produtoMapper;

    @Autowired
    private ItemCarrinhoRepository itemCarrinhoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<ProdutoResponse> recuperarProdutos() {
        List<Produto> produtos = produtoRepository.recuperarProduos();
        return produtoMapper.toProdutosResponse(produtos);
    }

    public ProdutoResponse recuperarProdutoPorId(long id) {
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException(
                "Produto com id = " + id + " não encontrado."));
        return produtoMapper.toProdutoResponse(produto);
    }

    public ProdutoResponse cadastrarProduto(ProdutoRequest produtoRequest) {
        Produto produto = produtoMapper.toProduto(produtoRequest);
        produto.setDataCadastro(LocalDate.now());
        produto = produtoRepository.save(produto);
        return produtoMapper.toProdutoResponse(produto);
    }

    public ProdutoResponse alterarProduto(ProdutoRequest produtoRequest) {
        Produto produto = produtoMapper.toProduto(produtoRequest);
        produtoRepository.findById(produto.getId())
            .ifPresent(existing -> produto.setDataCadastro(existing.getDataCadastro()));
        Produto salvo = produtoRepository.save(produto);
        return produtoMapper.toProdutoResponse(salvo);
    }

    @Transactional
    public void removerProdutoPorId(long id) {
        recuperarProdutoPorId(id); // garante 404 se o produto não existir

        // Exclusão em cascata, mantendo os pedidos consistentes:
        // 1) tira o produto de todos os carrinhos;
        itemCarrinhoRepository.deleteByProdutoId(id);

        // 2) remove o produto dos pedidos. Para cada pedido afetado: tira o(s)
        //    item(ns) desse produto (orphanRemoval apaga as linhas); se o pedido
        //    ficar vazio, apaga o pedido; senão, recalcula o valorTotal com os
        //    itens restantes (para o total não ficar "inflado" pelo item removido).
        List<ItemPedido> itensDoProduto = itemPedidoRepository.findByProdutoId(id);
        Set<Pedido> pedidosAfetados = new LinkedHashSet<>();
        for (ItemPedido item : itensDoProduto) {
            pedidosAfetados.add(item.getPedido());
        }
        for (Pedido pedido : pedidosAfetados) {
            pedido.getItens().removeIf(item -> item.getProduto().getId().equals(id));
            if (pedido.getItens().isEmpty()) {
                pedidoRepository.delete(pedido);
            } else {
                BigDecimal novoTotal = pedido.getItens().stream()
                    .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                pedido.setValorTotal(novoTotal);
                pedidoRepository.save(pedido);
            }
        }
        // Força a remoção dos item_pedido / pedidos vazios ANTES de apagar o
        // produto, senão a FK item_pedido -> produto bloquearia o DELETE.
        pedidoRepository.flush();

        // 3) finalmente, apaga o produto.
        produtoRepository.deleteById(id);
    }

    // Sobrecarga com filtro opcional por categoria.
    // Quando categoriaId é null, cai no método original (todas as categorias).
    // Quando não é null, chama a sobrecarga do repository que adiciona o
    // where por categoria.id — mantendo a paginação do lado do banco.
    public Page<ProdutoResponse> recuperarProdutosComPaginacao(PageRequest pageRequest, String nome, Long categoriaId) {
        Page<Produto> page = categoriaId == null
            ? produtoRepository.recuperarProdutosComPaginacao(pageRequest, "%" + nome + "%")
            : produtoRepository.recuperarProdutosComPaginacaoECategoria(pageRequest, "%" + nome + "%", categoriaId);
        return page.map((produto) -> produtoMapper.toProdutoResponse(produto));
    }
}
