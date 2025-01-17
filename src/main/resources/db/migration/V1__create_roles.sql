-- Migration: Nötige Tabellen für die Datenbank erstellen
-- Datei: V1__create_schema.sql

-- Tabelle roles erstellen und Rollen direkt hinzufügen
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     roleName VARCHAR(255) UNIQUE NOT NULL
);

INSERT INTO roles (roleName) VALUES ('ADMIN')
ON CONFLICT (roleName) DO NOTHING;

-- Tabelle users erstellen
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL
);

-- Tabelle user_roles erstellen (Many-to-Many-Beziehung zwischen users und roles)
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          PRIMARY KEY (user_id, role_id),
                                          CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                                          CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Tabelle groups erstellen
CREATE TABLE IF NOT EXISTS groups (
                                      id BIGSERIAL PRIMARY KEY,
                                      groupName VARCHAR(255) UNIQUE NOT NULL,
                                      owner_id BIGINT NOT NULL
);

-- Tabelle group_users erstellen (Many-to-Many-Beziehung zwischen groups und users)
CREATE TABLE IF NOT EXISTS group_users (
                                           group_id BIGINT NOT NULL,
                                           user_id BIGINT NOT NULL,
                                           PRIMARY KEY (group_id, user_id),
                                           CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
                                           CONSTRAINT fk_user_group FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
