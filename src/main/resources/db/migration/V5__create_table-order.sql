CREATE TABLE TB_PAYMENT_METHOD
(
    id            SMALLINT UNIQUE   NOT NULL,
    payment_name VARCHAR(50) NOT NULL

);

INSERT INTO TB_PAYMENT_METHOD (id, payment_name)
VALUES (1, 'Crédito'),
       (2, 'Débito'),
       (3, 'Pix');


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

    CONSTRAINT fk_payment_method FOREIGN KEY (payment_method_id)
        REFERENCES TB_PAYMENT_METHOD (id)
);
