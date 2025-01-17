-- Migration: Nötige Tabellen für die Datenbank erstellen
-- Datei: V2__create_schema_with_permissions.sql

-- Tabelle roles erstellen und Rollen direkt hinzufügen
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     roleName VARCHAR(255) UNIQUE NOT NULL
    );

INSERT INTO roles (roleName) VALUES ('BASIC_USER'), ('ADVANCED_USER'), ('ADMIN')
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

-- Tabelle permissions erstellen
CREATE TABLE IF NOT EXISTS permissions (
                                           id BIGSERIAL PRIMARY KEY,
                                           permissionName VARCHAR(255) UNIQUE NOT NULL
    );

-- Beispiel-Berechtigungen hinzufügen
INSERT INTO permissions (permissionName) VALUES
                                             ('CREATE_GROUP'),
                                             ('DELETE_GROUP'),
                                             ('ADD_USER_TO_GROUP'),
                                             ('REMOVE_USER_FROM_GROUP'),
                                             ('VIEW_ALL_GROUPS'),
                                             ('MANAGE_ROLES')
    ON CONFLICT (permissionName) DO NOTHING;

-- Tabelle role_permissions erstellen (Many-to-Many-Beziehung zwischen roles und permissions)
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id BIGINT NOT NULL,
                                                permission_id BIGINT NOT NULL,
                                                PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
    );

-- Berechtigungen Rollen zuweisen
-- BASIC_USER: Keine speziellen Berechtigungen
-- ADVANCED_USER: Kann Gruppen erstellen und bearbeiten
-- ADMIN: Kann alle Berechtigungen ausführen
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON
        (r.roleName = 'ADVANCED_USER' AND p.permissionName IN ('CREATE_GROUP', 'DELETE_GROUP', 'ADD_USER_TO_GROUP', 'REMOVE_USER_FROM_GROUP'))
        OR (r.roleName = 'ADMIN');

-- Tabelle groups erstellen
CREATE TABLE IF NOT EXISTS groups (
                                      id BIGSERIAL PRIMARY KEY,
                                      owner_id BIGINT NOT NULL,
                                      groupName VARCHAR(255) UNIQUE NOT NULL,
    CONSTRAINT fk_group_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
    );

-- Tabelle group_users erstellen (Many-to-Many-Beziehung zwischen groups und users)
CREATE TABLE IF NOT EXISTS group_users (
                                           group_id BIGINT NOT NULL,
                                           user_id BIGINT NOT NULL,
                                           PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_group FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    );
