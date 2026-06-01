package com.deskflow.module.ticket.domain;

import java.util.Map;
import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    PENDING_CUSTOMER,
    RESOLVED,
    CLOSED;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = Map.of(
            OPEN,             Set.of(IN_PROGRESS),
            IN_PROGRESS,      Set.of(PENDING_CUSTOMER, RESOLVED),
            PENDING_CUSTOMER, Set.of(IN_PROGRESS),
            RESOLVED,         Set.of(CLOSED),
            CLOSED,           Set.of()
    );

    public boolean canTransitionTo(TicketStatus next) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(next);
    }
}
