package fr.monolith.infrastructure.persistence.user;

import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.infrastructure.persistence.user.adapter.UserPersistenceAdapter;
import fr.monolith.infrastructure.persistence.user.mapper.UserPersistenceMapper;
import fr.monolith.infrastructure.persistence.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fr.monolith.infrastructure.persistence.user.entity.UserJpaEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPersistenceAdapter — Tests unitaires")
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @Mock
    private UserPersistenceMapper mapper;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    private User sampleUser;
    private UserJpaEntity sampleEntity;

    @BeforeEach
    void setUp() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        sampleUser = User.builder()
                .id(id)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleEntity = UserJpaEntity.builder()
                .id(id)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("doit convertir et sauvegarder l'utilisateur")
        void shouldConvertAndSave() {
            when(mapper.toJpaEntity(sampleUser)).thenReturn(sampleEntity);
            when(jpaRepository.save(sampleEntity)).thenReturn(sampleEntity);
            when(mapper.toDomain(sampleEntity)).thenReturn(sampleUser);

            User result = adapter.save(sampleUser);

            assertThat(result).isEqualTo(sampleUser);
            verify(mapper).toJpaEntity(sampleUser);
            verify(jpaRepository).save(sampleEntity);
            verify(mapper).toDomain(sampleEntity);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("doit retourner l'utilisateur si trouvé")
        void shouldReturnUserIfFound() {
            UUID id = sampleUser.getId();
            when(jpaRepository.findById(id)).thenReturn(Optional.of(sampleEntity));
            when(mapper.toDomain(sampleEntity)).thenReturn(sampleUser);

            Optional<User> result = adapter.findById(id);

            assertThat(result).isPresent().contains(sampleUser);
        }

        @Test
        @DisplayName("doit retourner Optional.empty si non trouvé")
        void shouldReturnEmptyIfNotFound() {
            when(jpaRepository.findById(any())).thenReturn(Optional.empty());

            Optional<User> result = adapter.findById(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("doit retourner true si l'email existe en ignorant la casse")
        void shouldReturnTrueIfEmailExists() {
            when(jpaRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

            assertThat(adapter.existsByEmail("john@example.com")).isTrue();
        }

        @Test
        @DisplayName("doit retourner false si l'email n'existe pas")
        void shouldReturnFalseIfEmailNotFound() {
            when(jpaRepository.existsByEmailIgnoreCase("unknown@example.com")).thenReturn(false);

            assertThat(adapter.existsByEmail("unknown@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("doit déléguer la suppression au repository")
        void shouldDeleteUserById() {
            UUID id = sampleUser.getId();

            adapter.deleteById(id);

            verify(jpaRepository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("doit retourner la liste mappée")
        void shouldReturnMappedList() {
            when(jpaRepository.findAll()).thenReturn(List.of(sampleEntity));
            when(mapper.toDomainList(List.of(sampleEntity))).thenReturn(List.of(sampleUser));

            List<User> result = adapter.findAll();

            assertThat(result).hasSize(1).contains(sampleUser);
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("findByLogin doit mapper l'entité trouvée")
        void shouldFindByLogin() {
            when(jpaRepository.findByLogin("john")).thenReturn(Optional.of(sampleEntity));
            when(mapper.toDomain(sampleEntity)).thenReturn(sampleUser);

            assertThat(adapter.findByLogin("john")).contains(sampleUser);
        }

        @Test
        @DisplayName("findByLogin doit retourner vide si inconnu")
        void shouldReturnEmptyIfUnknownLogin() {
            when(jpaRepository.findByLogin("nobody")).thenReturn(Optional.empty());

            assertThat(adapter.findByLogin("nobody")).isEmpty();
        }

        @Test
        @DisplayName("existsByLogin doit déléguer au repository")
        void shouldDelegateExistsByLogin() {
            when(jpaRepository.existsByLogin("john")).thenReturn(true);

            assertThat(adapter.existsByLogin("john")).isTrue();
        }
    }
}