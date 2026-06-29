package com.carlosribeiro.apirestful.repository;

import com.carlosribeiro.apirestful.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    // Usado na exclusão em cascata de um produto: descobre quais itens (e, por
    // eles, quais pedidos) referenciam o produto, para então remover os itens,
    // recalcular o total dos pedidos afetados e apagar os que ficarem vazios.
    List<ItemPedido> findByProdutoId(Long produtoId);
}
