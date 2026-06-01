package com.deskflow.module.assignment.service;

import com.deskflow.infrastructure.kafka.KafkaProducerService;
import com.deskflow.module.assignment.domain.AgentAssignment;
import com.deskflow.module.assignment.dto.AgentStatsDto;
import com.deskflow.module.assignment.dto.AssignmentDto;
import com.deskflow.module.assignment.event.AgentAssignedEvent;
import com.deskflow.module.assignment.repository.AgentAssignmentRepository;
import com.deskflow.module.ticket.domain.Ticket;
import com.deskflow.module.ticket.domain.TicketCategory;
import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.module.ticket.dto.TicketDto;
import com.deskflow.module.ticket.repository.TicketRepository;
import com.deskflow.module.user.domain.AgentProfile;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.repository.AgentProfileRepository;
import com.deskflow.shared.config.KafkaConfig;
import com.deskflow.shared.exception.ResourceNotFoundException;
import com.deskflow.shared.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private static final List<TicketStatus> ACTIVE_STATUSES =
            List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.PENDING_CUSTOMER);

    private final TicketRepository ticketRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final AgentAssignmentRepository agentAssignmentRepository;
    private final KafkaProducerService kafka;

    @Transactional
    public Optional<AssignmentDto> autoAssign(UUID ticketId, TicketCategory category) {
        Ticket ticket = loadTicket(ticketId);

        // Try expertise-matched agents first
        List<AgentProfile> candidates = agentProfileRepository.findAvailableByExpertise(category.name());

        // Fallback: any available agent sorted by load
        if (candidates.isEmpty()) {
            candidates = agentProfileRepository.findByAvailableTrueOrderByCurrentLoadAsc();
        }

        if (candidates.isEmpty()) {
            log.warn("No available agents for ticket {}", ticket.getReferenceNumber());
            return Optional.empty();
        }

        AgentProfile selected = candidates.get(0);
        User agent = selected.getUser();

        AgentAssignment assignment = new AgentAssignment();
        assignment.setTicket(ticket);
        assignment.setAgent(agent);
        assignment.setAssignedBy(null);
        agentAssignmentRepository.save(assignment);

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        selected.setCurrentLoad(selected.getCurrentLoad() + 1);
        agentProfileRepository.save(selected);

        kafka.publish(KafkaConfig.ASSIGNMENTS_CREATED, ticketId.toString(),
                new AgentAssignedEvent(ticketId, ticket.getReferenceNumber(), agent.getId(), null));

        log.info("Auto-assigned ticket {} to agent {}", ticket.getReferenceNumber(), agent.getEmail());
        return Optional.of(AssignmentDto.from(assignment));
    }

    @Transactional
    public AssignmentDto manualAssign(UUID ticketId, UUID agentId, User assignedBy) {
        Ticket ticket = loadTicket(ticketId);
        AgentProfile profile = loadAgentProfile(agentId);
        User agent = profile.getUser();

        AgentAssignment assignment = new AgentAssignment();
        assignment.setTicket(ticket);
        assignment.setAgent(agent);
        assignment.setAssignedBy(assignedBy);
        agentAssignmentRepository.save(assignment);

        ticket.setAssignedAgent(agent);
        ticketRepository.save(ticket);

        profile.setCurrentLoad(profile.getCurrentLoad() + 1);
        agentProfileRepository.save(profile);

        kafka.publish(KafkaConfig.ASSIGNMENTS_CREATED, ticketId.toString(),
                new AgentAssignedEvent(ticketId, ticket.getReferenceNumber(), agentId, assignedBy.getId()));

        log.info("Manual assign: ticket {} → agent {} by {}",
                ticket.getReferenceNumber(), agent.getEmail(), assignedBy.getEmail());
        return AssignmentDto.from(assignment);
    }

    @Transactional
    public AssignmentDto reassign(UUID ticketId, UUID newAgentId, User reassignedBy) {
        Ticket ticket = loadTicket(ticketId);

        // Decrement old agent's load
        if (ticket.getAssignedAgent() != null) {
            agentProfileRepository.findByUserId(ticket.getAssignedAgent().getId())
                    .ifPresent(oldProfile -> {
                        oldProfile.setCurrentLoad(Math.max(0, oldProfile.getCurrentLoad() - 1));
                        agentProfileRepository.save(oldProfile);
                    });
        }

        AgentProfile newProfile = loadAgentProfile(newAgentId);
        User newAgent = newProfile.getUser();

        AgentAssignment assignment = new AgentAssignment();
        assignment.setTicket(ticket);
        assignment.setAgent(newAgent);
        assignment.setAssignedBy(reassignedBy);
        agentAssignmentRepository.save(assignment);

        ticket.setAssignedAgent(newAgent);
        ticketRepository.save(ticket);

        newProfile.setCurrentLoad(newProfile.getCurrentLoad() + 1);
        agentProfileRepository.save(newProfile);

        kafka.publish(KafkaConfig.ASSIGNMENTS_CREATED, ticketId.toString(),
                new AgentAssignedEvent(ticketId, ticket.getReferenceNumber(), newAgentId, reassignedBy.getId()));

        log.info("Reassigned ticket {} → agent {} by {}",
                ticket.getReferenceNumber(), newAgent.getEmail(), reassignedBy.getEmail());
        return AssignmentDto.from(assignment);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketDto> getAgentQueue(User agent, Pageable pageable) {
        Page<Ticket> page = ticketRepository.findByAssignedAgentIdAndStatusIn(
                agent.getId(), ACTIVE_STATUSES, pageable);
        return PageResponse.of(page.map(TicketDto::summary));
    }

    @Transactional(readOnly = true)
    public AgentStatsDto getAgentStats(User agent) {
        int active = (int) ticketRepository.countByAssignedAgentIdAndStatusIn(agent.getId(), ACTIVE_STATUSES);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        int resolvedToday = (int) ticketRepository.countResolvedSince(agent.getId(), startOfDay);
        int load = agentProfileRepository.findByUserId(agent.getId())
                .map(AgentProfile::getCurrentLoad)
                .orElse(0);
        return new AgentStatsDto(active, resolvedToday, load);
    }

    // ── helpers ───────────────────────────────────────────────────

    private Ticket loadTicket(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private AgentProfile loadAgentProfile(UUID agentId) {
        return agentProfileRepository.findByUserId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent profile not found for user " + agentId));
    }
}
