package com.deskflow.module.assignment.web;

import com.deskflow.module.assignment.dto.AgentStatsDto;
import com.deskflow.module.assignment.dto.AssignmentDto;
import com.deskflow.module.assignment.dto.ManualAssignRequest;
import com.deskflow.module.assignment.service.AssignmentService;
import com.deskflow.module.ticket.dto.TicketDto;
import com.deskflow.module.user.domain.User;
import com.deskflow.shared.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/tickets/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<AssignmentDto> assign(
            @PathVariable UUID ticketId,
            @RequestBody @Valid ManualAssignRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(assignmentService.manualAssign(ticketId, request.agentId(), user));
    }

    @PostMapping("/tickets/{ticketId}/reassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<AssignmentDto> reassign(
            @PathVariable UUID ticketId,
            @RequestBody @Valid ManualAssignRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(assignmentService.reassign(ticketId, request.agentId(), user));
    }

    @GetMapping("/assignments/my-queue")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<PageResponse<TicketDto>> myQueue(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(assignmentService.getAgentQueue(user, pageable));
    }

    @GetMapping("/assignments/my-stats")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<AgentStatsDto> myStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(assignmentService.getAgentStats(user));
    }
}
