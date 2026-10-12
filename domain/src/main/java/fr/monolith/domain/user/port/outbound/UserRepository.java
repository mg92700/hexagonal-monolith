package fr.monolith.domain.user.port.outbound;

import fr.monolith.domain.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PORT SORTANT — contrat que le domaine impose à la persistance.
 *
 * Cette interface est définie dans le domaine.
 * Elle est implémentée dans la couche infrastructure (UserPersistenceAdapter).
 * Elle est injectée dans la couche application (UserService).
 *
 * Pattern : Secondary Port / Driven Port
 *
 * Important : ce port manipule des objets domaine (User), PAS des entités JPA.
 */
public interface UserRepository {

    /**
     * Sauvegarder (créer ou mettre à jour) un utilisateur.
     */
    User save(User user);

    /**
     * Trouver un utilisateur par son ID.
     */
    Optional<User> findById(UUID id);

    /**
     * Récupérer tous les utilisateurs.
     */
    List<User> findAll();

    /**
     * Supprimer un utilisateur par son ID.
     */
    void deleteById(UUID id);

    /**
     * Vérifier l'existence d'un utilisateur par email, sans tenir compte de la casse.
     */
    boolean existsByEmail(String email);

    /**
     * Trouver un utilisateur par son login (déjà normalisé en minuscules).
     */
    Optional<User> findByLogin(String login);

    /**
     * Vérifier l'existence d'un login (déjà normalisé en minuscules).
     */
    boolean existsByLogin(String login);
}
