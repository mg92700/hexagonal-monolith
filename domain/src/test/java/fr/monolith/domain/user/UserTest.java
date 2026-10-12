package fr.monolith.domain.user;

import fr.monolith.domain.user.exception.UserAlreadyExistsException;
import fr.monolith.domain.user.model.Role;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User — Règles métier domaine")
class UserTest {

    @Nested
    @DisplayName("Création")
    class Creation {

        @Test
        @DisplayName("doit créer un utilisateur avec statut PENDING")
        void shouldCreateUserWithPendingStatus() {
            User user = User.create("john", "hash", Role.USER, "john@example.com", "John", "Doe");

            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(user.getEmail()).isEqualTo("john@example.com");
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getId()).isNotNull();
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("doit utiliser le rôle USER par défaut")
        void shouldDefaultToUserRole() {
            User user = User.create("john", "hash", null, "john@example.com", "John", "Doe");

            assertThat(user.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("doit conserver le rôle ADMIN demandé")
        void shouldKeepAdminRole() {
            User user = User.create("root", "hash", Role.ADMIN, "root@example.com", "Root", "Admin");

            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("doit normaliser le login en minuscules et conserver le hash")
        void shouldNormalizeLoginAndKeepHash() {
            User user = User.create("  JoHn ", "$2a$hash", Role.USER, "john@example.com", "John", "Doe");

            assertThat(user.getLogin()).isEqualTo("john");
            assertThat(user.getPassword()).isEqualTo("$2a$hash");
        }

        @Test
        @DisplayName("doit rejeter un login ou un mot de passe vide")
        void shouldRejectBlankLoginOrPassword() {
            assertThatThrownBy(() -> User.create(" ", "hash", Role.USER, "a@b.fr", "A", "B"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Login");
            assertThatThrownBy(() -> User.create("a", " ", Role.USER, "a@b.fr", "A", "B"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Password");
        }

        @Test
        @DisplayName("doit générer un ID unique à chaque création")
        void shouldGenerateUniqueId() {
            User user1 = User.create("john", "hash", Role.USER, "a@example.com", "A", "A");
            User user2 = User.create("john", "hash", Role.USER, "b@example.com", "B", "B");

            assertThat(user1.getId()).isNotEqualTo(user2.getId());
        }
    }

    @Nested
    @DisplayName("Activation")
    class Activation {

        @Test
        @DisplayName("doit activer un utilisateur PENDING")
        void shouldActivatePendingUser() {
            User pending = User.create("john", "hash", Role.USER, "john@example.com", "John", "Doe");

            User activated = pending.activate();

            assertThat(activated.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("doit rejeter l'activation d'un utilisateur déjà ACTIVE")
        void shouldRejectActivationOfActiveUser() {
            User active = User.create("john", "hash", Role.USER, "john@example.com", "John", "Doe").activate();

            assertThatThrownBy(active::activate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot activate user with status");
        }
    }

    @Nested
    @DisplayName("Désactivation")
    class Deactivation {

        @Test
        @DisplayName("doit désactiver un utilisateur ACTIVE")
        void shouldDeactivateActiveUser() {
            User active = User.create("john", "hash", Role.USER, "john@example.com", "John", "Doe").activate();

            User inactive = active.deactivate();

            assertThat(inactive.getStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        @DisplayName("doit rejeter la désactivation d'un utilisateur PENDING")
        void shouldRejectDeactivationOfPendingUser() {
            User pending = User.create("john", "hash", Role.USER, "john@example.com", "John", "Doe");

            assertThatThrownBy(pending::deactivate)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Méthodes utilitaires")
    class Utilities {

        @Test
        @DisplayName("doit retourner le nom complet")
        void shouldReturnFullName() {
            User user = User.create("john", "hash", Role.USER, "john@example.com", "John", "Doe");

            assertThat(user.fullName()).isEqualTo("John Doe");
        }
    }
}
