package fr.monolith.infrastructure.persistence.user.mapper;

import fr.monolith.domain.user.model.User;
import fr.monolith.infrastructure.persistence.user.entity.UserJpaEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper persistance — conversion entre entité JPA et modèle domaine.
 *
 * Couche : JPA Entity ↔ Domain Model
 *
 * Règle : ce mapper est la seule couture entre persistence et domaine.
 * Les deux modèles peuvent évoluer indépendamment.
 */
@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    /**
     * JPA Entity → Domain Model.
     */
    User toDomain(UserJpaEntity entity);

    /**
     * Domain Model → JPA Entity.
     */
    UserJpaEntity toJpaEntity(User user);

    /**
     * Liste JPA → liste domaine.
     */
    List<User> toDomainList(List<UserJpaEntity> entities);
}
