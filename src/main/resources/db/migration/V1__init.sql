-- ============================================================
-- V1__init.sql  —  Esquema inicial + dados de demonstracao
-- ============================================================
-- Gerenciado pelo Flyway. Hibernate esta com ddl-auto=validate,
-- ou seja, nao cria/dropa tabelas: apenas confere que este script
-- produziu um esquema compativel com as entidades JPA.
-- ------------------------------------------------------------
-- O esquema abaixo foi capturado do que o proprio Hibernate gerava
-- quando ddl-auto=create (tipos, tamanhos e nomes identicos), para
-- garantir que o validate nao reclame no startup.
-- ------------------------------------------------------------
-- Para evoluir o schema, crie arquivos V2__<descricao>.sql,
-- V3__<descricao>.sql, etc.  Nunca edite um migration ja aplicado.

-- ----------------------------- categoria
CREATE TABLE categoria (
    id   bigint       NOT NULL AUTO_INCREMENT,
    nome varchar(255) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------- produto
CREATE TABLE produto (
    id            bigint        NOT NULL AUTO_INCREMENT,
    imagem        varchar(255)  DEFAULT NULL,
    nome          varchar(255)  DEFAULT NULL,
    descricao     varchar(255)  DEFAULT NULL,
    disponivel    bit(1)        NOT NULL,
    qtd_estoque   int           NOT NULL,
    preco         decimal(38,2) DEFAULT NULL,
    data_cadastro date          DEFAULT NULL,
    categoria_id  bigint        DEFAULT NULL,
    PRIMARY KEY (id),
    KEY PRODUTO_CATEGORIA_CATEGORIA_ID_FK (categoria_id),
    CONSTRAINT PRODUTO_CATEGORIA_CATEGORIA_ID_FK
        FOREIGN KEY (categoria_id) REFERENCES categoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------- usuario
CREATE TABLE usuario (
    id    bigint       NOT NULL AUTO_INCREMENT,
    nome  varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    senha varchar(255) NOT NULL,
    role  enum('ADMIN','USER') DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK5171l57faosmj8myawaucatdw (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- Dados de demonstracao (categorias, produtos e usuarios)
-- ============================================================

INSERT INTO categoria (id, nome) VALUES
    (1, 'fruta'),
    (2, 'legume'),
    (3, 'verdura');

INSERT INTO produto (id, imagem, nome, descricao, disponivel, qtd_estoque, preco, data_cadastro, categoria_id) VALUES
    ( 1, 'abacate.png',    'Abacate',    '1 unidade aprox. 750g',   b'1', 100,  2.45,  '2025-04-26', 1),
    ( 2, 'abobrinha.png',  'Abobrinha',  '1 unidade aprox. 250g',   b'0', 500,  1.10,  '2025-05-22', 2),
    ( 3, 'abobora.png',    'Abóbora',    '1 unidade aprox. 1,9kg',  b'1', 400,  4.70,  '2025-03-24', 2),
    ( 4, 'acelga.png',     'Acelga',     '1 maço de aprox. 400g',   b'1', 120,  4.99,  '2025-03-12', 3),
    ( 5, 'agriao.png',     'Agrião',     '1 maço de aprox. 200g',   b'1', 340,  2.50,  '2025-05-17', 3),
    ( 6, 'alface.png',     'Alface',     '1 maço de aprox. 200g',   b'1', 220,  4.99,  '2023-05-14', 3),
    ( 7, 'banana.png',     'Banana',     '1 unidade aprox. 165g',   b'1', 350,  1.05,  '2023-02-22', 1),
    ( 8, 'berinjela.png',  'Berinjela',  '1 unidade aprox. 370g',   b'1', 720,  1.85,  '2023-02-23', 2),
    ( 9, 'brocolis.png',   'Brócolis',   '1 unidade aprox. 300g',   b'1', 600,  5.39,  '2023-03-28', 3),
    (10, 'cebola.png',     'Cebola',     '1 unidade aprox. 200g',   b'1',  95,  0.56,  '2023-04-30', 2),
    (11, 'cenoura.png',    'Cenoura',    '1 unidade aprox. 180g',   b'1', 350,  1.01,  '2023-05-29', 2),
    (12, 'cereja.png',     'Cereja',     '1 unidade aprox. 250g',   b'1', 240, 11.23,  '2023-05-11', 1);

-- Senhas ja em BCrypt (hash das credenciais de demonstracao).
INSERT INTO usuario (id, nome, email, senha, role) VALUES
    (1, 'Admin', 'admin@mail.com', '$2a$10$51QxpynyRq2.IjtA/5vCNuml2TMHEq3F/Gyl1dTXABzF4eqegAs7m', 'ADMIN'),
    (2, 'User',  'user@mail.com',  '$2a$10$gVjoY4Nm0YsuqfHZwdx6kOse9jyyz6VZG/MSKvDMw7Y.mWrqEQgeS', 'USER');