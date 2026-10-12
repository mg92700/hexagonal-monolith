package fr.monolith.application.user;

import fr.monolith.application.user.dto.UserResult;
import fr.monolith.application.user.mapper.UserApplicationMapper;
import fr.monolith.domain.user.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserApplicationMapperTest {

    private final UserApplicationMapper mapper = Mappers.getMapper(UserApplicationMapper.class);

    @Test
    void shouldMapUserToResult() {
        User user = User.create("john@example.com", "John", "Doe");

        UserResult result = mapper.toResult(user);

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.fullName()).isEqualTo("John Doe");
        assertThat(result.status()).isEqualTo(user.getStatus());
        assertThat(result.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void shouldMapUserListToResults() {
        User user = User.create("john@example.com", "John", "Doe");

        assertThat(mapper.toResultList(List.of(user)))
                .singleElement()
                .extracting(UserResult::email)
                .isEqualTo("john@example.com");
    }
}
