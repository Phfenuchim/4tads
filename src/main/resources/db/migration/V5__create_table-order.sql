-- Tabela de métodos de pagamento
CREATE TABLE TB_PAYMENT_METHOD
(
    id           SMALLINT UNIQUE NOT NULL,
    payment_name VARCHAR(50)     NOT NULL
);

-- Dados iniciais para métodos de pagamento
INSERT INTO TB_PAYMENT_METHOD (id, payment_name)
VALUES (1, 'Crédito'),
       (2, 'Boleto');

-- Tabela de pedidos com os campos adicionais
CREATE TABLE TB_ORDER
(
    id                UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID           NOT NULL,
    total_price       DECIMAL(10, 2) NOT NULL,
    created_at        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    cep               VARCHAR(255)   NOT NULL,
    address           VARCHAR(255)   NOT NULL,
    address_number    VARCHAR(5)     NOT NULL,
    complement        VARCHAR(255),
    shipping          DECIMAL(10, 2) NOT NULL,
    payment_method_id SMALLINT       NULL,
    order_number      BIGINT         NOT NULL UNIQUE,  -- Campo faltante
    status            VARCHAR(50)    NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO', -- Campo faltante

    CONSTRAINT fk_payment_method FOREIGN KEY (payment_method_id)
        REFERENCES TB_PAYMENT_METHOD (id)
);
