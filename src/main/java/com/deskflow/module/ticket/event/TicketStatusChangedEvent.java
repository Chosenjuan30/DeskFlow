package com.deskflow.module.ticket.event;

import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.shared.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TicketStatusChangedEvent implements DomainEvent {

    private final UUID eventId    = UUID.randomUUID();
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID ticketId;
    private final String referenceNumber;
    private final TicketStatus oldStatus;
    private final TicketStatus newStatus;
    private final UUID changedBy;

    public TicketStatusChangedEvent(UUID ticketId, String referenceNumber,
                                    TicketStatus oldStatus, TicketStatus newStatus,
                                    UUID changedBy) {
        this.ticketId        = ticketId;
        this.referenceNumber = referenceNumber;
        this.oldStatus       = oldStatus;
        this.newStatus       = newStatus;
        this.changedBy       = changedBy;
    }
}
