package com.deskflow.shared.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Marker interface for all domain events published to Kafka.
 * Every event must carry a unique ID and the time it occurred.
 */
public interface DomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredAt();
}