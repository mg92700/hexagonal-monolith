package fr.monolith.infrastructure.web.user;

import fr.monolith.application.user.dto.UserResult;
import fr.monolith.application.user.mapper.UserApplicationMapper;
import fr.monolith.domain.user.exception.UserNotFoundException;
import fr.monolith.domain.user.model.User;
import fr.monolith.domain.user.model.UserStatus;
import fr.monolith.domain.user.port.inbound.UserUseCase;
import fr.monolith.infrastructure.web.exception.GlobalExceptionHandler;
import fr.monolith.infrastructure.web.user.controller.UserController;
import fr.monolith.infrastructure.web.user.dto.CreateUserRequest;
import fr.monolith.infrastructure.web.user.dto.UserResponse;
import fr.monolith.infrastructure.web.user.mapper.UserWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = fr.monolith.infrastructure.web.WebTestApplication.class)
@AutoConfigureMockMvc
@Import({UserController.class, GlobalExceptionHandler.class})
@DisplayName("UserController — Tests intégration MockMvc")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private UserWebMapper webMapper;

    @MockitoBean
    private UserApplicationMapper applicationMapper;

    private User sampleUser;
    private UserResult sampleResult;
    private UserResponse sampleResponse;

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

        sampleResult = new UserResult(id, "john@example.com", "John", "Doe",
                "John Doe", UserStatus.PENDING, now, now);

        sampleResponse = new UserResponse(id, "john@example.com", "John", "Doe",
                "John Doe", UserStatus.PENDING, now, now);
    }

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUser {

        @Test
        @DisplayName("doit créer un utilisateur et retourner 201")
        void shouldCreateUserAndReturn201() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "john@example.com", "John", "Doe"
            );

            when(userUseCase.createUser(anyString(), anyString(), anyString()))
                    .thenReturn(sampleUser);
            when(applicationMapper.toResult(sampleUser)).thenReturn(sampleResult);
            when(webMapper.toResponse(sampleResult)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("doit retourner 400 si l'email est invalide")
        void shouldReturn400IfEmailInvalid() throws Exception {
            CreateUserRequest request = new CreateUserRequest("not-an-email", "John", "Doe");

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("doit retourner 400 si le prénom est vide")
        void shouldReturn400IfFirstNameBlank() throws Exception {
            CreateUserRequest request = new CreateUserRequest("john@example.com", "", "Doe");

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("doit retourner l'utilisateur et 200")
        void shouldReturnUserAnd200() throws Exception {
            UUID id = sampleUser.getId();

            when(userUseCase.getUserById(id)).thenReturn(sampleUser);
            when(applicationMapper.toResult(sampleUser)).thenReturn(sampleResult);
            when(webMapper.toResponse(sampleResult)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("doit retourner 404 si l'utilisateur est introuvable")
        void shouldReturn404IfNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(userUseCase.getUserById(id)).thenThrow(new UserNotFoundException(id));

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUsers {

        @Test
        @DisplayName("doit retourner la liste et 200")
        void shouldReturnListAnd200() throws Exception {
            when(userUseCase.getAllUsers()).thenReturn(List.of(sampleUser));
            when(applicationMapper.toResultList(any())).thenReturn(List.of(sampleResult));
            when(webMapper.toResponseList(any())).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].email").value("john@example.com"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("doit retourner 204 après suppression")
        void shouldReturn204AfterDelete() throws Exception {
            UUID id = sampleUser.getId();
            doNothing().when(userUseCase).deleteUser(id);

            mockMvc.perform(delete("/api/v1/users/{id}", id))
                    .andExpect(status().isNoContent());
        }
    }
}
