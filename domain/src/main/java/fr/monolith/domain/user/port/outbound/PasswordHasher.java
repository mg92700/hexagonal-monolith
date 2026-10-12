package fr.monolith.domain.user.port.outbound;

/**
 * PORT SORTANT — hachage des mots de passe.
 *
 * Le domaine et l'application ne manipulent jamais un mot de passe en clair
 * au-delà de cet appel : seul le hash est stocké.
 * Implémenté dans infra-security.
 */
public interface PasswordHasher {

    /**
     * Calcule le hash (salé) d'un mot de passe en clair.
     */
    String hash(String rawPassword);
}