-- Phase 3: agent_assignments table

CREATE TABLE agent_assignments
(
    id          UUID      NOT NULL DEFAULT gen_random_uuid(),
    ticket_id   UUID      NOT NULL,
    agent_id    UUID      NOT NULL,
    assigned_by UUID,                          -- NULL = auto-assignment
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    CONSTRAINT pk_agent_assignments PRIMARY KEY (id),
    CONSTRAINT fk_assignment_ticket FOREIGN KEY (ticket_id)   REFERENCES tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_agent  FOREIGN KEY (agent_id)    REFERENCES users   (id),
    CONSTRAINT fk_assignment_by     FOREIGN KEY (assigned_by) REFERENCES users   (id)
);

CREATE INDEX idx_assignment_ticket ON agent_assignments (ticket_id);
CREATE INDEX idx_assignment_agent  ON agent_assignments (agent_id);