package com.carlosribeiro.apirestful.repository;

import com.carlosribeiro.apirestful.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByDataPedidoDesc(Long usuarioId);
}
