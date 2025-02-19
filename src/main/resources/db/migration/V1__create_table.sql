CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usr (
                        id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        phone VARCHAR(20),
                        password VARCHAR(255) NOT NULL
);

CREATE TABLE role (
                      id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL
);


CREATE TABLE user_role (
                           usr_id UUID NOT NULL,
                           role_id UUID NOT NULL,
                           PRIMARY KEY (usr_id, role_id),
                           FOREIGN KEY (usr_id) REFERENCES usr(id),
                           FOREIGN KEY (role_id) REFERENCES role(id)
);
