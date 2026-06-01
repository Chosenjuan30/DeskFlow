-- Phase 1: users + agent_profiles tables

CREATE TABLE users
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);

CREATE TABLE agent_profiles
(
    id           UUID    NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID    NOT NULL,
    current_load INT     NOT NULL DEFAULT 0,
    available    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    CONSTRAINT pk_agent_profiles PRIMARY KEY (id),
    CONSTRAINT uq_agent_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_agent_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE agent_profile_expertise
(
    agent_profile_id UUID         NOT NULL,
    expertise        VARCHAR(100) NOT NULL,
    CONSTRAINT fk_expertise_profile FOREIGN KEY (agent_profile_id)
        REFERENCES agent_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_agent_expertise ON agent_profile_expertise (expertise);