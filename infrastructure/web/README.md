# Module `infra-web`

> Artefact Maven : `fr.monolith:infra-web` — chemin : `infrastructure/web`
> Dépend de `application` (+ Spring MVC, Bean Validation, springdoc-openapi, MapStruct).

## Rôle

**Adaptateur entrant** HTTP/REST : transforme les requêtes en appels du port `UserUseCase`
et les résultats en réponses JSON. Documente l'API avec OpenAPI/Swagger.

## Ce que le module doit faire

| Responsabilité | Où |
|---|---|
| Exposer l'API REST `/api/v1/users` | `user/controller/UserController` |
| Valider les entrées (Jakarta Validation) | `user/dto/CreateUserRequest` |
| Définir le contrat JSON de sortie | `user/dto/UserResponse` |
| Convertir `UserResult` → `UserResponse` | `user/mapper/UserWebMapper` |
| Traduire les exceptions en réponses RFC 7807 (`ProblemDetail`) | `exception/GlobalExceptionHandler` |

## Ce que le module ne doit PAS faire

- Aucune logique métier : le contrôleur délègue au port `UserUseCase`, jamais à une implémentation concrète.
- Aucune dépendance vers `infra-persistence` ou `infra-stream`.

## API

| Méthode | Endpoint | Succès | Erreurs |
|---|---|---|---|
| POST | `/api/v1/users` | 201 | 400 (validation), 409 (email déjà utilisé) |
| GET | `/api/v1/users` | 200 | |
| GET | `/api/v1/users/{id}` | 200 | 404 |
| PUT | `/api/v1/users/{id}/activate` | 200 | 400 (statut invalide), 404 |
| PUT | `/api/v1/users/{id}/deactivate` | 200 | 400 (statut invalide), 404 |
| DELETE | `/api/v1/users/{id}` | 204 | 404 |

Swagger UI : <http://localhost:8080/swagger-ui.html> — spec JSON : <http://localhost:8080/api-docs>

## Structure

```
fr.monolith.infrastructure.web
├── exception/   GlobalExceptionHandler
└── user/
    ├── controller/  UserController
    ├── dto/         CreateUserRequest, UserResponse
    └── mapper/      UserWebMapper
```

## Tests

`UserControllerTest` : `@SpringBootTest` + MockMvc, `UserUseCase` et mappers remplacés par `@MockitoBean`
(Spring Boot 4 : `spring-boot-starter-webmvc-test`, Jackson 3 `tools.jackson`).

```bash
mvn test -pl infrastructure/web
```

## Ajouter un autre protocole

GraphQL, gRPC… : créer un module `infra-<protocole>` qui appelle lui aussi `UserUseCase`.
