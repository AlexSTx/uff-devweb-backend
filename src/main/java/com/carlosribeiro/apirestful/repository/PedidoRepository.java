package com.carlosribeiro.apirestful.repository;

import com.carlosribeiro.apirestful.model.Pedido;
import com.carlosribeiro.apirestful.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByDataPedidoDesc(Long usuarioId);

    // Pedidos pendentes criados antes do instante dado (usado para expirar).
    List<Pedido> findByStatusAndDataPedidoBefore(StatusPedido status, LocalDateTime dataPedido);
}
