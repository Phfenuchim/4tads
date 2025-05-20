CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tb_users (
                        id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        cpf VARCHAR(14),
                        password VARCHAR(255) NOT NULL
);

CREATE TABLE tb_role (
                      id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL
);


CREATE TABLE tb_user_role (
                           user_id UUID NOT NULL,
                           role_id UUID NOT NULL,
                           PRIMARY KEY (user_id, role_id),
                           FOREIGN KEY (user_id) REFERENCES tb_users(id),
                           FOREIGN KEY (role_id) REFERENCES tb_role(id)
);
