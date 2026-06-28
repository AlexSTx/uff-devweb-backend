-- ============================================================
-- V4__adicionar_forma_pagamento_pedido.sql
-- ============================================================
-- Adiciona a forma de pagamento escolhida no checkout ao pedido.
-- Valores: CARTAO_CREDITO, CARTAO_DEBITO, PIX, BOLETO.

ALTER TABLE pedido
    ADD COLUMN forma_pagamento varchar(255) DEFAULT NULL;
