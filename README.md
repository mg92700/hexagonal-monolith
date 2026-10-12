# Hexagonal Monolith — Spring Boot 4

Monolithe modulaire Maven en **architecture hexagonale** (ports & adapters), prêt à évoluer
vers un découpage en microservices.

**Stack** : Java 21 · Spring Boot 4.1 · Spring Data JPA/Hibernate 7 · PostgreSQL · Kafka · MapStruct · springdoc-openapi 3.

---

## Modules

Chaque module a sa propre documentation (rôle, responsabilités, interdits) :

| Module | Artefact | Rôle | Doc |
|---|---|---|---|
| `domain` | `domain` | Cœur métier pur : modèle, règles, **ports** | [domain/README.md](domain/README.md) |
| `application` | `application` | Cas d'usage (implémente les ports entrants) | [application/README.md](application/README.md) |
| `infrastructure/persistence` | `infra-persistence` | Adaptateur sortant JPA/PostgreSQL | [README](infrastructure/persistence/README.md) |
| `infrastructure/web` | `infra-web` | Adaptateur entrant REST + Swagger | [README](infrastructure/web/README.md) |
| `infrastructure/stream` | `infra-stream` | Adaptateur sortant Kafka (events) | [README](infrastructure/stream/README.md) |
| `infrastructure/security` | `infra-security` | Spring Security : Basic auth, rôles, BCrypt, admin initial | [README](infrastructure/security/README.md) |
| `app` | `app` | Point d'entrée Spring Boot, configuration | [app/README.md](app/README.md) |

Historique de la migration Spring Boot 4 et du nettoyage : [docs/MIGRATION-SPRING-BOOT-4.md](docs/MIGRATION-SPRING-BOOT-4.md).

### Règle de dépendances

```
app ──▶ infra-web ──────▶ application ──▶ domain
  │                                          ▲
  ├────▶ infra-persistence ──────────────────┤
  └────▶ infra-stream ───────────────────────┘
```

`domain` ne dépend de rien. Les adaptateurs ne dépendent jamais les uns des autres.

```
hexagonal-monolith/
├── domain/                  user/{model, port/inbound, port/outbound, exception}
├── application/             user/{service, dto, mapper}
├── infrastructure/
│   ├── persistence/         config/, user/{adapter, entity, repository, mapper}
│   ├── web/                 exception/, user/{controller, dto, mapper}
│   └── stream/              user/{producer, event, mapper}
├── app/                     HexagonalMonolithApplication + application.yml
├── docs/                    Documentation transverse
└── docker-compose.yml       PostgreSQL + Kafka + Kafka UI
```

---

## Flux de données — création d'un utilisateur

```
POST /api/v1/users
   │
   ▼  [infra-web] UserController  (CreateUserRequest validé)
   │
   ▼  [domain port] UserUseCase.createUser(email, firstName, lastName)
   │      implémenté par [application] UserService
   │        ├─ UserRepository.existsByEmail()     ──▶ [infra-persistence] UserPersistenceAdapter
   │        ├─ User.create()                        règle métier dans le domaine
   │        ├─ UserRepository.save()              ──▶ UserPersistenceMapper → UserJpaRepository
   │        └─ UserEventPublisher.publishUserCreated() ──▶ [infra-stream] UserKafkaProducer
   │                                                      → topic user.created
   ▼  User → UserResult (UserApplicationMapper) → UserResponse (UserWebMapper)
   │
HTTP 201 Created
```

---

## Démarrage rapide

### Prérequis
Java 21, Maven 3.9+, Docker.

### 1. Stack locale (PostgreSQL + Kafka)
```bash
docker compose up -d
```
Les ports 5432 (PostgreSQL) et 9092 (Kafka) doivent être libres.
Arrêter / supprimer la stack :
```bash
docker compose stop          # arrête les conteneurs (données conservées)
docker compose down          # arrête et supprime les conteneurs (données conservées dans le volume)
docker compose down -v       # supprime aussi le volume postgres_data : TOUTES les données de la base sont perdues
docker compose down -v --rmi all   # + supprime les images téléchargées
```
Pour tout supprimer puis repartir d'une stack neuve :
```bash
docker compose down -v && docker compose up -d
```
(sous PowerShell : `docker compose down -v; docker compose up -d`)

