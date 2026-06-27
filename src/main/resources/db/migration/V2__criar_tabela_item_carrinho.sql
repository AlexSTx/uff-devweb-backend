-- ============================================================
-- V2__criar_tabela_item_carrinho.sql
-- ============================================================
-- Carrinho de compras persistido por usuário.
-- Cada linha guarda o produto, o preço no momento em que foi
-- adicionado (para não mudar caso o preço do produto seja alterado
-- depois), quantas unidades o usuário quer e quando o item entrou.

CREATE TABLE item_carrinho (
    id          bigint        NOT NULL AUTO_INCREMENT,
    usuario_id  bigint        NOT NULL,
    produto_id  bigint        NOT NULL,
    preco       decimal(38,2) NOT NULL,
    data_adicao datetime      NULL,
    quantidade  int           NOT NULL,
    PRIMARY KEY (id),
    KEY FK_ITEM_CARRINHO_USUARIO (usuario_id),
    KEY FK_ITEM_CARRINHO_PRODUTO (produto_id),
    CONSTRAINT FK_ITEM_CARRINHO_USUARIO
        FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT FK_ITEM_CARRINHO_PRODUTO
        FOREIGN KEY (produto_id) REFERENCES produto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;