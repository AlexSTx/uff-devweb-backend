package com.carlosribeiro.apirestful.controller;

import com.carlosribeiro.apirestful.dto.ItemCarrinhoRequest;
import com.carlosribeiro.apirestful.dto.ItemCarrinhoResponse;
import com.carlosribeiro.apirestful.service.CarrinhoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:5173")
@RestController
@RequestMapping("carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @GetMapping
    public List<ItemCarrinhoResponse> recuperarCarrinho() {
        return carrinhoService.recuperarCarrinho();
    }

    @PostMapping
    public ItemCarrinhoResponse adicionarItem(@RequestBody @Valid ItemCarrinhoRequest request) {
        return carrinhoService.adicionarItem(request);
    }

    @PutMapping("{itemId}")
    public ItemCarrinhoResponse alterarQuantidade(@PathVariable("itemId") long itemId,
                                                  @RequestParam("quantidade") int quantidade) {
        return carrinhoService.alterarQuantidade(itemId, quantidade);
    }

    @DeleteMapping("{itemId}")
    public void removerItem(@PathVariable("itemId") long itemId) {
        carrinhoService.removerItem(itemId);
    }

    @DeleteMapping
    public void limparCarrinho() {
        carrinhoService.limparCarrinho();
    }
}