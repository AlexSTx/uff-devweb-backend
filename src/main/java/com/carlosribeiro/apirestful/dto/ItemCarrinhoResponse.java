package com.carlosribeiro.apirestful.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemCarrinhoResponse(
    Long id,
    Long produtoId,
    String nome,
    String descricao,
    String imagem,
    BigDecimal precoUnitario,
    int quantidade,
    BigDecimal subtotal,
    LocalDateTime dataAdicao,
    // Indica se o produto ainda tem estoque físico no momento do GET /carrinho.
    // Quando false, o frontend mostra o item visualmente apagado e um aviso
    // de que ele esgotou desde a adição ao carrinho.
    boolean disponivel,
    // Estoque físico atual do produto no momento do GET. Permite ao frontend
    // detectar também o caso "parcial": produto ainda disponível mas com
    // quantidade menor do que a pedida no carrinho.
    int estoqueDisponivel
) {
}