# Module `app`

> Artefact Maven : `fr.monolith:app` — jar exécutable Spring Boot.
> Dépend de tous les autres modules + Actuator + driver PostgreSQL (runtime).

## Rôle

**Racine de composition** : assemble les modules et démarre l'application. Il ne contient
**aucune logique métier**.

## Ce que le module doit faire

| Responsabilité | Où |
|---|---|
| Point d'entrée (`@SpringBootApplication(scanBasePackages = "fr.monolith")`) | `HexagonalMonolithApplication` |
| Configuration unique de l'application (datasource, JPA, Kafka, actuator, springdoc, logs) | `src/main/resources/application.yml` |
| Choisir quels adaptateurs sont embarqués (via ses dépendances Maven) | `pom.xml` |
| Produire le fat jar (`spring-boot-maven-plugin`, Lombok exclu) | `pom.xml` |

## Ce que le module ne doit PAS faire

- Aucune classe métier, aucun contrôleur, aucun adapter.
- Aucune configuration spécifique à un adaptateur qui peut vivre dans son module
  (ex. le scan JPA est dans `infra-persistence`).

## Configuration (`application.yml`)

| Clé | Valeur par défaut |
|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/hexagonal_db` |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `spring.jpa.hibernate.ddl-auto` | `update` (dev) — passer à `validate` + migrations en production |
| `spring.jpa.open-in-view` | `false` |
| Actuator exposé | `health, info, metrics, env` |
| springdoc | `/api-docs`, `/swagger-ui.html` |

## Lancer

```bash
docker compose up -d                 # PostgreSQL + Kafka + Kafka UI (à la racine)
mvn spring-boot:run -pl app          # ou : mvn clean package puis java -jar app/target/app-*.jar
```

L'application nécessite PostgreSQL. Kafka n'est contacté qu'à la publication d'un événement.

## Pour la production

- Désactiver Swagger : `springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false`.
- Restreindre les endpoints Actuator (`env` en particulier).
- Remplacer `ddl-auto: update` par des migrations versionnées.

## Sécurité (Spring Security)

Ajout : dépendance `infra-security`, propriétés `app.security.admin.*` (admin initial) et `app.security.jwt.*` (secret, durée).
