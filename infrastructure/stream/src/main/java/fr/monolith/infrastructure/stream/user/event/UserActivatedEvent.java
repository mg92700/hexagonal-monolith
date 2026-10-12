package fr.monolith.infrastructure.stream.user.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka — utilisateur activé.
 *
 * @param eventId    identifiant unique de l'événement
 * @param userId     identifiant de l'utilisateur
 * @param email      email de l'utilisateur
 * @param occurredAt date d'occurrence
 */
public record UserActivatedEvent(
        UUID eventId,
        UUID userId,
        String email,
        LocalDateTime occurredAt
) {
    public static final String TOPIC = "user.activated";
}
