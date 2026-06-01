package com.deskflow.module.ticket.dto;

import com.deskflow.module.auth.dto.UserDto;
import com.deskflow.module.ticket.domain.Ticket;
import com.deskflow.module.ticket.domain.TicketCategory;
import com.deskflow.module.ticket.domain.TicketPriority;
import com.deskflow.module.ticket.domain.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketDto(
        UUID id,
        String referenceNumber,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category,
        UserDto customer,
        UserDto assignedAgent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt,
        LocalDateTime closedAt,
        List<TicketStatusUpdateDto> statusUpdates
) {
    /** Lightweight mapping for list views — no status history loaded. */
    public static TicketDto summary(Ticket t) {
        return new TicketDto(
                t.getId(), t.getReferenceNumber(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getCategory(),
                UserDto.from(t.getCustomer()),
                t.getAssignedAgent() != null ? UserDto.from(t.getAssignedAgent()) : null,
                t.getCreatedAt(), t.getUpdatedAt(), t.getResolvedAt(), t.getClosedAt(),
                null
        );
    }

    /** Full mapping for detail view — includes status history. */
    public static TicketDto detail(Ticket t) {
        List<TicketStatusUpdateDto> history = t.getStatusUpdates().stream()
                .map(TicketStatusUpdateDto::from)
                .toList();
        return new TicketDto(
                t.getId(), t.getReferenceNumber(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getCategory(),
                UserDto.from(t.getCustomer()),
                t.getAssignedAgent() != null ? UserDto.from(t.getAssignedAgent()) : null,
                t.getCreatedAt(), t.getUpdatedAt(), t.getResolvedAt(), t.getClosedAt(),
                history
        );
    }
}
