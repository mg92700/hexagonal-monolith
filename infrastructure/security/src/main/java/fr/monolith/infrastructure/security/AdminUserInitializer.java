package fr.monolith.infrastructure.security;

import fr.monolith.domain.user.model.Role;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.port.outbound.PasswordHasher;
import fr.monolith.domain.user.port.outbound.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Au démarrage, crée l'utilisateur ADMIN s'il n'existe pas (par défaut admin / admin).
 * Le compte est directement ACTIVE. Aucun événement Kafka n'est publié.
 * Ne fait rien si le login existe déjà (le mot de passe n'est jamais écrasé).
 *
 * Changer le mot de passe en production via app.security.admin.password.
 */
@Slf4j
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String login;
    private final String password;
    private final String email;

    public AdminUserInitializer(UserRepository userRepository,
                                PasswordHasher passwordHasher,
                                @Value("${app.security.admin.login:admin}") String login,
                                @Value("${app.security.admin.password:admin}") String password,
                                @Value("${app.security.admin.email:admin@monolith.local}") String email) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.login = login;
        this.password = password;
        this.email = email;
    }

    @Override
    public void run(ApplicationArguments args) {
        String normalizedLogin = User.normalizeLogin(login);
        if (userRepository.existsByLogin(normalizedLogin)) {
            log.debug("Admin user '{}' already exists", normalizedLogin);
            return;
        }

        User admin = User.create(normalizedLogin, passwordHasher.hash(password), Role.ADMIN,
                email, "Admin", "Admin").activate();
        userRepository.save(admin);
        log.warn("Default admin user '{}' created — change its password in production", normalizedLogin);
    }
}