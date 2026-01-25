package com.personal.planner.api;

import com.personal.planner.domain.common.exception.AuthenticationException;
import com.personal.planner.domain.common.exception.InvalidRequestException;
import com.personal.planner.domain.user.User;
import com.personal.planner.domain.user.UserRepository;
import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.UserCreated;
import com.personal.planner.infra.security.JwtService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Controller for authentication operations (registration and login).
 * 
 * <p>Handles user registration and authentication. Uses domain-specific exceptions
 * for proper error handling. All validation errors return 400, authentication
 * failures return 401, and duplicate email registration returns 409.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DomainEventPublisher eventPublisher;

    /**
     * Registers a new user.
     * 
     * <p>Validates input:
     * - Email must be non-empty and valid format
     * - Password must be non-empty and meet minimum requirements
     * 
     * @param request registration request with email and password
     * @return authentication response with JWT token and user ID
     * @throws InvalidRequestException if validation fails (400)
     * @throws InvalidRequestException if email already exists (409)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody AuthRequest request) {
        // Input validation
        if (!StringUtils.hasText(request.getEmail())) {
            throw new InvalidRequestException("Email is required");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new InvalidRequestException("Password is required");
        }
        if (request.getPassword().length() < 6) {
            throw new InvalidRequestException("Password must be at least 6 characters");
        }

        // Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidRequestException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId());

        // Notify system that a user was created
        eventPublisher.publish(UserCreated.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .createdAt(Instant.now())
                .build());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponse.builder().token(token).userId(user.getId()).build()));
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * <p>Validates input:
     * - Email must be non-empty
     * - Password must be non-empty
     * 
     * @param request login request with email and password
     * @return authentication response with JWT token and user ID
     * @throws InvalidRequestException if validation fails (400)
     * @throws AuthenticationException if credentials are invalid (401)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        // Input validation
        if (!StringUtils.hasText(request.getEmail())) {
            throw new InvalidRequestException("Email is required");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new InvalidRequestException("Password is required");
        }

        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .<ResponseEntity<ApiResponse<AuthResponse>>>map(user -> {
                    String token = jwtService.generateToken(user.getId());
                    return ResponseEntity.ok(ApiResponse.success(
                            AuthResponse.builder().token(token).userId(user.getId()).build()));
                })
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
    }

    @Data
    public static class AuthRequest {
        private String email;
        private String password;
    }

    @Data
    @Builder
    public static class AuthResponse {
        private String token;
        private String userId;
    }
}
