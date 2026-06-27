package com.carlosribeiro.apirestful.model;

import com.carlosribeiro.apirestful.auth.model.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class ItemCarrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private BigDecimal preco;

    private LocalDateTime dataAdicao;

    private int quantidade;

    public ItemCarrinho(Usuario usuario, Produto produto, BigDecimal preco, int quantidade) {
        this.usuario = usuario;
        this.produto = produto;
        this.preco = preco;
        this.quantidade = quantidade;
    }
}