package fr.monolith.domain.user;

import fr.monolith.domain.user.exception.UserAlreadyExistsException;
import fr.monolith.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exceptions")
class ExceptionsTest {

    @Test
    @DisplayName("UserNotFoundException doit exposer l'userId")
    void shouldExposeUserId() {
        UUID id = UUID.randomUUID();
        UserNotFoundException ex = new UserNotFoundException(id);

        assertThat(ex.getUserId()).isEqualTo(id);
        assertThat(ex.getMessage()).contains(id.toString());
    }

    @Test
    @DisplayName("UserAlreadyExistsException doit exposer le champ et la valeur")
    void shouldExposeEmail() {
        UserAlreadyExistsException ex = UserAlreadyExistsException.withEmail("john@example.com");

        assertThat(ex.getField()).isEqualTo("email");
        assertThat(ex.getValue()).isEqualTo("john@example.com");
        assertThat(ex.getMessage()).contains("john@example.com");
    }

    @Test
    @DisplayName("UserAlreadyExistsException par login doit exposer le login")
    void shouldExposeLogin() {
        UserAlreadyExistsException ex = UserAlreadyExistsException.withLogin("admin");

        assertThat(ex.getField()).isEqualTo("login");
        assertThat(ex.getValue()).isEqualTo("admin");
        assertThat(ex.getMessage()).contains("login").contains("admin");
    }
}