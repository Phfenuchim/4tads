-- Tabela CLIENTES
CREATE TABLE tb_client (
    id              UUID            PRIMARY KEY,
    firstName       VARCHAR(127)    NOT NULL,
    lastName        VARCHAR(127)    NOT NULL,
    fullname        VARCHAR(255)    NOT NULL,
    cpf             VARCHAR(14)     UNIQUE NOT NULL,
    email           VARCHAR(255)    UNIQUE NOT NULL,
    phone           VARCHAR(20),
    Password VARCHAR(20) NOT NULL,
    date_birth      DATE,
    status          BOOLEAN         DEFAULT TRUE,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Tabela ENDERECOS
CREATE TABLE tb_address (
    id          UUID            PRIMARY KEY,
    client_id  UUID            NOT NULL,
    tipo        VARCHAR(20)     CHECK (tipo IN ('faturamento', 'entrega')),
    cep         CHAR(8)         NOT NULL,
    street  VARCHAR(255)    NOT NULL,
    number      VARCHAR(10)     NOT NULL,
    complement VARCHAR(255),
    district      VARCHAR(100)    NOT NULL,
    city      VARCHAR(100)    NOT NULL,
    state      VARCHAR(100)    NOT NULL,
    country        VARCHAR(100)    NOT NULL,
    is_default  BOOLEAN         DEFAULT FALSE,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id)    REFERENCES tb_client(id)
);
