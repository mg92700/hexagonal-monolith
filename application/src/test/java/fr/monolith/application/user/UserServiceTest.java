package fr.monolith.application.user;

import fr.monolith.application.user.service.UserService;
import fr.monolith.domain.user.exception.UserAlreadyExistsException;
import fr.monolith.domain.user.exception.UserNotFoundException;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.domain.user.port.outbound.UserEventPublisher;
import fr.monolith.domain.user.port.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Tests unitaires")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.create("john@example.com", "John", "Doe");
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("doit créer et sauvegarder un utilisateur")
        void shouldCreateAndSaveUser() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            doNothing().when(userEventPublisher).publishUserCreated(any());

            User result = userService.createUser("john@example.com", "John", "Doe");

            assertThat(result.getEmail()).isEqualTo("john@example.com");
            verify(userRepository).save(any(User.class));
            verify(userEventPublisher).publishUserCreated(sampleUser);
        }

        @Test
        @DisplayName("doit lever UserAlreadyExistsException si email déjà utilisé")
        void shouldThrowIfEmailAlreadyExists() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser("john@example.com", "John", "Doe"))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("john@example.com");

            verify(userRepository, never()).save(any());
            verify(userEventPublisher, never()).publishUserCreated(any());
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("doit retourner l'utilisateur existant")
        void shouldReturnExistingUser() {
            UUID id = sampleUser.getId();
            when(userRepository.findById(id)).thenReturn(Optional.of(sampleUser));

            User result = userService.getUserById(id);

            assertThat(result).isEqualTo(sampleUser);
        }

        @Test
        @DisplayName("doit lever UserNotFoundException si introuvable")
        void shouldThrowIfNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("doit retourner la liste complète")
        void shouldReturnAllUsers() {
            List<User> users = List.of(sampleUser);
            when(userRepository.findAll()).thenReturn(users);

            List<User> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("activateUser")
    class ActivateUser {

        @Test
        @DisplayName("doit activer un utilisateur PENDING")
        void shouldActivatePendingUser() {
            UUID id = sampleUser.getId();
            when(userRepository.findById(id)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(userEventPublisher).publishUserActivated(any());

            User result = userService.activateUser(id);

            assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
            verify(userEventPublisher).publishUserActivated(any());
        }

        @Test
        @DisplayName("doit lever UserNotFoundException si utilisateur inexistant")
        void shouldThrowIfUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.activateUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("doit supprimer un utilisateur existant")
        void shouldDeleteExistingUser() {
            UUID id = sampleUser.getId();
            when(userRepository.findById(id)).thenReturn(Optional.of(sampleUser));
            doNothing().when(userRepository).deleteById(id);

            assertThatNoException().isThrownBy(() -> userService.deleteUser(id));
            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("doit lever UserNotFoundException si utilisateur inexistant")
        void shouldThrowIfNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateUser")
    class DeactivateUser {

        @Test
        @DisplayName("doit désactiver un utilisateur")
        void shouldDeactivateUser() {
            UUID id = sampleUser.getId();
            User activeUser = sampleUser.withStatus(UserStatus.ACTIVE);
            when(userRepository.findById(id)).thenReturn(Optional.of(activeUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(userEventPublisher).publishUserDeactivated(any());

            User result = userService.deactivateUser(id);

            assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
            verify(userEventPublisher).publishUserDeactivated(any());
        }

        @Test
        @DisplayName("doit lever UserNotFoundException si utilisateur inexistant")
        void shouldThrowIfUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deactivateUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
