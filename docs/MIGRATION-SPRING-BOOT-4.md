# Migration Spring Boot 3.3 → 4.1 et nettoyage

Date : 2026-10-12. Le projet n'est pas un dépôt Git : une sauvegarde d'avant migration existe uniquement dans le dossier de session Copilot.

## 1. Migration

| Élément | Avant | Après |
|---|---|---|
| Spring Boot | 3.3.0 | **4.1.1** |
| Spring Framework | 6.1 | 7.x |
| Hibernate | 6.5 | 7.x |
| Jackson | 2 (`com.fasterxml`) | 3 (`tools.jackson`) |
| springdoc-openapi | 2.5.0 | **3.1.1** |
| MapStruct | 1.5.5.Final | **1.6.3** |
| Lombok | 1.18.32 (forcé) | version gérée par le parent Spring Boot |
| maven-compiler-plugin | 3.11.0 (forcé) | version gérée par le parent |

### Changements nécessaires

- **Starters modulaires** (Spring Boot 4) :
  - `spring-boot-starter-web` → `spring-boot-starter-webmvc`
  - `spring-kafka` → `spring-boot-starter-kafka`
  - tests web : `spring-boot-starter-webmvc-test`
- **Packages déplacés** :
  - `@AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure`
  - `@EntityScan` → `org.springframework.boot.persistence.autoconfigure`
- **`@MockBean` supprimé** → `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`).
- **Jackson 3** : `ObjectMapper` vient de `tools.jackson.databind`.
- **Kafka** : `JsonSerializer` (Jackson 2) → `JacksonJsonSerializer` dans `application.yml`.
- **Hibernate 7** : propriété `hibernate.dialect` retirée (détection automatique).

### Vérification

- `mvn clean verify` : succès sur les 7 modules (tests unitaires + JaCoCo).
- Démarrage réel du jar contre PostgreSQL : 1 repository JPA détecté, Tomcat démarré, `POST/GET/DELETE /api/v1/users`
  fonctionnels, `/actuator/health` = `UP`, Swagger UI accessible.
- Non vérifié de bout en bout : la réception effective des messages côté Kafka (broker `unhealthy` au moment du test).

## 2. Nettoyage du code non utilisé

| Supprimé | Raison |
|---|---|
| `UserKafkaConsumer` (+ dépendance `infra-stream → application`) | `@KafkaListener` commenté : jamais actif |
| Config `spring.kafka.consumer.*` | Aucun consumer |
| `CreateUserCommand`, `UserWebMapper.toCommand`, `UserApplicationMapper.commandToDomain`, `UserService.createUserFromCommand` | Aucun appelant en production (le contrôleur appelait directement `createUser`) |
| `UserRepository.findByEmail` / `findByEmailIgnoreCase` (port, adapter, repository JPA) + leurs tests | Jamais appelés ; seul `existsByEmail` est utilisé |
| `UserJpaRepository.existsByEmail` | Doublon non utilisé de `existsByEmailIgnoreCase` |
| `UserNotFoundException(String email)` + son test | Plus d'usage après retrait de `findByEmail` |
| Profil Spring `local` (H2, Kafka exclu) | Inopérant : H2 n'était qu'en scope test, `KafkaTemplate` introuvable |
| Dépendance H2, `src/test/resources/application.properties` de persistence | Le test de l'adapter est purement Mockito |
| `spring-kafka-test` (infra-stream) | Aucun test Kafka embarqué |
| `spring-boot-starter-test` en double (infra-web) | Déjà hérité du `pom.xml` racine |
| `mapstruct-processor` dans `dependencyManagement` | Seul le `annotationProcessorPaths` du compilateur l'utilise |
| `maven.compiler.source/target` | Redondant avec `<release>` |
| `<version>6.2.10</version>` forcé sur `spring-tx` | Désaccord avec la version gérée par Spring Boot |
| `infrastructure/pom.xml` | POM orphelin : non référencé par le build (les modules sont déclarés dans le `pom.xml` racine) |
| `build.log`, `point.md` | Journal de build périmé ; contenu utile repris dans cette doc |
| `version: '3.8'` de `docker-compose.yml` | Attribut obsolète (avertissement Compose) |
| `@EnableJpaRepositories`/`@EntityScan` dans `HexagonalMonolithApplication` | Déplacés dans `infra-persistence` (`PersistenceConfiguration`) |

## 3. Améliorations associées

- `UserController` dépend du **port** `UserUseCase` au lieu de la classe `UserService` concrète.
- `spring.jpa.open-in-view: false`.
- `ExceptionsTest` : retrait d'un `@Nested` invalide sur une classe de premier niveau.

## 4. Correctifs métier hérités de l'ancien `point.md`

- L'événement Kafka de désactivation est bien `UserDeactivatedEvent`.
- Emails normalisés (`trim` + minuscules) ; unicité insensible à la casse.
- Prénom/nom nettoyés et obligatoires.

## 5. Points restant à traiter (hors périmètre)

- Migrations de schéma (Flyway/Liquibase) pour passer à `ddl-auto: validate`.
- `UserStatus.DELETED` n'est atteint par aucune transition.
- `UserService.normalizeEmail/normalizeName` dupliquent la normalisation de `User.create`.
- Avertissement Mockito (auto-attachement de l'agent) sur JDK récents : configurer l'agent Mockito dans Surefire.
- Endpoints Actuator `env` et Swagger exposés par défaut.
