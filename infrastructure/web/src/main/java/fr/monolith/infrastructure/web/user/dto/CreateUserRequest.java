package fr.monolith.infrastructure.web.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête HTTP — création d'un utilisateur.
 *
 * Couche : HTTP Request → Application Command
 *
 * Contient les validations Jakarta (Bean Validation).
 * N'a aucune connaissance du domaine.
 */
@Schema(description = "Requête de création d'un utilisateur")
public record CreateUserRequest(

        @Schema(description = "Email unique de l'utilisateur", example = "john.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Prénom", example = "John")
        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @Schema(description = "Nom de famille", example = "Doe")
        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName
) {}
