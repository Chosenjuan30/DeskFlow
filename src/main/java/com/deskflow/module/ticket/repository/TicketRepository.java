package com.deskflow.module.ticket.repository;

import com.deskflow.module.ticket.domain.Ticket;
import com.deskflow.module.ticket.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Page<Ticket> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Ticket> findByCustomerIdAndStatus(UUID customerId, TicketStatus status, Pageable pageable);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    long countByCustomerIdAndStatus(UUID customerId, TicketStatus status);

    @Query(value = "SELECT nextval('ticket_ref_seq')", nativeQuery = true)
    Long nextRefSeq();
}
