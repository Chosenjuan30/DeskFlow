-- PostgreSQL initialisation script
-- Runs once when the container is first created.
-- Flyway owns all schema DDL — this file only handles DB-level setup.

-- Ensure UUID extension is available (used by all entities)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the application schema (Flyway will create all tables inside it)
-- The default schema is 'public'; no additional schema needed for now.

-- Grant all privileges to the application user
GRANT ALL PRIVILEGES ON DATABASE deskflow TO deskflow;