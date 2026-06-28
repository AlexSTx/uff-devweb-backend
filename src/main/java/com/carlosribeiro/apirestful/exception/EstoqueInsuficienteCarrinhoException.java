package com.carlosribeiro.apirestful.exception;

import java.util.List;

/**
 * Lançada ao adicionar ou alterar um item do carrinho quando a quantidade
 * final no carrinho excederia o estoque físico atual do produto. Carrega a
 * lista dos itens problemáticos (em geral um único item, mas mantida como
 * lista para casar com o formato usado no checkout).
 */
public class EstoqueInsuficienteCarrinhoException extends RuntimeException {

    private final List<ItemProblema> itens;

    public EstoqueInsuficienteCarrinhoException(List<ItemProblema> itens) {
        super("Quantidade solicitada excede o estoque disponível do produto.");
        this.itens = itens;
    }

    public List<ItemProblema> getItens() {
        return itens;
    }

    public record ItemProblema(
        Long produtoId,
        String nome,
        int quantidadePedida,
        int estoqueDisponivel
    ) {
    }
}