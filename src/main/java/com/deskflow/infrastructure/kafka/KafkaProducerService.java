package com.deskflow.infrastructure.kafka;

import com.deskflow.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Central Kafka producer. All modules publish through this service
 * to keep Kafka wiring out of domain logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish an event. Uses the event's class name as the Kafka key
     * so related events land on the same partition when the ticket ID
     * is used as key.
     */
    public void publish(String topic, DomainEvent event) {
        publish(topic, event.getEventId().toString(), event);
    }

    public void publish(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish to topic={} key={}: {}",
                                topic, key, ex.getMessage(), ex);
                    } else {
                        log.debug("Published to topic={} key={} partition={} offset={}",
                                topic, key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}