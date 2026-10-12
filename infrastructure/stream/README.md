# Module `infra-stream`

> Artefact Maven : `fr.monolith:infra-stream` — chemin : `infrastructure/stream`
> Dépend de `domain` (+ Spring Kafka via `spring-boot-starter-kafka`, MapStruct).

## Rôle

**Adaptateur sortant** de messagerie : implémente le port `UserEventPublisher` du domaine en publiant
des événements JSON sur Kafka.

## Ce que le module doit faire

| Responsabilité | Où |
|---|---|
| Implémenter `UserEventPublisher` (created / activated / deactivated) | `user/producer/UserKafkaProducer` |
| Définir le contrat des messages publiés (un record par événement, avec sa constante `TOPIC`) | `user/event/*` |
| Convertir `User` → événement (génère `eventId` et `occurredAt`) | `user/mapper/UserEventMapper` |

## Ce que le module ne doit PAS faire

- Aucune règle métier.
- Ne pas faire fuiter les classes d'événements vers le domaine : elles sont propres à ce module.

## Topics

| Topic | Événement | Déclencheur |
|---|---|---|
| `user.created` | `UserCreatedEvent` | création d'un utilisateur |
| `user.activated` | `UserActivatedEvent` | activation |
| `user.deactivated` | `UserDeactivatedEvent` | désactivation |

Clé de message : l'`id` de l'utilisateur. Sérialisation : `JacksonJsonSerializer` (Jackson 3), configurée dans
`app/src/main/resources/application.yml`.

## Structure

```
fr.monolith.infrastructure.stream.user
├── producer/  UserKafkaProducer
├── event/     UserCreatedEvent, UserActivatedEvent, UserDeactivatedEvent
└── mapper/    UserEventMapper
```

## Consommation (non implémentée)

Aucun consumer n'est actif : l'ancien `UserKafkaConsumer` était un squelette dont le `@KafkaListener` était
commenté, il a été supprimé. Pour consommer des messages, ajouter un `@KafkaListener` dans ce module qui
appelle le port `UserUseCase` (ajouter la dépendance `application`/`domain` nécessaire et la configuration
`spring.kafka.consumer.*` dans `app`).

## Tests

`UserKafkaProducerTest` : test unitaire (Mockito, `KafkaTemplate` mocké), sans broker.

```bash
mvn test -pl infrastructure/stream
```
