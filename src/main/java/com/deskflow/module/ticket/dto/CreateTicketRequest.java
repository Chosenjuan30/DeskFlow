package com.deskflow.module.ticket.dto;

import com.deskflow.module.ticket.domain.TicketCategory;
import com.deskflow.module.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @NotNull TicketPriority priority,
        @NotNull TicketCategory category
) {}
