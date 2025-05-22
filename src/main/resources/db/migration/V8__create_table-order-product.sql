CREATE TABLE TB_ORDER_PRODUCT
(
    product_id UUID     NOT NULL,
    order_id   UUID     NOT NULL,
    quantity   SMALLINT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL, -- NOVO CAMPO ADICIONADO AQUI


    CONSTRAINT pk_order_product PRIMARY KEY (product_id, order_id),

    CONSTRAINT fk_order_product_product FOREIGN KEY (product_id)
        REFERENCES TB_PRODUCTS (id) ON DELETE CASCADE,

    CONSTRAINT fk_order_product_order FOREIGN KEY (order_id)
        REFERENCES TB_ORDER (id) ON DELETE CASCADE
);

