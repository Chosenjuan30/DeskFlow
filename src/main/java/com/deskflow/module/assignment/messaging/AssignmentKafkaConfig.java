package com.deskflow.module.assignment.messaging;

import com.deskflow.module.ticket.event.TicketCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
class AssignmentKafkaConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TicketCreatedEvent> assignmentListenerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "deskflow-assignment");

        JsonDeserializer<TicketCreatedEvent> deser = new JsonDeserializer<>(TicketCreatedEvent.class);
        deser.setUseTypeHeaders(false);
        deser.addTrustedPackages("com.deskflow.*");

        var consumerFactory = new DefaultKafkaConsumerFactory<String, TicketCreatedEvent>(
                props, new StringDeserializer(), deser);

        var factory = new ConcurrentKafkaListenerContainerFactory<String, TicketCreatedEvent>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
