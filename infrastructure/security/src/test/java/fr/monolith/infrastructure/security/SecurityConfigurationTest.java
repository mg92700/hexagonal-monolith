package fr.monolith.infrastructure.security;

import fr.monolith.domain.user.model.Role;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.domain.user.port.outbound.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SecurityConfigurationTest.TestApp.class)
@AutoConfigureMockMvc
@DisplayName("SecurityConfiguration — règles d'accès")
class SecurityConfigurationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({SecurityConfiguration.class, UserRepositoryUserDetailsService.class, Probe.class})
    static class TestApp {
    }

    @RestController
    @RequestMapping
    static class Probe {
        @org.springframework.web.bind.annotation.RequestMapping({
                "/api/v1/users", "/api/v1/users/x", "/actuator/health", "/actuator/env", "/swagger-ui.html", "/api-docs"})
        String ok() {
            return "ok";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("sans authentification : 401 sur l'API et sur Swagger")
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api-docs")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("le health actuator est public")
    void healthShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER : lecture autorisée, écriture et actuator interdits")
    void userShouldOnlyRead() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/users")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/users/x")).andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator/env")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN : écriture et actuator autorisés")
    void adminShouldWrite() throws Exception {
        mockMvc.perform(post("/api/v1/users")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/env")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("HTTP Basic : identifiants valides acceptés, mot de passe erroné refusé")
    void basicAuthShouldUseStoredHash() throws Exception {
        User admin = User.builder().login("admin").password(passwordEncoder.encode("admin"))
                .role(Role.ADMIN).status(UserStatus.ACTIVE).build();
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/api/v1/users").with(httpBasic("admin", "admin"))).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/users").with(httpBasic("admin", "wrong"))).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HTTP Basic : compte PENDING refusé")
    void pendingAccountShouldBeRejected() throws Exception {
        User pending = User.builder().login("bob").password(passwordEncoder.encode("password1"))
                .role(Role.USER).status(UserStatus.PENDING).build();
        when(userRepository.findByLogin("bob")).thenReturn(Optional.of(pending));

        mockMvc.perform(get("/api/v1/users").with(httpBasic("bob", "password1")))
                .andExpect(status().isUnauthorized());
    }
}