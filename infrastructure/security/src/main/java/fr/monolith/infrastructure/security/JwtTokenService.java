package fr.monolith.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Émet les access tokens JWT : subject = login, claim "roles" = autorités (ex. ROLE_ADMIN).
 */
@Service
public class JwtTokenService {

    public static final String ROLES_CLAIM = "roles";
    static final String ISSUER = "hexagonal-monolith";

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Value("${app.security.jwt.expiration:PT1H}") Duration expiration) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public AccessToken issue(UserDetails user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .claim(ROLES_CLAIM, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .build();
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AccessToken(token, expiration.toSeconds());
    }

    public record AccessToken(String value, long expiresInSeconds) {
    }
}