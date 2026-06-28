-- ============================================================
-- V5__adicionar_qtd_reservado_produto.sql
-- ============================================================
-- Quantidade de um produto que está reservada em pedidos
-- ainda não finalizados (ex.: pendentes/pagos mas não entregues).
-- Permite controlar a disponibilidade real sem alterar o
-- estoque físico até a entrega.

ALTER TABLE produto
    ADD COLUMN qtd_reservado int NOT NULL DEFAULT 0;