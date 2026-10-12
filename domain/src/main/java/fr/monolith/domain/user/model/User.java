package fr.monolith.domain.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Entité domaine User — cœur métier pur.
 *
 * Règles :
 * - Aucune annotation Spring, JPA, Jackson ici
 * - Immuable via @Builder + @With (Lombok)
 * - Contient la logique métier (validation, règles)
 */
@Getter
@Builder
@With
public class User {

    private final UUID id;
    private final String login;
    /** Hash du mot de passe (jamais le mot de passe en clair). */
    private final String password;
    private final Role role;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Règle métier : un utilisateur peut être activé
     * uniquement s'il est en statut PENDING.
     */
    public User activate() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot activate user with status: " + this.status
            );
        }
        return this.withStatus(UserStatus.ACTIVE)
                   .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * Règle métier : désactivation d'un utilisateur actif.
     */
    public User deactivate() {
        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                "Cannot deactivate user with status: " + this.status
            );
        }
        return this.withStatus(UserStatus.INACTIVE)
                   .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * Factory method — création d'un nouvel utilisateur.
     *
     * @param passwordHash mot de passe DÉJÀ haché (voir PasswordHasher)
     * @param role         rôle ; USER si null
     */
    public static User create(String login, String passwordHash, Role role,
                              String email, String firstName, String lastName) {
        String normalizedLogin = normalizeLogin(login);
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        String normalizedEmail = normalizeEmail(email);
        String normalizedFirstName = normalizeName(firstName, "First name");
        String normalizedLastName = normalizeName(lastName, "Last name");

        return User.builder()
                .id(UUID.randomUUID())
                .login(normalizedLogin)
                .password(passwordHash)
                .role(role != null ? role : Role.USER)
                .email(normalizedEmail)
                .firstName(normalizedFirstName)
                .lastName(normalizedLastName)
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Le login est insensible à la casse : stocké en minuscules.
     */
    public static String normalizeLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }
        return login.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    /**
     * Nom complet — règle de présentation métier.
     */
    public String fullName() {
        return firstName + " " + lastName;
    }
}
