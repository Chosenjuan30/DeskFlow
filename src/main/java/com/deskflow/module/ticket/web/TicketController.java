package com.deskflow.module.ticket.web;

import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.module.ticket.dto.CreateTicketRequest;
import com.deskflow.module.ticket.dto.TicketDto;
import com.deskflow.module.ticket.dto.UpdateStatusRequest;
import com.deskflow.module.ticket.service.TicketService;
import com.deskflow.module.user.domain.User;
import com.deskflow.shared.util.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Submit a new support ticket")
    public TicketDto createTicket(@Valid @RequestBody CreateTicketRequest request,
                                  @AuthenticationPrincipal User user) {
        return ticketService.createTicket(request, user);
    }

    @GetMapping
    @Operation(summary = "List tickets (customer sees own; agents/admins see all)")
    public PageResponse<TicketDto> listTickets(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketService.listTickets(user, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single ticket with full status history")
    public TicketDto getTicket(@PathVariable UUID id,
                               @AuthenticationPrincipal User user) {
        return ticketService.getTicket(id, user);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition ticket status")
    public TicketDto updateStatus(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateStatusRequest request,
                                  @AuthenticationPrincipal User user) {
        return ticketService.updateStatus(id, request, user);
    }
}
