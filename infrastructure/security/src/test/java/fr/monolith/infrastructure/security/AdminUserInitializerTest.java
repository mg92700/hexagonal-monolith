package fr.monolith.infrastructure.security;

import fr.monolith.domain.user.model.Role;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.domain.user.port.outbound.PasswordHasher;
import fr.monolith.domain.user.port.outbound.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserInitializer")
class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private AdminUserInitializer initializer() {
        return new AdminUserInitializer(userRepository, passwordHasher, "admin", "admin", "admin@monolith.local");
    }

    @Test
    @DisplayName("doit créer l'admin ACTIVE avec mot de passe haché s'il n'existe pas")
    void shouldCreateAdminIfMissing() {
        when(userRepository.existsByLogin("admin")).thenReturn(false);
        when(passwordHasher.hash("admin")).thenReturn("hashed");

        initializer().run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getLogin()).isEqualTo("admin");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("ne doit rien faire si l'admin existe déjà")
    void shouldDoNothingIfAdminExists() {
        when(userRepository.existsByLogin("admin")).thenReturn(true);

        initializer().run(null);

        verify(userRepository, never()).save(any());
    }
}