package fr.monolith.infrastructure.web.user.mapper;

import fr.monolith.application.user.dto.UserResult;
import fr.monolith.infrastructure.web.user.dto.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper web — conversion des résultats applicatifs vers les DTOs HTTP.
 *
 * Couche : Application DTOs → HTTP DTOs
 *
 * Ce mapper ne touche JAMAIS le domaine directement.
 *
 * Chaîne complète :
 *   User (use case) → UserResult (UserApplicationMapper)
 *                   → UserResponse (ce mapper) → HTTP Response
 */
@Mapper(componentModel = "spring")
public interface UserWebMapper {

    /**
     * Application Result → HTTP Response.
     */
    UserResponse toResponse(UserResult result);

    /**
     * Liste application → liste HTTP.
     */
    List<UserResponse> toResponseList(List<UserResult> results);
}
