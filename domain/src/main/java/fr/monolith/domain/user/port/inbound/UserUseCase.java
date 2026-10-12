package fr.monolith.domain.user.port.inbound;

import fr.monolith.domain.user.model.User;

import java.util.List;
import java.util.UUID;

/**
 * PORT ENTRANT — contrat que la couche application expose.
 *
 * Cette interface est définie dans le domaine.
 * Elle est implémentée dans la couche application (UserService).
 * Elle est appelée depuis les adaptateurs entrants (REST, Kafka consumer...).
 *
 * Pattern : Primary Port / Driving Port
 */
public interface UserUseCase {

    /**
     * Créer un nouvel utilisateur.
     *
     * @param email     email unique
     * @param firstName prénom
     * @param lastName  nom
     * @return utilisateur créé
     */
    User createUser(String email, String firstName, String lastName);

    /**
     * Récupérer un utilisateur par son identifiant.
     *
     * @param id UUID de l'utilisateur
     * @return utilisateur trouvé
     */
    User getUserById(UUID id);

    /**
     * Récupérer tous les utilisateurs.
     *
     * @return liste de tous les utilisateurs
     */
    List<User> getAllUsers();

    /**
     * Activer un utilisateur (statut PENDING → ACTIVE).
     *
     * @param id UUID de l'utilisateur
     * @return utilisateur activé
     */
    User activateUser(UUID id);

    /**
     * Désactiver un utilisateur (statut ACTIVE → INACTIVE).
     *
     * @param id UUID de l'utilisateur
     * @return utilisateur désactivé
     */
    User deactivateUser(UUID id);

    /**
     * Supprimer un utilisateur.
     *
     * @param id UUID de l'utilisateur
     */
    void deleteUser(UUID id);
}
