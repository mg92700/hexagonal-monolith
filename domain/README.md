# Module `domain`

> Artefact Maven : `fr.monolith:domain` — dépend de **rien** (hors Lombok, à la compilation).

## Rôle

Cœur métier pur de l'architecture hexagonale. C'est la seule couche qui ne change jamais
quand on remplace un framework, une base de données ou un broker.

Ce module **définit** :

- le **modèle** métier et ses règles (`User`, `UserStatus`) ;
- les **ports entrants** (ce que le métier offre) : `UserUseCase` ;
- les **ports sortants** (ce dont le métier a besoin) : `UserRepository`, `UserEventPublisher` ;
- les **exceptions métier** : `UserNotFoundException`, `UserAlreadyExistsException`.

## Ce que le module doit faire

| Responsabilité | Où |
|---|---|
| Porter les invariants et transitions d'état (`PENDING → ACTIVE → INACTIVE`) | `user/model/User` |
| Normaliser/valider à la création (`User.create` : email en minuscules, noms non vides) | `user/model/User` |
| Déclarer les contrats vers l'extérieur sans les implémenter | `user/port/inbound`, `user/port/outbound` |
| Exprimer les erreurs métier | `user/exception` |

## Ce que le module ne doit PAS faire

- Aucune annotation ou classe Spring, JPA, Jackson, Kafka.
- Aucune dépendance vers un autre module du projet.
- Aucune notion de HTTP, de SQL ou de topic.

## Structure

```
fr.monolith.domain.user
├── model/        User (immuable : @Builder + @With), UserStatus
├── port/inbound/ UserUseCase            ← implémenté par `application`
├── port/outbound UserRepository         ← implémenté par `infra-persistence`
│                 UserEventPublisher     ← implémenté par `infra-stream`
└── exception/    UserNotFoundException, UserAlreadyExistsException
```

## Règles métier actuelles

- `User.create(email, firstName, lastName)` : statut initial `PENDING`, email normalisé (`trim` + minuscules).
- `activate()` : autorisé uniquement depuis `PENDING`, sinon `IllegalStateException`.
- `deactivate()` : autorisé uniquement depuis `ACTIVE`, sinon `IllegalStateException`.
- `UserStatus.DELETED` existe dans l'énumération mais n'est aujourd'hui atteint par aucune transition
  (la suppression est physique, via `UserRepository.deleteById`).

## Ajouter un concept métier

1. Créer un package `fr.monolith.domain.<concept>` avec `model/`, `port/inbound/`, `port/outbound/`, `exception/`.
2. Ne mettre ici que du Java pur ; les implémentations vont dans `application` et `infrastructure/*`.

## Tests

`UserTest`, `ExceptionsTest` — tests unitaires purs (JUnit 5 + AssertJ), sans contexte Spring.

```bash
mvn test -pl domain
```

## Sécurité (Spring Security)

Ajout : enum Role, champs login/password (hash)/ole sur User, port sortant PasswordHasher, UserRepository.findByLogin/existsByLogin, UserAlreadyExistsException générique (champ + valeur).
