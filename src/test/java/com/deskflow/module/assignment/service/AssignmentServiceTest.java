package com.deskflow.module.assignment.service;

import com.deskflow.infrastructure.kafka.KafkaProducerService;
import com.deskflow.module.assignment.domain.AgentAssignment;
import com.deskflow.module.assignment.dto.AgentStatsDto;
import com.deskflow.module.assignment.dto.AssignmentDto;
import com.deskflow.module.assignment.repository.AgentAssignmentRepository;
import com.deskflow.module.ticket.domain.Ticket;
import com.deskflow.module.ticket.domain.TicketCategory;
import com.deskflow.module.ticket.domain.TicketPriority;
import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.module.ticket.repository.TicketRepository;
import com.deskflow.module.user.domain.AgentProfile;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import com.deskflow.module.user.repository.AgentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock AgentProfileRepository agentProfileRepository;
    @Mock AgentAssignmentRepository agentAssignmentRepository;
    @Mock KafkaProducerService kafka;

    @InjectMocks AssignmentService assignmentService;

    private Ticket ticket;
    private User agentUser;
    private AgentProfile agentProfile;

    @BeforeEach
    void setUp() {
        User customer = buildUser(UserRole.CUSTOMER);
        ticket = buildTicket(customer);

        agentUser = buildUser(UserRole.SUPPORT_AGENT);
        agentProfile = buildProfile(agentUser, Set.of("TECHNICAL"), 0);

        when(agentAssignmentRepository.save(any())).thenAnswer(inv -> {
            AgentAssignment a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "id", UUID.randomUUID());
            return a;
        });
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(agentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── autoAssign ────────────────────────────────────────────────

    @Test
    void autoAssign_expertiseMatch_assignsCorrectAgent() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentProfileRepository.findAvailableByExpertise("TECHNICAL")).thenReturn(List.of(agentProfile));

        Optional<AssignmentDto> result = assignmentService.autoAssign(ticket.getId(), TicketCategory.TECHNICAL);

        assertThat(result).isPresent();
        assertThat(result.get().agentId()).isEqualTo(agentUser.getId());
        assertThat(result.get().assignedById()).isNull();
        verify(kafka).publish(any(), any(), any());
    }

    @Test
    void autoAssign_noExpertiseMatch_fallsBackToAnyAvailableAgent() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentProfileRepository.findAvailableByExpertise("BILLING")).thenReturn(List.of());
        when(agentProfileRepository.findByAvailableTrueOrderByCurrentLoadAsc()).thenReturn(List.of(agentProfile));

        Optional<AssignmentDto> result = assignmentService.autoAssign(ticket.getId(), TicketCategory.BILLING);

        assertThat(result).isPresent();
        assertThat(result.get().agentId()).isEqualTo(agentUser.getId());
        verify(agentProfileRepository).findByAvailableTrueOrderByCurrentLoadAsc();
    }

    @Test
    void autoAssign_noAgentsAvailable_returnsEmpty() {
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentProfileRepository.findAvailableByExpertise(any())).thenReturn(List.of());
        when(agentProfileRepository.findByAvailableTrueOrderByCurrentLoadAsc()).thenReturn(List.of());

        Optional<AssignmentDto> result = assignmentService.autoAssign(ticket.getId(), TicketCategory.TECHNICAL);

        assertThat(result).isEmpty();
        verify(agentAssignmentRepository, never()).save(any());
        verify(kafka, never()).publish(any(), any(), any());
    }

    @Test
    void autoAssign_incrementsAgentLoad() {
        agentProfile.setCurrentLoad(3);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentProfileRepository.findAvailableByExpertise("TECHNICAL")).thenReturn(List.of(agentProfile));

        assignmentService.autoAssign(ticket.getId(), TicketCategory.TECHNICAL);

        verify(agentProfileRepository).save(argThat(p -> ((AgentProfile) p).getCurrentLoad() == 4));
    }

    // ── manualAssign ──────────────────────────────────────────────

    @Test
    void manualAssign_setsAssignedByUser() {
        User supervisor = buildUser(UserRole.SUPERVISOR);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentProfileRepository.findByUserId(agentUser.getId())).thenReturn(Optional.of(agentProfile));

        AssignmentDto dto = assignmentService.manualAssign(ticket.getId(), agentUser.getId(), supervisor);

        assertThat(dto.agentId()).isEqualTo(agentUser.getId());
        assertThat(dto.assignedById()).isEqualTo(supervisor.getId());
    }

    // ── reassign ──────────────────────────────────────────────────

    @Test
    void reassign_decrementsOldAgentLoad_incrementsNewAgentLoad() {
        User oldAgent = buildUser(UserRole.SUPPORT_AGENT);
        AgentProfile oldProfile = buildProfile(oldAgent, Set.of(), 2);
        ticket.setAssignedAgent(oldAgent);

        User supervisor = buildUser(UserRole.SUPERVISOR);
        User newAgent = buildUser(UserRole.SUPPORT_AGENT);
        AgentProfile newProfile = buildProfile(newAgent, Set.of(), 1);

        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentProfileRepository.findByUserId(oldAgent.getId())).thenReturn(Optional.of(oldProfile));
        when(agentProfileRepository.findByUserId(newAgent.getId())).thenReturn(Optional.of(newProfile));

        assignmentService.reassign(ticket.getId(), newAgent.getId(), supervisor);

        verify(agentProfileRepository).save(argThat(p -> ((AgentProfile) p).getCurrentLoad() == 1)); // old: 2→1
        verify(agentProfileRepository).save(argThat(p -> ((AgentProfile) p).getCurrentLoad() == 2)); // new: 1→2
    }

    // ── helpers ───────────────────────────────────────────────────

    private User buildUser(UserRole role) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        u.setEmail(role.name().toLowerCase() + UUID.randomUUID() + "@test.com");
        u.setPasswordHash("hashed");
        u.setFirstName("Test");
        u.setLastName(role.name());
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private AgentProfile buildProfile(User user, Set<String> expertise, int load) {
        AgentProfile p = new AgentProfile();
        ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
        p.setUser(user);
        p.getExpertise().addAll(expertise);
        p.setCurrentLoad(load);
        p.setAvailable(true);
        return p;
    }

    private Ticket buildTicket(User customer) {
        Ticket t = new Ticket();
        ReflectionTestUtils.setField(t, "id", UUID.randomUUID());
        t.setReferenceNumber("TKT-202600001");
        t.setTitle("Test");
        t.setDescription("Desc");
        t.setStatus(TicketStatus.OPEN);
        t.setPriority(TicketPriority.MEDIUM);
        t.setCategory(TicketCategory.TECHNICAL);
        t.setCustomer(customer);
        return t;
    }
}
