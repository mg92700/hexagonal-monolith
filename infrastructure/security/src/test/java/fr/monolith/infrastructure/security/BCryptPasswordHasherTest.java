package fr.monolith.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BCryptPasswordHasher")
class BCryptPasswordHasherTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(encoder);

    @Test
    @DisplayName("doit produire un hash BCrypt différent du mot de passe et vérifiable")
    void shouldProduceVerifiableBcryptHash() {
        String hash = hasher.hash("S3cretPassw0rd");

        assertThat(hash).startsWith("$2").isNotEqualTo("S3cretPassw0rd");
        assertThat(encoder.matches("S3cretPassw0rd", hash)).isTrue();
    }

    @Test
    @DisplayName("doit saler : deux hashs du même mot de passe diffèrent")
    void shouldSalt() {
        assertThat(hasher.hash("same")).isNotEqualTo(hasher.hash("same"));
    }
}