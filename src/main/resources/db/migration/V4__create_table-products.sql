CREATE TABLE TB_PRODUCTS
(
    id           UUID      DEFAULT gen_random_uuid() PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    active       BOOLEAN   DEFAULT TRUE,
    quantity     INT          NOT NULL,
    description  VARCHAR(255) NOT NULL,
    price        DECIMAL      NOT NULL,
    rating       DECIMAL NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TB_PRODUCT_IMAGES
(
    id            SERIAL PRIMARY KEY,
    product_id    UUID         NOT NULL,
    default_image BOOLEAN DEFAULT FALSE,
    path_url      VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES TB_PRODUCTS (id) ON DELETE CASCADE
);
