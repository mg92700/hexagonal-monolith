package fr.monolith.infrastructure.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI : déclare l'authentification JWT Bearer.
 * 1. POST /api/v1/auth/login pour obtenir un accessToken ;
 * 2. bouton « Authorize » de Swagger UI : coller le token (sans le préfixe « Bearer »).
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Hexagonal Monolith API", version = "1.0"),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}