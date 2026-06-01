package com.deskflow.module.ticket.service;

import com.deskflow.infrastructure.kafka.KafkaProducerService;
import com.deskflow.module.ticket.domain.Ticket;
import com.deskflow.module.ticket.domain.TicketCategory;
import com.deskflow.module.ticket.domain.TicketPriority;
import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.module.ticket.dto.CreateTicketRequest;
import com.deskflow.module.ticket.dto.UpdateStatusRequest;
import com.deskflow.module.ticket.repository.TicketRepository;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import com.deskflow.shared.exception.BusinessRuleException;
import com.deskflow.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock ReferenceNumberGenerator refGen;
    @Mock KafkaProducerService kafka;

    @InjectMocks TicketService ticketService;

    private User customer;
    private User agent;

    @BeforeEach
    void setUp() {
        customer = buildUser(UserRole.CUSTOMER);
        agent    = buildUser(UserRole.SUPPORT_AGENT);
        when(refGen.generate()).thenReturn("TKT-202600001");
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", UUID.randomUUID());
            return t;
        });
    }

    @Test
    void createTicket_setsOpenStatusAndRefNumber() {
        var req = new CreateTicketRequest("Login broken", "Cannot log in", TicketPriority.HIGH, TicketCategory.TECHNICAL);

        var dto = ticketService.createTicket(req, customer);

        assertThat(dto.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(dto.referenceNumber()).isEqualTo("TKT-202600001");
        verify(kafka).publish(any(), any(), any());
    }

    @Test
    void listTickets_customerSeesOwnOnly() {
        var page = new PageImpl<>(List.of(buildTicket()));
        when(ticketRepository.findByCustomerId(customer.getId(), PageRequest.of(0, 20))).thenReturn(page);

        var result = ticketService.listTickets(customer, null, PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
        verify(ticketRepository).findByCustomerId(customer.getId(), PageRequest.of(0, 20));
        verify(ticketRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void listTickets_agentSeesAll() {
        var page = new PageImpl<>(List.of(buildTicket()));
        when(ticketRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

        var result = ticketService.listTickets(agent, null, PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
        verify(ticketRepository).findAll(PageRequest.of(0, 20));
    }

    @Test
    void updateStatus_validTransition_succeeds() {
        Ticket ticket = buildTicket(); // OPEN
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        var dto = ticketService.updateStatus(ticket.getId(),
                new UpdateStatusRequest(TicketStatus.IN_PROGRESS, "Taking it"), agent);

        assertThat(dto.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(kafka).publish(any(), any(), any());
    }

    @Test
    void updateStatus_invalidTransition_throwsBusinessRule() {
        Ticket ticket = buildTicket(); // OPEN → cannot jump to RESOLVED directly
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                ticketService.updateStatus(ticket.getId(),
                        new UpdateStatusRequest(TicketStatus.RESOLVED, null), agent))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateStatus_customerCantMoveToInProgress_throwsUnauthorized() {
        Ticket ticket = buildTicket(); // OPEN
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                ticketService.updateStatus(ticket.getId(),
                        new UpdateStatusRequest(TicketStatus.IN_PROGRESS, null), customer))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void ticketStatus_allowedTransitions_areCorrect() {
        assertThat(TicketStatus.OPEN.canTransitionTo(TicketStatus.IN_PROGRESS)).isTrue();
        assertThat(TicketStatus.OPEN.canTransitionTo(TicketStatus.RESOLVED)).isFalse();
        assertThat(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.PENDING_CUSTOMER)).isTrue();
        assertThat(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.RESOLVED)).isTrue();
        assertThat(TicketStatus.PENDING_CUSTOMER.canTransitionTo(TicketStatus.IN_PROGRESS)).isTrue();
        assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED)).isTrue();
        assertThat(TicketStatus.CLOSED.canTransitionTo(TicketStatus.OPEN)).isFalse();
    }

    // ── helpers ───────────────────────────────────────────────────

    private User buildUser(UserRole role) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        u.setEmail(role.name().toLowerCase() + "@test.com");
        u.setPasswordHash("hashed");
        u.setFirstName("Test");
        u.setLastName(role.name());
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private Ticket buildTicket() {
        Ticket t = new Ticket();
        ReflectionTestUtils.setField(t, "id", UUID.randomUUID());
        t.setReferenceNumber("TKT-202600001");
        t.setTitle("Test ticket");
        t.setDescription("Description");
        t.setStatus(TicketStatus.OPEN);
        t.setPriority(TicketPriority.MEDIUM);
        t.setCategory(TicketCategory.TECHNICAL);
        t.setCustomer(customer);
        return t;
    }
}
