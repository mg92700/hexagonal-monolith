package fr.monolith.application.user.dto;

import fr.monolith.domain.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Résultat applicatif — ce que le use case retourne aux adaptateurs.
 *
 * Ce record est distinct du modèle domaine {@link fr.monolith.domain.user.model.User}.
 * Le mapper applicatif fait la conversion.
 *
 * Avantage : les adaptateurs (REST, Kafka) ne dépendent pas du modèle domaine directement.
 *
 * @param id        identifiant unique
 * @param email     email
 * @param firstName prénom
 * @param lastName  nom
 * @param fullName  nom complet (calculé)
 * @param status    statut
 * @param createdAt date de création
 * @param updatedAt date de mise à jour
 */
public record UserResult(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
