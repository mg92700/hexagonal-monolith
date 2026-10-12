package fr.monolith.infrastructure.security;

import fr.monolith.domain.user.model.Role;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.domain.user.port.outbound.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepositoryUserDetailsService")
class UserRepositoryUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRepositoryUserDetailsService service;

    private User user(Role role, UserStatus status) {
        return User.builder().login("john").password("hash").role(role).status(status).build();
    }

    @Test
    @DisplayName("doit exposer login, hash et autorité ROLE_ADMIN (login insensible à la casse)")
    void shouldLoadUser() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user(Role.ADMIN, UserStatus.ACTIVE)));

        UserDetails details = service.loadUserByUsername(" John ");

        assertThat(details.getUsername()).isEqualTo("john");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("doit désactiver les comptes non ACTIVE")
    void shouldDisableNonActiveAccounts() {
        when(userRepository.findByLogin("john")).thenReturn(Optional.of(user(Role.USER, UserStatus.PENDING)));

        assertThat(service.loadUserByUsername("john").isEnabled()).isFalse();
    }

    @Test
    @DisplayName("doit lever UsernameNotFoundException si inconnu")
    void shouldThrowIfUnknown() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}