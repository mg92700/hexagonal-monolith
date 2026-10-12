package fr.monolith.infrastructure.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/v1/auth/login : échange login + mot de passe contre un JWT.
 * Même réponse 401 pour login inconnu, mauvais mot de passe ou compte non ACTIVE.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public record LoginRequest(@NotBlank String login, @NotBlank String password) {
    }

    public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(request.login());
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        JwtTokenService.AccessToken token = tokenService.issue(user);
        return new LoginResponse(token.value(), "Bearer", token.expiresInSeconds());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid login or password");
        problem.setTitle("Unauthorized");
        return problem;
    }
}