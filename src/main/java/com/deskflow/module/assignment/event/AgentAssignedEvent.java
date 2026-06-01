package com.deskflow.module.assignment.event;

import com.deskflow.shared.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AgentAssignedEvent implements DomainEvent {

    private final UUID eventId     = UUID.randomUUID();
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID ticketId;
    private final String referenceNumber;
    private final UUID agentId;
    private final UUID assignedById; // null = auto-assignment

    public AgentAssignedEvent(UUID ticketId, String referenceNumber,
                              UUID agentId, UUID assignedById) {
        this.ticketId        = ticketId;
        this.referenceNumber = referenceNumber;
        this.agentId         = agentId;
        this.assignedById    = assignedById;
    }
}