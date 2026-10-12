package fr.monolith.infrastructure.stream.user.mapper;

import fr.monolith.domain.user.model.User;
import fr.monolith.infrastructure.stream.user.event.UserActivatedEvent;
import fr.monolith.infrastructure.stream.user.event.UserCreatedEvent;
import fr.monolith.infrastructure.stream.user.event.UserDeactivatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * Mapper stream — conversion entre modèle domaine et événements Kafka.
 *
 * Couche : Domain Model → Kafka Event
 */
@Mapper(componentModel = "spring", imports = {UUID.class})
public interface UserEventMapper {

    /**
     * Domaine → UserCreatedEvent.
     * L'eventId est généré lors du mapping.
     */
    @Mapping(target = "eventId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", expression = "java(user.fullName())")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    UserCreatedEvent toCreatedEvent(User user);

    /**
     * Domaine → UserActivatedEvent.
     */
    @Mapping(target = "eventId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    UserActivatedEvent toActivatedEvent(User user);

    /**
     * Domaine → UserDeactivatedEvent.
     */
    @Mapping(target = "eventId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "occurredAt", expression = "java(java.time.LocalDateTime.now())")
    UserDeactivatedEvent toDeactivatedEvent(User user);
}
