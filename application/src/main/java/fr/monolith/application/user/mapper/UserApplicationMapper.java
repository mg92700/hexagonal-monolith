package fr.monolith.application.user.mapper;

import fr.monolith.application.user.dto.UserResult;
import fr.monolith.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper applicatif — conversion entre domaine et DTOs applicatifs.
 *
 * Couche : Domain Model ↔ Application DTOs
 *
 * Règle Clean Code :
 * - Le domaine n'est pas pollué par des annotations applicatives
 * - Ce mapper est la couture entre domaine et application
 */
@Mapper(componentModel = "spring")
public interface UserApplicationMapper {

    /**
     * Domaine → Result DTO.
     * fullName est calculé via expression MapStruct.
     */
    @Mapping(target = "fullName", expression = "java(user.fullName())")
    UserResult toResult(User user);

    /**
     * Liste domaine → liste Result DTO.
     */
    List<UserResult> toResultList(List<User> users);
}
