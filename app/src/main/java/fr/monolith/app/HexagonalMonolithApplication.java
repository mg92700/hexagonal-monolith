package fr.monolith.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée Spring Boot.
 *
 * Ce module assemble tous les autres modules.
 * Il ne contient aucune logique métier.
 *
 * Architecture hexagonale — résumé des couches :
 *
 *  ┌─────────────────────────────────────────────────────┐
 *  │  Adaptateurs Entrants (Primary / Driving)           │
 *  │  - infra-web    : REST Controllers                  │
 *  │  - (futur) consumers Kafka dans infra-stream        │
 *  ├─────────────────────────────────────────────────────┤
 *  │  Application (Use Cases / Services)                 │
 *  │  - Orchestration via ports inbound                  │
 *  │  - Délégation aux ports outbound                    │
 *  ├─────────────────────────────────────────────────────┤
 *  │  Domain (Cœur métier pur)                           │
 *  │  - Entités, règles métier, ports (interfaces)       │
 *  ├─────────────────────────────────────────────────────┤
 *  │  Adaptateurs Sortants (Secondary / Driven)          │
 *  │  - infra-persistence : JPA/PostgreSQL               │
 *  │  - infra-stream      : Kafka Producers              │
 *  └─────────────────────────────────────────────────────┘
 *
 * Pour ajouter un nouveau domaine :
 * 1. Créer le modèle + ports dans le module domain
 * 2. Créer le service dans le module application
 * 3. Créer les adapters dans les modules infra concernés
 *
 * Pour ajouter un nouveau type d'infra (ex: Redis) :
 * 1. Créer infrastructure/redis/pom.xml
 * 2. Ajouter le module dans le pom.xml racine
 * 3. Implémenter les ports outbound concernés
 * 4. Ajouter la dépendance dans app/pom.xml
 */
@SpringBootApplication(scanBasePackages = "fr.monolith")
public class HexagonalMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(HexagonalMonolithApplication.class, args);
    }
}
