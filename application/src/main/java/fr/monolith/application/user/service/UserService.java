package fr.monolith.application.user.service;

import fr.monolith.domain.user.exception.UserAlreadyExistsException;
import fr.monolith.domain.user.exception.UserNotFoundException;
import fr.monolith.domain.user.model.Role;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.port.inbound.UserUseCase;
import fr.monolith.domain.user.port.outbound.PasswordHasher;
import fr.monolith.domain.user.port.outbound.UserEventPublisher;
import fr.monolith.domain.user.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service applicatif — implémente le port entrant {@link UserUseCase}.
 *
 * Responsabilités :
 * - Orchestration des cas d'usage
 * - Appel des ports sortants (repository, event publisher)
 * - Application des règles métier via le domaine
 *
 * Ce service NE connaît PAS :
 * - Spring MVC (aucun @RequestMapping)
 * - JPA (aucun @Entity, @Query)
 * - Kafka (aucun @KafkaListener)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;
    private final PasswordHasher passwordHasher;

    // ── Implémentation UserUseCase ─────────────────────────

    @Override
    @Transactional
    public User createUser(String login, String rawPassword, Role role,
                           String email, String firstName, String lastName) {
        String normalizedLogin = User.normalizeLogin(login);
        String normalizedEmail = normalizeEmail(email);
        String normalizedFirstName = normalizeName(firstName, "First name");
        String normalizedLastName = normalizeName(lastName, "Last name");
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        log.info("Creating user with login: {}", normalizedLogin);

        if (userRepository.existsByLogin(normalizedLogin)) {
            throw UserAlreadyExistsException.withLogin(normalizedLogin);
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw UserAlreadyExistsException.withEmail(normalizedEmail);
        }

        User user = User.create(normalizedLogin, passwordHasher.hash(rawPassword), role,
                normalizedEmail, normalizedFirstName, normalizedLastName);
        User saved = userRepository.save(user);

        userEventPublisher.publishUserCreated(saved);

        log.info("User created with id: {}", saved.getId());
        return saved;
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User activateUser(UUID id) {
        log.info("Activating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        User activated = user.activate(); // règle métier dans le domaine
        User saved = userRepository.save(activated);

        userEventPublisher.publishUserActivated(saved);

        return saved;
    }

    @Override
    @Transactional
    public User deactivateUser(UUID id) {
        log.info("Deactivating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        User deactivated = user.deactivate();
        User saved = userRepository.save(deactivated);

        userEventPublisher.publishUserDeactivated(saved);

        return saved;
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);

        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    // ── Méthodes utilitaires ───────────────────────────────

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
