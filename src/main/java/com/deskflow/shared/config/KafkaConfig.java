package com.deskflow.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares all Kafka topics. Spring Kafka auto-creates them on startup
 * if KAFKA_AUTO_CREATE_TOPICS_ENABLE is false on the broker.
 */
@Configuration
public class KafkaConfig {

    // ── Topic name constants (imported by producers & consumers) ──
    public static final String TICKETS_CREATED         = "deskflow.tickets.created";
    public static final String TICKETS_STATUS_CHANGED  = "deskflow.tickets.status-changed";
    public static final String ASSIGNMENTS_CREATED     = "deskflow.assignments.created";
    public static final String SLA_WARNING             = "deskflow.sla.warning";
    public static final String SLA_BREACHED            = "deskflow.sla.breached";
    public static final String ESCALATIONS_CREATED     = "deskflow.escalations.created";
    public static final String NOTIFICATIONS_SEND      = "deskflow.notifications.send";

    @Bean
    public NewTopic ticketsCreated() {
        return TopicBuilder.name(TICKETS_CREATED).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic ticketsStatusChanged() {
        return TopicBuilder.name(TICKETS_STATUS_CHANGED).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic assignmentsCreated() {
        return TopicBuilder.name(ASSIGNMENTS_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic slaWarning() {
        return TopicBuilder.name(SLA_WARNING).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic slaBreached() {
        return TopicBuilder.name(SLA_BREACHED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic escalationsCreated() {
        return TopicBuilder.name(ESCALATIONS_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationsSend() {
        return TopicBuilder.name(NOTIFICATIONS_SEND).partitions(6).replicas(1).build();
    }
}