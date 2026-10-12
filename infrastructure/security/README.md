# infra-security

Adaptateur Spring Security.

## Rôle du module
- `SecurityConfiguration` : HTTP Basic stateless, CSRF désactivé, règles d'accès par rôle (voir README racine), Swagger protégé.
- `BCryptPasswordHasher` : implémente le port `PasswordHasher` du domaine.
- `UserRepositoryUserDetailsService` : charge l'utilisateur via le port `UserRepository`, autorité `ROLE_<role>`, compte actif seulement si `ACTIVE`.
- `AdminUserInitializer` : crée au démarrage l'admin (`app.security.admin.login/password/email`, défaut `admin`/`admin`) s'il n'existe pas ; ne modifie jamais un compte existant ; n'émet pas d'événement Kafka.

## Dépendances
`domain` + `spring-boot-starter-security`. Aucune dépendance vers `application`, `infra-web` ou `infra-persistence`.

## Tests
Hash BCrypt, UserDetailsService, initialiseur d'admin et règles d'accès (401/403/200, Swagger, health) via MockMvc.