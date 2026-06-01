package com.deskflow.module.ticket.event;

import com.deskflow.module.ticket.domain.TicketCategory;
import com.deskflow.module.ticket.domain.TicketPriority;
import com.deskflow.shared.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TicketCreatedEvent implements DomainEvent {

    private final UUID eventId     = UUID.randomUUID();
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID ticketId;
    private final String referenceNumber;
    private final TicketPriority priority;
    private final TicketCategory category;
    private final UUID customerId;

    public TicketCreatedEvent(UUID ticketId, String referenceNumber,
                              TicketPriority priority, TicketCategory category,
                              UUID customerId) {
        this.ticketId        = ticketId;
        this.referenceNumber = referenceNumber;
        this.priority        = priority;
        this.category        = category;
        this.customerId      = customerId;
    }
}
