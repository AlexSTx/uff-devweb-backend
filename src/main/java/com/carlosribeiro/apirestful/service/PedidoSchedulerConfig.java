package com.carlosribeiro.apirestful.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class PedidoSchedulerConfig {

    @Autowired
    private PedidoService pedidoService;

    // Roda a cada minuto verificando pedidos pendentes expirados.
    @Scheduled(fixedDelay = 60_000L)
    public void expirarPedidosPendentes() {
        int cancelados = pedidoService.cancelarPedidosExpirados();
        if (cancelados > 0) {
            System.out.println("Pedidos expirados cancelados automaticamente: " + cancelados);
        }
    }
}