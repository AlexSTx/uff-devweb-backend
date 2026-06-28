-- ============================================================
-- V6__reativacao_kachow.sql  —  Re-tematizacao Kachow! (hardware)
-- ============================================================
-- Troca as 3 categorias de hortifruti por categorias de hardware
-- e substitui os 12 produtos de demonstracao por 45 pecas de PC
-- (Processadores, Placas de Video e Memorias RAM).
--
-- Mantem os mesmos IDs de categoria (1, 2, 3) para nao quebrar
-- referencias existentes em pedidos/carrinhos ja criados. Os IDs
-- de produto antigos (1..12) sao removidos; os novos comecam em 1.
--
-- As imagens sao apenas nomes de arquivo (PNG). Os arquivos deve-
-- rao ser colocados no /public do frontend com os mesmos nomes.
-- ============================================================

-- ----------------------------- Categorias (renomeia as 3 existentes)
UPDATE categoria SET nome = 'Processador'   WHERE id = 1;
UPDATE categoria SET nome = 'Placa de Vídeo' WHERE id = 2;
UPDATE categoria SET nome = 'Memória RAM'   WHERE id = 3;

-- ----------------------------- Produtos (remove antigos e insere novos)
DELETE FROM produto;

INSERT INTO produto (id, imagem, nome, descricao, disponivel, qtd_estoque, preco, data_cadastro, categoria_id) VALUES
    -- Processadores (categoria 1)
    ( 1, 'ryzen-5-5600.png',          'Ryzen 5 5600',         '6 núcleos, 3.5GHz, AM4',          b'1',  50,  899.00, '2024-06-15', 1),
    ( 2, 'ryzen-5-7600.png',          'Ryzen 5 7600',         '6 núcleos, 3.8GHz, AM5',          b'1',  40, 1399.00, '2024-09-20', 1),
    ( 3, 'ryzen-7-5700x.png',         'Ryzen 7 5700X',        '8 núcleos, 3.4GHz, AM4',          b'1',  35, 1250.00, '2024-04-10', 1),
    ( 4, 'ryzen-7-7700x.png',         'Ryzen 7 7700X',        '8 núcleos, 4.5GHz, AM5',          b'1',  30, 1799.00, '2025-01-05', 1),
    ( 5, 'ryzen-9-7900x.png',         'Ryzen 9 7900X',        '12 núcleos, 4.7GHz, AM5',         b'1',  20, 2499.00, '2025-02-12', 1),
    ( 6, 'core-i5-12400f.png',        'Core i5-12400F',       '6 núcleos, 2.5GHz, LGA1700',      b'1',  45,  999.00, '2024-03-18', 1),
    ( 7, 'core-i5-13400f.png',        'Core i5-13400F',       '10 núcleos, 2.6GHz, LGA1700',     b'1',  40, 1299.00, '2024-08-22', 1),
    ( 8, 'core-i5-13600k.png',        'Core i5-13600K',       '14 núcleos, 3.5GHz, LGA1700',     b'1',  25, 1799.00, '2025-03-01', 1),
    ( 9, 'core-i7-12700k.png',        'Core i7-12700K',       '12 núcleos, 3.6GHz, LGA1700',     b'1',  30, 1999.00, '2024-05-14', 1),
    (10, 'core-i7-13700k.png',        'Core i7-13700K',       '16 núcleos, 3.4GHz, LGA1700',     b'1',  25, 2299.00, '2025-01-28', 1),
    (11, 'core-i7-14700k.png',        'Core i7-14700K',       '20 núcleos, 3.4GHz, LGA1700',     b'1',  20, 2599.00, '2025-04-03', 1),
    (12, 'core-i9-12900k.png',        'Core i9-12900K',       '16 núcleos, 3.2GHz, LGA1700',     b'1',  15, 2899.00, '2024-07-09', 1),
    (13, 'core-i9-13900k.png',        'Core i9-13900K',       '24 núcleos, 3.0GHz, LGA1700',     b'1',  10, 3299.00, '2025-02-25', 1),
    (14, 'core-i9-14900k.png',        'Core i9-14900K',       '24 núcleos, 3.2GHz, LGA1700',     b'1',  12, 3599.00, '2025-05-30', 1),
    (15, 'ryzen-9-5900x.png',         'Ryzen 9 5900X',        '12 núcleos, 3.7GHz, AM4',         b'1',  28, 1899.00, '2024-10-17', 1),

    -- Placas de Vídeo (categoria 2)
    (16, 'rtx-3050-8gb.png',          'RTX 3050 8GB',         'GDDR6, 8GB, PCIe 4.0',            b'1',  40, 1499.00, '2024-06-01', 2),
    (17, 'rtx-4060-8gb.png',          'RTX 4060 8GB',         'GDDR6, 8GB, PCIe 4.0',            b'1',  35, 1999.00, '2024-11-12', 2),
    (18, 'rtx-4060ti-16gb.png',       'RTX 4060 Ti 16GB',     'GDDR6, 16GB, PCIe 4.0',           b'1',  25, 2799.00, '2025-03-15', 2),
    (19, 'rtx-4070-12gb.png',         'RTX 4070 12GB',        'GDDR6X, 12GB, PCIe 4.0',          b'1',  20, 3499.00, '2025-01-19', 2),
    (20, 'rtx-4070-super-12gb.png',   'RTX 4070 Super 12GB',  'GDDR6X, 12GB, PCIe 4.0',          b'1',  18, 3999.00, '2025-04-22', 2),
    (21, 'rtx-4070ti-super-16gb.png', 'RTX 4070 Ti Super 16GB', 'GDDR6X, 16GB, PCIe 4.0',         b'1',  12, 5499.00, '2025-05-08', 2),
    (22, 'rtx-4080-super-16gb.png',   'RTX 4080 Super 16GB',  'GDDR6X, 16GB, PCIe 4.0',          b'1',  10, 6999.00, '2025-06-10', 2),
    (23, 'rtx-4090-24gb.png',         'RTX 4090 24GB',        'GDDR6X, 24GB, PCIe 4.0',           b'1',   5, 14999.00, '2025-02-28', 2),
    (24, 'rx-6600-8gb.png',           'RX 6600 8GB',          'GDDR6, 8GB, PCIe 4.0',            b'1',  45, 1299.00, '2024-07-04', 2),
    (25, 'rx-6700xt-12gb.png',        'RX 6700 XT 12GB',      'GDDR6, 12GB, PCIe 4.0',           b'1',  30, 1899.00, '2024-09-25', 2),
    (26, 'rx-7600-8gb.png',           'RX 7600 8GB',          'GDDR6, 8GB, PCIe 4.0',            b'1',  38, 1599.00, '2025-01-15', 2),
    (27, 'rx-7700xt-12gb.png',        'RX 7700 XT 12GB',      'GDDR6, 12GB, PCIe 4.0',           b'1',  22, 2599.00, '2025-03-29', 2),
    (28, 'rx-7900xtx-24gb.png',       'RX 7900 XTX 24GB',     'GDDR6, 24GB, PCIe 4.0',           b'1',   8, 6499.00, '2025-05-18', 2),
    (29, 'arc-a750-8gb.png',          'Arc A750 8GB',         'GDDR6, 8GB, PCIe 4.0',            b'1',  30, 1199.00, '2024-08-13', 2),
    (30, 'rtx-3060-12gb.png',         'RTX 3060 12GB',        'GDDR6, 12GB, PCIe 4.0',           b'0',   0, 1699.00, '2024-04-30', 2),

    -- Memórias RAM (categoria 3)
    (31, 'kingston-8gb-ddr4-3200.png',     'Kingston 8GB DDR4 3200',     '8GB, DDR4, 3200MHz',              b'1', 100,  189.00, '2024-03-05', 3),
    (32, 'kingston-16gb-ddr4-3200.png',    'Kingston 16GB DDR4 3200',    '16GB (2x8), DDR4, 3200MHz',       b'1',  80,  329.00, '2024-05-22', 3),
    (33, 'corsair-16gb-ddr4-3600.png',     'Corsair Vengeance 16GB DDR4 3600',  '16GB (2x8), DDR4, 3600MHz',  b'1',  75,  399.00, '2024-06-18', 3),
    (34, 'corsair-32gb-ddr4-3600.png',     'Corsair Vengeance 32GB DDR4 3600',  '32GB (2x16), DDR4, 3600MHz', b'1',  60,  699.00, '2024-09-30', 3),
    (35, 'gskill-16gb-ddr4-3600.png',       'G.Skill Trident Z 16GB DDR4 3600', '16GB (2x8), DDR4, 3600MHz RGB', b'1',  50,  449.00, '2024-11-08', 3),
    (36, 'gskill-32gb-ddr4-3600.png',       'G.Skill Trident Z 32GB DDR4 3600', '32GB (2x16), DDR4, 3600MHz RGB', b'1',  45,  799.00, '2025-02-14', 3),
    (37, 'kingston-16gb-ddr5-6000.png',     'Kingston 16GB DDR5 6000',     '16GB (2x8), DDR5, 6000MHz',       b'1',  55,  549.00, '2024-10-21', 3),
    (38, 'corsair-16gb-ddr5-6000.png',      'Corsair Vengeance 16GB DDR5 6000',  '16GB (2x8), DDR5, 6000MHz', b'1',  50,  599.00, '2024-12-03', 3),
    (39, 'corsair-32gb-ddr5-6000.png',      'Corsair Vengeance 32GB DDR5 6000',  '32GB (2x16), DDR5, 6000MHz', b'1',  40, 1099.00, '2025-01-26', 3),
    (40, 'gskill-32gb-ddr5-6000.png',       'G.Skill Trident Z5 32GB DDR5 6000', '32GB (2x16), DDR5, 6000MHz RGB', b'1',  35, 1299.00, '2025-04-11', 3),
    (41, 'gskill-64gb-ddr5-6000.png',       'G.Skill Trident Z5 64GB DDR5 6000', '64GB (2x32), DDR5, 6000MHz RGB', b'1',  20, 2499.00, '2025-05-25', 3),
    (42, 'corsair-32gb-ddr5-6400.png',      'Corsair Dominator 32GB DDR5 6400', '32GB (2x16), DDR5, 6400MHz', b'1',  25, 1499.00, '2025-03-07', 3),
    (43, 'corsair-64gb-ddr5-6400.png',      'Corsair Dominator 64GB DDR5 6400', '64GB (2x32), DDR5, 6400MHz', b'1',  15, 2899.00, '2025-06-15', 3),
    (44, 'hyperx-8gb-ddr4-2666.png',        'HyperX 8GB DDR4 2666',       '8GB, DDR4, 2666MHz',              b'1',  90,  159.00, '2024-02-19', 3),
    (45, 'crucial-16gb-ddr4-3200.png',      'Crucial 16GB DDR4 3200',     '16GB (2x8), DDR4, 3200MHz',       b'1',  70,  289.00, '2024-07-27', 3);