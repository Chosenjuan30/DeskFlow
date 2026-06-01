package com.deskflow.module.assignment.messaging;

import com.deskflow.module.assignment.service.AssignmentService;
import com.deskflow.module.ticket.event.TicketCreatedEvent;
import com.deskflow.shared.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentEventConsumer {

    private final AssignmentService assignmentService;

    @KafkaListener(
            topics = KafkaConfig.TICKETS_CREATED,
            containerFactory = "assignmentListenerFactory"
    )
    public void onTicketCreated(TicketCreatedEvent event) {
        log.info("Received TicketCreatedEvent ticketId={} category={}", event.getTicketId(), event.getCategory());
        try {
            assignmentService.autoAssign(event.getTicketId(), event.getCategory())
                    .ifPresentOrElse(
                            dto -> log.info("Auto-assigned ticket {} to agent {}", event.getTicketId(), dto.agentId()),
                            () -> log.warn("No agent available — ticket {} left unassigned", event.getTicketId())
                    );
        } catch (Exception e) {
            log.error("Auto-assign failed for ticket {}: {}", event.getTicketId(), e.getMessage(), e);
        }
    }
}
