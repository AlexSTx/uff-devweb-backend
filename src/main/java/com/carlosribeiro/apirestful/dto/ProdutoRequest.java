package com.carlosribeiro.apirestful.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(
    @Null(groups = OnCreate.class, message = "O 'Id' deve ser nulo no cadastro.")
    @NotNull(groups = OnUpdate.class, message = "O 'Id' deve ser informado na alteração.")
    Long id,
    String imagem,
    String nome,
    String descricao,
    boolean disponivel,
    Integer qtdEstoque,
    BigDecimal preco,
    LocalDate dataCadastro,
    @JsonProperty("categoria")
    CategoriaResumo categoriaResumo
) {
    // Oncreate e OnUpdate são duas interfaces marcadoras (marker interface) para grupos de
    // validação do Bean Validation.
    // Em ProdutoRequest a interface OnCreate (que não tem métodos de propósito) apenas serve
    // como “rótulo” para dizer quais regras devem rodar no cenário de criação.
    // No campo id de ProdutoRequest existem regras por grupo
    // @Null(groups = OnCreate.class) no POST
    // @NotNull(groups = OnUpdate.class) no PUT
    // No controller os grupos de validação são ativados com @Validated:
    // POST ativa OnCreate em ProdutoController
    // PUT ativa OnUpdate em ProdutoController

    public interface OnCreate {}
    public interface OnUpdate {}
}