package com.carlosribeiro.apirestful.controller;

import com.carlosribeiro.apirestful.dto.PedidoRequest;
import com.carlosribeiro.apirestful.dto.PedidoResponse;
import com.carlosribeiro.apirestful.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:5173")
@RestController
@RequestMapping("pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public PedidoResponse criarPedido(@RequestBody @Valid PedidoRequest request) {
        return pedidoService.criarPedido(request);
    }

    @PostMapping("{id}/pagar")
    public PedidoResponse pagarPedido(@PathVariable("id") long id) {
        return pedidoService.pagarPedido(id);
    }

    @PostMapping("{id}/cancelar")
    public PedidoResponse cancelarPedido(@PathVariable("id") long id) {
        return pedidoService.cancelarPedido(id);
    }

    @GetMapping
    public List<PedidoResponse> recuperarPedidos() {
        return pedidoService.recuperarPedidos();
    }

    @GetMapping("{id}")
    public PedidoResponse recuperarPedidoPorId(@PathVariable("id") long id) {
        return pedidoService.recuperarPedidoPorId(id);
    }
}
