-- Datei: V3__add_password_email.sql
-- Fügt die Spalten 'email' zur Tabelle 'users' und 'password' zur Tabelle 'groups' hinzu

-- Überprüfen, ob die Spalte 'email' in 'users' existiert und falls nicht, hinzufügen
DO $$
    BEGIN
        IF NOT EXISTS (
                SELECT 1 FROM information_schema.columns
                WHERE table_name = 'users' AND column_name = 'email'
            ) THEN
            ALTER TABLE users ADD COLUMN email VARCHAR(255);
        END IF;
    END $$;

-- Überprüfen, ob die Spalte 'password' in 'groups' existiert und falls nicht, hinzufügen
DO $$
    BEGIN
        IF NOT EXISTS (
                SELECT 1 FROM information_schema.columns
                WHERE table_name = 'groups' AND column_name = 'password'
            ) THEN
            ALTER TABLE groups ADD COLUMN password VARCHAR(255) NOT NULL;
        END IF;
    END $$;
