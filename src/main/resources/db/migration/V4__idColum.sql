-- Primärschlüssel entfernen
ALTER TABLE user_roles DROP CONSTRAINT user_roles_pkey;

-- Neue ID-Spalte als Primärschlüssel hinzufügen
ALTER TABLE user_roles ADD COLUMN id SERIAL PRIMARY KEY;

DELETE FROM flyway_schema_history WHERE version = '4';
