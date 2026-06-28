-- ============================================================
-- V3__criar_tabelas_pedido.sql
-- ============================================================
-- Pedidos (compras finalizados) e seus itens.
-- Cada pedido pertence a um usuário, possui um status
-- (PENDENTE, PAGO, ENVIADO, ENTREGUE, CANCELADO) e guarda o
-- valor total. Os itens preservam o preço unitário no momento
-- da compra, para não mudar caso o preço do produto seja
-- alterado depois.

CREATE TABLE pedido (
    id          bigint        NOT NULL AUTO_INCREMENT,
    usuario_id  bigint        DEFAULT NULL,
    data_pedido datetime      DEFAULT NULL,
    valor_total decimal(38,2) DEFAULT NULL,
    status      varchar(255)  DEFAULT NULL,
    PRIMARY KEY (id),
    KEY FK_PEDIDO_USUARIO (usuario_id),
    CONSTRAINT FK_PEDIDO_USUARIO
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE item_pedido (
    id             bigint        NOT NULL AUTO_INCREMENT,
    pedido_id      bigint        DEFAULT NULL,
    produto_id     bigint        DEFAULT NULL,
    preco_unitario decimal(38,2) DEFAULT NULL,
    quantidade     int           NOT NULL,
    PRIMARY KEY (id),
    KEY FK_ITEM_PEDIDO_PEDIDO (pedido_id),
    KEY FK_ITEM_PEDIDO_PRODUTO (produto_id),
    CONSTRAINT FK_ITEM_PEDIDO_PEDIDO
        FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT FK_ITEM_PEDIDO_PRODUTO
        FOREIGN KEY (produto_id) REFERENCES produto (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
