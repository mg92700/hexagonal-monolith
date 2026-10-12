package fr.monolith.infrastructure.web.user.dto;

import fr.monolith.domain.user.model.Role;
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

        @Schema(description = "Login unique (insensible à la casse)", example = "john.doe")
        @NotBlank(message = "Login is required")
        @Size(min = 3, max = 100, message = "Login must be between 3 and 100 characters")
        String login,

        @Schema(description = "Mot de passe en clair (haché côté serveur, jamais renvoyé)", example = "S3cretPassw0rd")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @Schema(description = "Rôle (USER par défaut si absent)", example = "USER")
        Role role,

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
