package com.deskflow.module.assignment.repository;

import com.deskflow.module.assignment.domain.AgentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentAssignmentRepository extends JpaRepository<AgentAssignment, UUID> {

    List<AgentAssignment> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);

    Optional<AgentAssignment> findTopByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}