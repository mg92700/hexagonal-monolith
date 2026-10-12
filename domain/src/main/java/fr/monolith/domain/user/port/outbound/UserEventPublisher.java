package fr.monolith.domain.user.port.outbound;

import fr.monolith.domain.user.model.User;

/**
 * PORT SORTANT — publication d'événements domaine.
 *
 * Implémenté dans infra-stream (Kafka) ou tout autre broker.
 * Le domaine ne connaît pas Kafka — il connaît seulement ce contrat.
 */
public interface UserEventPublisher {

    /**
     * Publier un événement de création d'utilisateur.
     */
    void publishUserCreated(User user);

    /**
     * Publier un événement d'activation d'utilisateur.
     */
    void publishUserActivated(User user);

    /**
     * Publier un événement de désactivation d'utilisateur.
     */
    void publishUserDeactivated(User user);
}
