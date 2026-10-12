package fr.monolith.infrastructure.persistence.user.adapter;

import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.port.outbound.UserRepository;
import fr.monolith.infrastructure.persistence.user.entity.UserJpaEntity;
import fr.monolith.infrastructure.persistence.user.mapper.UserPersistenceMapper;
import fr.monolith.infrastructure.persistence.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur de persistance — implémente le port sortant {@link UserRepository}.
 *
 * C'est la seule classe du module persistence qui est connue de l'extérieur.
 * Elle traduit les appels domaine en opérations JPA via le repository Spring Data.
 *
 * Pattern : Adapter (Hexagonal Architecture)
 *           Secondary Adapter / Driven Adapter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toJpaEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return jpaRepository.findByLogin(login).map(mapper::toDomain);
    }

    @Override
    public boolean existsByLogin(String login) {
        return jpaRepository.existsByLogin(login);
    }
}
