CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE "user" (
                        id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        phone VARCHAR(20),
                        password VARCHAR(255) NOT NULL
);

CREATE TABLE roles (
                      id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                      name VARCHAR(50) NOT NULL
);

CREATE TABLE user_roles (
                            user_id UUID REFERENCES "user"(id),
                            role_id UUID REFERENCES role(id),
                            PRIMARY KEY (user_id, role_id)
);
