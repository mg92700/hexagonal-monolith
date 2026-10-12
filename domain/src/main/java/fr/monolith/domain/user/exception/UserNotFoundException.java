package fr.monolith.domain.user.exception;

import java.util.UUID;

/**
 * Exception métier — utilisateur introuvable.
 * Définie dans le domaine, interceptée dans la couche web.
 */
public class UserNotFoundException extends RuntimeException {

    private final UUID userId;

    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
