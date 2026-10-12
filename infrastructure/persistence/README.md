# Module `infra-persistence`

> Artefact Maven : `fr.monolith:infra-persistence` — chemin : `infrastructure/persistence`
> Dépend de `domain` (+ Spring Data JPA, Hibernate, MapStruct).

## Rôle

**Adaptateur sortant** de persistance : implémente le port `UserRepository` du domaine avec
JPA/Hibernate sur PostgreSQL. C'est ici, et seulement ici, que l'on connaît les tables et le SQL.

## Ce que le module doit faire

| Responsabilité | Où |
|---|---|
| Implémenter `UserRepository` en traduisant domaine ↔ JPA | `user/adapter/UserPersistenceAdapter` |
| Décrire le schéma (table `users`, contraintes, timestamps) | `user/entity/UserJpaEntity` |
| Exposer l'accès Spring Data, **interne au module** | `user/repository/UserJpaRepository` |
| Convertir `User` ↔ `UserJpaEntity` | `user/mapper/UserPersistenceMapper` (MapStruct) |
| Déclarer lui-même le scan des entités et repositories | `config/PersistenceConfiguration` |

## Ce que le module ne doit PAS faire

- Aucune règle métier (pas de `activate()` ici).
- Ne pas exposer `UserJpaEntity` ni `UserJpaRepository` en dehors du module : seul l'adapter est utilisé par l'application.

## Structure

```
fr.monolith.infrastructure.persistence
├── config/      PersistenceConfiguration   @EnableJpaRepositories + @EntityScan
└── user/
    ├── adapter/     UserPersistenceAdapter  implémente UserRepository
    ├── entity/      UserJpaEntity
    ├── repository/  UserJpaRepository       (existsByEmailIgnoreCase)
    └── mapper/      UserPersistenceMapper
```

## Points d'attention

- `PersistenceConfiguration` est nécessaire : Spring Boot ne scanne les repositories/entités que sous le package
  de la classe principale (`fr.monolith.app`), or ce module est dans `fr.monolith.infrastructure.persistence`.
- Le schéma est géré par `spring.jpa.hibernate.ddl-auto` (`update` en dev, défini dans `app`).
  En production : migrations Flyway/Liquibase + `validate`.
- Le driver PostgreSQL est déclaré dans `app`, pas ici.

## Tests

`UserPersistenceAdapterTest` : test unitaire de l'adapter (Mockito), sans base de données.

```bash
mvn test -pl infrastructure/persistence
```

## Remplacer / compléter la base

Créer un module `infra-<techno>` (ex. `infra-mongodb`) qui implémente `UserRepository`, et le substituer à
celui-ci dans `app/pom.xml`. Le domaine et l'application ne changent pas.

## Sécurité (Spring Security)

Ajout : colonnes `login` (unique), `password` (hash) et `role` sur `users` ; `findByLogin` / `existsByLogin`.
