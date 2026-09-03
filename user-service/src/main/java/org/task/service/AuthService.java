package org.task.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.task.dto.request.LoginRequest;
import org.task.dto.request.UserRequest;
import org.task.dto.response.TokenResponse;
import org.task.model.User;
import org.task.repositories.UserRepository;

@Service
public class AuthService {
    private static final long USER_TOKEN_EXPIRES_IN_SECONDS = 7200;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordMatches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return tokenFor(user);
    }

    public TokenResponse register(UserRequest request) {
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .amount(0)
                .restriction(false)
                .isAdmin(false)
                .build();

        return tokenFor(userRepository.save(user));
    }

    private TokenResponse tokenFor(User user) {
        return new TokenResponse(
                "Bearer",
                jwtTokenService.createUserToken(user.getId(), user.getUsername(), user.isAdmin()),
                USER_TOKEN_EXPIRES_IN_SECONDS
        );
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }
}
