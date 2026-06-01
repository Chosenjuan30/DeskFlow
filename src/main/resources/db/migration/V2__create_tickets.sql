-- Phase 2: tickets + ticket_status_updates tables

CREATE SEQUENCE ticket_ref_seq START 1;

CREATE TABLE tickets
(
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    reference_number  VARCHAR(20)  NOT NULL,
    title             VARCHAR(255) NOT NULL,
    description       TEXT         NOT NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    priority          VARCHAR(20)  NOT NULL,
    category          VARCHAR(50)  NOT NULL,
    customer_id       UUID         NOT NULL,
    assigned_agent_id UUID,
    resolved_at       TIMESTAMP,
    closed_at         TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    CONSTRAINT pk_tickets               PRIMARY KEY (id),
    CONSTRAINT uq_tickets_reference     UNIQUE (reference_number),
    CONSTRAINT fk_tickets_customer      FOREIGN KEY (customer_id)       REFERENCES users (id),
    CONSTRAINT fk_tickets_agent         FOREIGN KEY (assigned_agent_id) REFERENCES users (id)
);

CREATE INDEX idx_tickets_customer   ON tickets (customer_id);
CREATE INDEX idx_tickets_agent      ON tickets (assigned_agent_id);
CREATE INDEX idx_tickets_status     ON tickets (status);
CREATE INDEX idx_tickets_reference  ON tickets (reference_number);

CREATE TABLE ticket_status_updates
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    ticket_id   UUID        NOT NULL,
    old_status  VARCHAR(30),
    new_status  VARCHAR(30) NOT NULL,
    comment     TEXT,
    changed_by  UUID        NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    CONSTRAINT pk_ticket_status_updates PRIMARY KEY (id),
    CONSTRAINT fk_status_ticket FOREIGN KEY (ticket_id)  REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_user   FOREIGN KEY (changed_by) REFERENCES users   (id)
);

CREATE INDEX idx_status_updates_ticket ON ticket_status_updates (ticket_id);
