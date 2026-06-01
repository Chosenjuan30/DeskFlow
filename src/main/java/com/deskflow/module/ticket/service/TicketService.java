package com.deskflow.module.ticket.service;

import com.deskflow.infrastructure.kafka.KafkaProducerService;
import com.deskflow.module.ticket.domain.*;
import com.deskflow.module.ticket.dto.*;
import com.deskflow.module.ticket.event.TicketCreatedEvent;
import com.deskflow.module.ticket.event.TicketStatusChangedEvent;
import com.deskflow.module.ticket.repository.TicketRepository;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import com.deskflow.shared.config.KafkaConfig;
import com.deskflow.shared.exception.BusinessRuleException;
import com.deskflow.shared.exception.ResourceNotFoundException;
import com.deskflow.shared.exception.UnauthorizedException;
import com.deskflow.shared.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ReferenceNumberGenerator refGen;
    private final KafkaProducerService kafka;

    @Transactional
    public TicketDto createTicket(CreateTicketRequest request, User customer) {
        Ticket ticket = new Ticket();
        ticket.setReferenceNumber(refGen.generate());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setCategory(request.category());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCustomer(customer);

        ticketRepository.save(ticket);

        kafka.publish(KafkaConfig.TICKETS_CREATED,
                ticket.getId().toString(),
                new TicketCreatedEvent(
                        ticket.getId(), ticket.getReferenceNumber(),
                        ticket.getPriority(), ticket.getCategory(),
                        customer.getId()));

        log.info("Ticket created ref={} by customer={}", ticket.getReferenceNumber(), customer.getEmail());
        return TicketDto.summary(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketDto> listTickets(User requester, TicketStatus statusFilter, Pageable pageable) {
        Page<Ticket> page;

        if (requester.getRole() == UserRole.CUSTOMER) {
            page = statusFilter != null
                    ? ticketRepository.findByCustomerIdAndStatus(requester.getId(), statusFilter, pageable)
                    : ticketRepository.findByCustomerId(requester.getId(), pageable);
        } else {
            page = statusFilter != null
                    ? ticketRepository.findByStatus(statusFilter, pageable)
                    : ticketRepository.findAll(pageable);
        }

        return PageResponse.of(page.map(TicketDto::summary));
    }

    @Transactional(readOnly = true)
    public TicketDto getTicket(UUID id, User requester) {
        Ticket ticket = loadTicket(id);
        checkReadAccess(ticket, requester);

        // Force-load lazy collections within the transaction
        ticket.getStatusUpdates().size();
        return TicketDto.detail(ticket);
    }

    @Transactional
    public TicketDto updateStatus(UUID id, UpdateStatusRequest request, User requester) {
        Ticket ticket = loadTicket(id);

        checkWriteAccess(ticket, requester, request.newStatus());

        TicketStatus oldStatus = ticket.getStatus();
        if (!oldStatus.canTransitionTo(request.newStatus())) {
            throw new BusinessRuleException(
                    "Cannot transition from " + oldStatus + " to " + request.newStatus());
        }

        ticket.setStatus(request.newStatus());
        if (request.newStatus() == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (request.newStatus() == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        TicketStatusUpdate update = new TicketStatusUpdate();
        update.setTicket(ticket);
        update.setOldStatus(oldStatus);
        update.setNewStatus(request.newStatus());
        update.setComment(request.comment());
        update.setChangedBy(requester);
        ticket.getStatusUpdates().add(update);

        ticketRepository.save(ticket);

        kafka.publish(KafkaConfig.TICKETS_STATUS_CHANGED,
                ticket.getId().toString(),
                new TicketStatusChangedEvent(
                        ticket.getId(), ticket.getReferenceNumber(),
                        oldStatus, request.newStatus(),
                        requester.getId()));

        log.info("Ticket {} transitioned {}→{} by {}",
                ticket.getReferenceNumber(), oldStatus, request.newStatus(), requester.getEmail());

        ticket.getStatusUpdates().size(); // ensure loaded before mapping
        return TicketDto.detail(ticket);
    }

    @Transactional(readOnly = true)
    public long countOpenTickets(UUID customerId) {
        return ticketRepository.countByCustomerIdAndStatus(customerId, TicketStatus.OPEN);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Ticket loadTicket(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private void checkReadAccess(Ticket ticket, User requester) {
        if (requester.getRole() == UserRole.CUSTOMER
                && !ticket.getCustomer().getId().equals(requester.getId())) {
            throw new UnauthorizedException("Access denied");
        }
    }

    private void checkWriteAccess(Ticket ticket, User requester, TicketStatus newStatus) {
        if (requester.getRole() == UserRole.CUSTOMER) {
            // Customer may only close their own resolved ticket
            if (!ticket.getCustomer().getId().equals(requester.getId())) {
                throw new UnauthorizedException("Access denied");
            }
            if (newStatus != TicketStatus.CLOSED) {
                throw new UnauthorizedException("Customers may only close resolved tickets");
            }
        }
    }
}
