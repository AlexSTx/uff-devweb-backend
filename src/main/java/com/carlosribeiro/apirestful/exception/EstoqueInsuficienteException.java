package com.carlosribeiro.apirestful.exception;

import java.util.List;

/**
 * Lançada durante o checkout quando um ou mais itens do carrinho não podem
 * ser atendidos: produto esgotado (estoque físico = 0) ou quantidade pedida
 * maior que o estoque disponível. Carrega a lista dos itens problemáticos
 * para o cliente poder destacar cada um na UI.
 */
public class EstoqueInsuficienteException extends RuntimeException {

    private final List<ItemProblema> itens;

    public EstoqueInsuficienteException(List<ItemProblema> itens) {
        super("Estoque insuficiente para um ou mais itens do carrinho.");
        this.itens = itens;
    }

    public List<ItemProblema> getItens() {
        return itens;
    }

    /**
     * Resumo de um item com problema de estoque. IDs/nomes em PT-BR para
     * casar com o restante do domínio.
     */
    public record ItemProblema(
        Long produtoId,
        String nome,
        int quantidadePedida,
        int estoqueDisponivel
    ) {
    }
}