package fr.monolith.infrastructure.stream.user.producer;

import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.port.outbound.UserEventPublisher;
import fr.monolith.infrastructure.stream.user.event.UserActivatedEvent;
import fr.monolith.infrastructure.stream.user.event.UserCreatedEvent;
import fr.monolith.infrastructure.stream.user.event.UserDeactivatedEvent;
import fr.monolith.infrastructure.stream.user.mapper.UserEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producteur Kafka — implémente le port sortant {@link UserEventPublisher}.
 *
 * Responsabilité : convertir les événements domaine en messages Kafka.
 *
 * Pattern : Secondary Adapter / Driven Adapter (pour les événements)
 *
 * Le domaine ne connaît pas Kafka.
 * Il appelle seulement UserEventPublisher.publishUserCreated(user).
 * C'est cet adapter qui fait la conversion et l'envoi Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserKafkaProducer implements UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserEventMapper eventMapper;

    @Override
    public void publishUserCreated(User user) {
        UserCreatedEvent event = eventMapper.toCreatedEvent(user);
        log.info("Publishing UserCreatedEvent for userId: {}", user.getId());

        kafkaTemplate.send(UserCreatedEvent.TOPIC, user.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserCreatedEvent for userId: {}", user.getId(), ex);
                    } else {
                        log.debug("UserCreatedEvent published — topic: {}, offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    @Override
    public void publishUserActivated(User user) {
        UserActivatedEvent event = eventMapper.toActivatedEvent(user);
        log.info("Publishing UserActivatedEvent for userId: {}", user.getId());

        kafkaTemplate.send(UserActivatedEvent.TOPIC, user.getId().toString(), event);
    }

    @Override
    public void publishUserDeactivated(User user) {
        UserDeactivatedEvent event = eventMapper.toDeactivatedEvent(user);
        log.info("Publishing UserDeactivatedEvent for userId: {}", user.getId());

        kafkaTemplate.send(UserDeactivatedEvent.TOPIC, user.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserDeactivatedEvent for userId: {}", user.getId(), ex);
                    } else {
                        log.debug("UserDeactivatedEvent published — topic: {}, offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
