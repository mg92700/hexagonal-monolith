package fr.monolith.infrastructure.stream.user.event;

import fr.monolith.domain.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement Kafka — utilisateur créé.
 *
 * Ce record est le message publié sur le topic Kafka.
 * Il est sérialisé en JSON par Spring Kafka.
 *
 * Règle : ce record est spécifique à l'infrastructure stream.
 *         Le domaine ne le connaît pas.
 *
 * @param eventId   identifiant unique de l'événement
 * @param userId    identifiant de l'utilisateur
 * @param email     email de l'utilisateur
 * @param fullName  nom complet
 * @param status    statut au moment de l'événement
 * @param occurredAt date d'occurrence
 */
public record UserCreatedEvent(
        UUID eventId,
        UUID userId,
        String email,
        String fullName,
        UserStatus status,
        LocalDateTime occurredAt
) {
    public static final String TOPIC = "user.created";
}
