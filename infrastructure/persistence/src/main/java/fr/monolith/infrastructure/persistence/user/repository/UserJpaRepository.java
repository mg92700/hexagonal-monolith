package fr.monolith.infrastructure.persistence.user.repository;

import fr.monolith.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository Spring Data JPA — interface technique BDD.
 *
 * Cette interface est INTERNE au module persistence.
 * Elle ne doit PAS être exposée à l'application ou au domaine.
 *
 * Règle : utilisée uniquement par {@link fr.monolith.infrastructure.persistence.user.adapter.UserPersistenceAdapter}
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}
