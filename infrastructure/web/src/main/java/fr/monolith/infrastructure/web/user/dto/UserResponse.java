package fr.monolith.infrastructure.web.user.dto;

import fr.monolith.domain.user.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse HTTP — représentation JSON d'un utilisateur.
 *
 * Couche : Application Result → HTTP Response
 *
 * Ce record est la représentation externe de l'utilisateur.
 * Il peut évoluer indépendamment du modèle domaine (champs supplémentaires,
 * renommage de champs JSON, etc.)
 */
@Schema(description = "Réponse représentant un utilisateur")
public record UserResponse(

        @Schema(description = "Identifiant unique", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Email", example = "john.doe@example.com")
        String email,

        @Schema(description = "Prénom", example = "John")
        String firstName,

        @Schema(description = "Nom de famille", example = "Doe")
        String lastName,

        @Schema(description = "Nom complet", example = "John Doe")
        String fullName,

        @Schema(description = "Statut du compte")
        UserStatus status,

        @Schema(description = "Date de création")
        LocalDateTime createdAt,

        @Schema(description = "Date de dernière mise à jour")
        LocalDateTime updatedAt
) {}