### 2. Lancer l'application
```bash
mvn spring-boot:run -pl app
```
PostgreSQL est obligatoire. Les tables sont créées automatiquement (`ddl-auto: update`).

### 3. Tests et build
```bash
mvn test                      # tous les modules
mvn test -pl domain           # un module
mvn clean verify              # build complet + JaCoCo (couverture minimale : 20 %)
```

---

## API REST

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/users` | Créer un utilisateur |
| GET | `/api/v1/users` | Lister |
| GET | `/api/v1/users/{id}` | Récupérer par ID |
| PUT | `/api/v1/users/{id}/activate` | Activer (`PENDING → ACTIVE`) |
| PUT | `/api/v1/users/{id}/deactivate` | Désactiver (`ACTIVE → INACTIVE`) |
| DELETE | `/api/v1/users/{id}` | Supprimer |

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Docs** : http://localhost:8080/api-docs
- **Health** : http://localhost:8080/actuator/health
- **Kafka UI** : http://localhost:8090

## Sécurité

- Authentification **HTTP Basic** (stateless) sur toute l'API **et sur Swagger** (`/swagger-ui.html`, `/api-docs`) ; bouton « Authorize » dans Swagger UI.
- `User` possède `login` (unique, insensible à la casse), `password` (hash BCrypt en BDD, jamais renvoyé) et `role` (`USER` par défaut, `ADMIN`).
- Autorisations : `GET /api/v1/users/**` = tout utilisateur authentifié ; création / activation / désactivation / suppression = `ADMIN` ; `/actuator/health` public, autres endpoints actuator = `ADMIN`.
- Seuls les comptes au statut `ACTIVE` peuvent se connecter : un nouvel utilisateur est `PENDING` jusqu'à ce qu'un admin appelle `PUT /api/v1/users/{id}/activate`.
- Au démarrage, un admin `admin` / `admin` est créé s'il n'existe pas (propriétés `app.security.admin.*`, variables `ADMIN_LOGIN`, `ADMIN_PASSWORD`, `ADMIN_EMAIL`). **Changer ce mot de passe en production.**
- Migration : la table `users` existante n'a pas les colonnes `login`/`password`/`role` (NOT NULL). Repartir d'une base vide (`docker compose down -v` puis `docker compose up -d`) ou vider la table `users`.
## Topics Kafka

| Topic | Producteur | Description |
|---|---|---|
| `user.created` | `UserKafkaProducer` | Utilisateur créé |
| `user.activated` | `UserKafkaProducer` | Utilisateur activé |
| `user.deactivated` | `UserKafkaProducer` | Utilisateur désactivé |

---

## Ajouter un nouveau domaine métier (ex : Product)

1. **domain** — `fr.monolith.domain.product` : `model/Product`, `port/inbound/ProductUseCase`, `port/outbound/ProductRepository`.
2. **application** — `fr.monolith.application.product` : `service/ProductService` (implémente `ProductUseCase`), `dto/`, `mapper/`.
3. **infra-persistence** — `...persistence.product` : `entity/`, `repository/`, `adapter/ProductPersistenceAdapter`, `mapper/`.
4. **infra-web** — `...web.product` : `controller/`, `dto/`, `mapper/`.
5. **infra-stream** (si événements) — `...stream.product` : `producer/`, `event/`, `mapper/`.

## Ajouter une nouvelle infrastructure (ex : Redis)

1. Créer `infrastructure/redis/pom.xml` (parent `hexagonal-monolith`, `<relativePath>../../pom.xml</relativePath>`, artefact `infra-redis`).
2. Déclarer `<module>infrastructure/redis</module>` et `infra-redis` dans le `dependencyManagement` du `pom.xml` racine.
3. Ajouter `infra-redis` dans les dépendances de `app/pom.xml`.
4. Implémenter les ports sortants concernés du domaine.
5. Documenter le module dans son `README.md`.

---

## Notes d'exploitation

- **Schéma BDD** : `ddl-auto: update` convient en développement uniquement. En production, utiliser Flyway/Liquibase et `validate`.
- **Swagger / Actuator** : à restreindre en production (voir [app/README.md](app/README.md)).
- Il n'existe plus de profil `local` (H2) : il était inopérant (H2 absent du classpath d'exécution, `KafkaTemplate` requis).
