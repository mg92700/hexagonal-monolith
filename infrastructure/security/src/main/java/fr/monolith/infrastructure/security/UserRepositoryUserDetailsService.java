package fr.monolith.infrastructure.security;

import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.domain.user.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Charge les utilisateurs depuis le port {@link UserRepository}.
 *
 * - Autorité : ROLE_USER ou ROLE_ADMIN.
 * - Seuls les comptes au statut ACTIVE peuvent se connecter.
 */
@Service
@RequiredArgsConstructor
public class UserRepositoryUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        String login = username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getLogin())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }
}