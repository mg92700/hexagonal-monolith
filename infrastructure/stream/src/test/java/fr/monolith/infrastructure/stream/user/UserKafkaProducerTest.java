package fr.monolith.infrastructure.stream.user;

import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.infrastructure.stream.user.event.UserCreatedEvent;
import fr.monolith.infrastructure.stream.user.mapper.UserEventMapper;
import fr.monolith.infrastructure.stream.user.producer.UserKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserKafkaProducer — Tests unitaires")
class UserKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private UserEventMapper eventMapper;

    @InjectMocks
    private UserKafkaProducer producer;

    private User sampleUser;
    private UserCreatedEvent sampleEvent;

    @BeforeEach
    void setUp() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        sampleUser = User.builder()
                .id(id)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleEvent = new UserCreatedEvent(
                UUID.randomUUID(), id, "john@example.com", "John Doe",
                UserStatus.PENDING, now
        );
    }

    @Nested
    @DisplayName("publishUserCreated")
    class PublishUserCreated {

        @Test
        @DisplayName("doit mapper et envoyer l'événement sur le bon topic")
        void shouldMapAndSendEvent() {
            when(eventMapper.toCreatedEvent(sampleUser)).thenReturn(sampleEvent);
            when(kafkaTemplate.send(anyString(), anyString(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));

            producer.publishUserCreated(sampleUser);

            verify(eventMapper).toCreatedEvent(sampleUser);
            verify(kafkaTemplate).send(
                    eq(UserCreatedEvent.TOPIC),
                    eq(sampleUser.getId().toString()),
                    eq(sampleEvent)
            );
        }
    }

    @Nested
    @DisplayName("publishUserActivated")
    class PublishUserActivated {

        @Test
        @DisplayName("doit envoyer l'événement d'activation")
        void shouldSendActivatedEvent() {
            var activatedEvent = new fr.monolith.infrastructure.stream.user.event.UserActivatedEvent(
                    UUID.randomUUID(), sampleUser.getId(),
                    sampleUser.getEmail(), LocalDateTime.now()
            );
            when(eventMapper.toActivatedEvent(sampleUser)).thenReturn(activatedEvent);
            when(kafkaTemplate.send(anyString(), anyString(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));

            producer.publishUserActivated(sampleUser);

            verify(kafkaTemplate).send(
                    eq("user.activated"),
                    eq(sampleUser.getId().toString()),
                    any()
            );
        }
    }

    @Nested
    @DisplayName("publishUserDeactivated")
    class PublishUserDeactivated {

        @Test
        @DisplayName("doit envoyer l'événement de désactivation sur le bon topic")
        void shouldSendDeactivatedEvent() {
            var deactivatedEvent = new fr.monolith.infrastructure.stream.user.event.UserDeactivatedEvent(
                    UUID.randomUUID(), sampleUser.getId(),
                    sampleUser.getEmail(), LocalDateTime.now()
            );
            when(eventMapper.toDeactivatedEvent(sampleUser)).thenReturn(deactivatedEvent);
            when(kafkaTemplate.send(anyString(), anyString(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));

            producer.publishUserDeactivated(sampleUser);

            verify(eventMapper).toDeactivatedEvent(sampleUser);
            verify(kafkaTemplate).send(
                    eq("user.deactivated"),
                    eq(sampleUser.getId().toString()),
                    eq(deactivatedEvent)
            );
        }
    }
}
