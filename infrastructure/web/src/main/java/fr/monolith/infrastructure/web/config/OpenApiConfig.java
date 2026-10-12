package fr.monolith.infrastructure.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI : déclare l'authentification HTTP Basic.
 * Le bouton « Authorize » de Swagger UI permet de saisir login / mot de passe
 * qui sont ensuite envoyés sur chaque appel « Try it out ».
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Hexagonal Monolith API", version = "1.0"),
        security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH)
)
@SecurityScheme(
        name = OpenApiConfig.BASIC_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class OpenApiConfig {

    public static final String BASIC_AUTH = "basicAuth";
}