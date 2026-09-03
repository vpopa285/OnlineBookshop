package org.task.service;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {
    private static final Duration USER_TOKEN_TTL = Duration.ofHours(2);
    private static final Duration SERVICE_TOKEN_TTL = Duration.ofMinutes(10);

    private final JwtEncoder jwtEncoder;

    public JwtTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String createUserToken(Long userId, String username, boolean admin) {
        List<String> roles = admin ? List.of("USER", "ADMIN") : List.of("USER");
        return createToken(username, userId, roles, USER_TOKEN_TTL);
    }

    public String createServiceToken() {
        return createToken("user-service", null, List.of("SERVICE"), SERVICE_TOKEN_TTL);
    }

    private String createToken(String subject, Long userId, List<String> roles, Duration ttl) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("online-bookshop")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(subject)
                .claim("roles", roles);

        if (userId != null) {
            claims.claim("user_id", userId);
        }

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims.build())).getTokenValue();
    }
}
