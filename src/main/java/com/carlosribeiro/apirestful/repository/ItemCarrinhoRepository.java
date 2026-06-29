package com.carlosribeiro.apirestful.repository;

import com.carlosribeiro.apirestful.model.ItemCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {
    List<ItemCarrinho> findByUsuarioId(Long usuarioId);
    Optional<ItemCarrinho> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
    void deleteByUsuarioId(Long usuarioId);
    // Usado na exclusão em cascata de um produto: tira o produto de todos os
    // carrinhos antes de apagá-lo.
    void deleteByProdutoId(Long produtoId);
}