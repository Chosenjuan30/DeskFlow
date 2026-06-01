package com.deskflow.module.ticket.dto;

import com.deskflow.module.auth.dto.UserDto;
import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.module.ticket.domain.TicketStatusUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketStatusUpdateDto(
        UUID id,
        TicketStatus oldStatus,
        TicketStatus newStatus,
        String comment,
        UserDto changedBy,
        LocalDateTime createdAt
) {
    public static TicketStatusUpdateDto from(TicketStatusUpdate u) {
        return new TicketStatusUpdateDto(
                u.getId(),
                u.getOldStatus(),
                u.getNewStatus(),
                u.getComment(),
                UserDto.from(u.getChangedBy()),
                u.getCreatedAt()
        );
    }
}
