CREATE TABLE TB_PRODUCTS
(
    id           UUID      DEFAULT gen_random_uuid() PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    active       BOOLEAN   DEFAULT TRUE,
    quantity     INT          NOT NULL,
    description TEXT NOT NULL CHECK (CHAR_LENGTH(description) <= 2000),
    price        DECIMAL(10,2)      NOT NULL,
    rating       DECIMAL(2,1) NULL CHECK (rating BETWEEN 1 AND 5),
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
