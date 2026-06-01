package com.deskflow.module.assignment.domain;

import com.deskflow.module.ticket.domain.Ticket;
import com.deskflow.module.user.domain.User;
import com.deskflow.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agent_assignments")
@Getter
@Setter
@NoArgsConstructor
public class AgentAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy; // null for auto-assignment
}