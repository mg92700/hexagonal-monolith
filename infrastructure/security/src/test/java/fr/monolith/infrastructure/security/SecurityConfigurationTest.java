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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SecurityConfigurationTest.TestApp.class, properties = {
        "app.security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "app.security.jwt.expiration=PT5M"})
@AutoConfigureMockMvc
@DisplayName("SecurityConfiguration — règles d'accès")
class SecurityConfigurationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({SecurityConfiguration.class, JwtConfiguration.class, JwtTokenService.class, AuthController.class,
            UserRepositoryUserDetailsService.class, Probe.class})
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
    @DisplayName("sans authentification : 401 sur l'API")
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("health, Swagger UI et api-docs sont publics")
    void publicEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
        mockMvc.perform(get("/api-docs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER : lecture autorisée, écriture et actuator interdits")
    void userShouldOnlyRead() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isOk());
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

    private void givenUser(String login, String password, Role role, UserStatus status) {
        User user = User.builder().login(login).password(passwordEncoder.encode(password))
                .role(role).status(status).build();
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
    }

    private String login(String login, String password) throws Exception {
        String body = "{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}";
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
    }

    private String token(String login, String password) throws Exception {
        String json = login(login, password);
        return json.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }

    @Test
    @DisplayName("login : renvoie un JWT Bearer avec expiration")
    void loginShouldReturnJwt() throws Exception {
        givenUser("admin", "admin", Role.ADMIN, UserStatus.ACTIVE);

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"Admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(300));
    }

    @Test
    @DisplayName("login : 401 si mot de passe erroné, login inconnu ou compte PENDING")
    void loginShouldRejectInvalidCredentials() throws Exception {
        givenUser("admin", "admin", Role.ADMIN, UserStatus.ACTIVE);
        givenUser("bob", "password1", Role.USER, UserStatus.PENDING);
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());

        for (String[] c : new String[][]{{"admin", "wrong"}, {"nobody", "x"}, {"bob", "password1"}}) {
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"login\":\"" + c[0] + "\",\"password\":\"" + c[1] + "\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("login : 400 si champs vides")
    void loginShouldValidateBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("le token d'un ADMIN permet l'écriture, celui d'un USER seulement la lecture")
    void tokenShouldCarryRole() throws Exception {
        givenUser("admin", "admin", Role.ADMIN, UserStatus.ACTIVE);
        givenUser("bob", "password1", Role.USER, UserStatus.ACTIVE);
        String admin = "Bearer " + token("admin", "admin");
        String bob = "Bearer " + token("bob", "password1");

        mockMvc.perform(post("/api/v1/users").header("Authorization", admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/users").header("Authorization", bob)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/users").header("Authorization", bob)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un token invalide ou falsifié est refusé (401)")
    void invalidTokenShouldBeRejected() throws Exception {
        givenUser("admin", "admin", Role.ADMIN, UserStatus.ACTIVE);
        String valid = token("admin", "admin");

        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer garbage"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + valid.substring(0, valid.length() - 3) + "abc"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Basic YWRtaW46YWRtaW4="))
                .andExpect(status().isUnauthorized());
    }
}