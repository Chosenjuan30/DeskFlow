package com.deskflow.module.ticket.dto;

import com.deskflow.module.ticket.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull TicketStatus newStatus,
        String comment
) {}
