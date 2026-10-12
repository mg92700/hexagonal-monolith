# Module `application`

> Artefact Maven : `fr.monolith:application` — dépend de `domain` (+ Spring Context/TX, MapStruct).

## Rôle

Couche d'orchestration des cas d'usage. Elle **implémente les ports entrants** du domaine
et **appelle les ports sortants**, sans jamais connaître la technologie qui se trouve derrière.

## Ce que le module doit faire

| Responsabilité | Où |
|---|---|
| Implémenter `UserUseCase` (créer, lire, lister, activer, désactiver, supprimer) | `user/service/UserService` |
| Gérer les transactions (`@Transactional`, lecture seule par défaut) | `UserService` |
| Vérifier les pré-conditions d'orchestration (unicité de l'email, existence de l'utilisateur) | `UserService` |
| Déclencher les événements après une écriture réussie (`UserEventPublisher`) | `UserService` |
| Fournir les objets de sortie découplés du domaine (`UserResult`) et leur mapper | `user/dto`, `user/mapper` |

## Ce que le module ne doit PAS faire

- Pas de Spring MVC, pas de JPA (`@Entity`, `@Query`), pas de Kafka (`@KafkaListener`, `KafkaTemplate`).
- Pas de règle métier propre à l'entité : elle reste dans `domain` (`User.activate()`, etc.).
- Pas de dépendance vers `infrastructure/*`.

## Structure

```
fr.monolith.application.user
├── service/  UserService               implémente UserUseCase
├── dto/      UserResult                record de sortie (inclut fullName calculé)
└── mapper/   UserApplicationMapper     MapStruct : User → UserResult (+ liste)
```

## Flux type : création d'un utilisateur

1. `UserService.createUser` normalise l'email et refuse les doublons (`existsByEmail`, insensible à la casse).
2. `User.create(...)` applique les règles du domaine.
3. `UserRepository.save` persiste, puis `UserEventPublisher.publishUserCreated` notifie.

## Tests

`UserServiceTest` (Mockito, ports mockés) et `UserApplicationMapperTest` (MapStruct sans contexte Spring).

```bash
mvn test -pl application
```

## Sécurité (Spring Security)

Ajout : `UserService.createUser(login, rawPassword, role, ...)` vérifie l'unicité login/email et hache via `PasswordHasher` ; `UserResult` expose `login` et `role` (jamais le mot de passe).
