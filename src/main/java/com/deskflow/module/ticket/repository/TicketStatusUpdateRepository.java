package com.deskflow.module.ticket.repository;

import com.deskflow.module.ticket.domain.TicketStatusUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketStatusUpdateRepository extends JpaRepository<TicketStatusUpdate, UUID> {
    List<TicketStatusUpdate> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
