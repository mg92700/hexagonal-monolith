package fr.monolith.infrastructure.persistence.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration JPA du module persistence.
 *
 * Les entités et repositories vivent hors du package de la classe principale
 * (fr.monolith.app) : le module déclare lui-même ce qu'il faut scanner,
 * l'application n'a rien à connaître de JPA.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "fr.monolith.infrastructure.persistence")
@EntityScan(basePackages = "fr.monolith.infrastructure.persistence")
public class PersistenceConfiguration {
}
