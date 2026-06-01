package com.deskflow.module.assignment.dto;

import com.deskflow.module.assignment.domain.AgentAssignment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssignmentDto(
        UUID assignmentId,
        UUID ticketId,
        String referenceNumber,
        UUID agentId,
        String agentName,
        UUID assignedById,
        LocalDateTime createdAt
) {
    public static AssignmentDto from(AgentAssignment a) {
        return new AssignmentDto(
                a.getId(),
                a.getTicket().getId(),
                a.getTicket().getReferenceNumber(),
                a.getAgent().getId(),
                a.getAgent().getFirstName() + " " + a.getAgent().getLastName(),
                a.getAssignedBy() != null ? a.getAssignedBy().getId() : null,
                a.getCreatedAt()
        );
    }
}