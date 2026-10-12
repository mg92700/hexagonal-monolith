package fr.monolith.infrastructure.web.user.controller;

import fr.monolith.application.user.dto.UserResult;
import fr.monolith.application.user.mapper.UserApplicationMapper;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.port.inbound.UserUseCase;
import fr.monolith.infrastructure.web.user.dto.CreateUserRequest;
import fr.monolith.infrastructure.web.user.dto.UserResponse;
import fr.monolith.infrastructure.web.user.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST — adaptateur entrant HTTP.
 *
 * Responsabilités :
 * - Recevoir les requêtes HTTP
 * - Valider les DTOs
 * - Déléguer au use case via le port entrant {@link UserUseCase}
 * - Retourner les réponses HTTP
 *
 * Ce contrôleur ne contient AUCUNE logique métier.
 * Il ne manipule que des DTOs HTTP et appelle le service.
 *
 * Pattern : Primary Adapter / Driving Adapter
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API de gestion des utilisateurs")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserWebMapper webMapper;
    private final UserApplicationMapper applicationMapper;

    // ── POST /api/v1/users ─────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un utilisateur", description = "Crée un utilisateur avec statut PENDING (rôle USER par défaut). Réservé aux ADMIN.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("POST /api/v1/users — login: {}", request.login());

        User created = userUseCase.createUser(
                request.login(),
                request.password(),
                request.role(),
                request.email(),
                request.firstName(),
                request.lastName()
        );
        UserResult result = applicationMapper.toResult(created);
        UserResponse response = webMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/v1/users ──────────────────────────────────

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/v1/users");

        List<User> users = userUseCase.getAllUsers();
        List<UserResult> results = applicationMapper.toResultList(users);
        List<UserResponse> responses = webMapper.toResponseList(results);

        return ResponseEntity.ok(responses);
    }

    // ── GET /api/v1/users/{id} ─────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par son ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("GET /api/v1/users/{}", id);

        User user = userUseCase.getUserById(id);
        UserResult result = applicationMapper.toResult(user);
        UserResponse response = webMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    // ── PUT /api/v1/users/{id}/activate ───────────────────

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activer un utilisateur", description = "Passe le statut de PENDING à ACTIVE")
    public ResponseEntity<UserResponse> activateUser(@PathVariable UUID id) {
        log.info("PUT /api/v1/users/{}/activate", id);

        User activated = userUseCase.activateUser(id);
        UserResult result = applicationMapper.toResult(activated);
        UserResponse response = webMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    // ── PUT /api/v1/users/{id}/deactivate ─────────────────

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver un utilisateur", description = "Passe le statut de ACTIVE à INACTIVE")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        log.info("PUT /api/v1/users/{}/deactivate", id);

        User deactivated = userUseCase.deactivateUser(id);
        UserResult result = applicationMapper.toResult(deactivated);
        UserResponse response = webMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    // ── DELETE /api/v1/users/{id} ──────────────────────────

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("DELETE /api/v1/users/{}", id);

        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
