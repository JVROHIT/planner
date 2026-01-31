package com.personal.planner.api;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.common.constants.TimeConstants;
import com.personal.planner.domain.common.exception.AuthenticationException;
import com.personal.planner.domain.common.exception.InvalidRequestException;
import com.personal.planner.domain.goal.Goal;
import com.personal.planner.domain.goal.GoalService;
import com.personal.planner.domain.plan.PlanningService;
import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.user.User;
import com.personal.planner.domain.user.UserRepository;
import com.personal.planner.events.DomainEventPublisher;
import com.personal.planner.events.UserCreated;
import com.personal.planner.infra.security.JwtService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
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
    private final ClockProvider clock;
    private final PlanningService planningService;
    private final GoalService goalService;

    /**
     * Registers a new user.
     * 
     * <p>Validates input:
     * - Email must be non-empty and valid format
     * - Password must be non-empty and meet minimum requirements
     * - Optional weekStart is normalized to the start of week (Monday)
     * - Optional onboarding goals are created for the new user
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

        String resolvedTimeZone = resolveTimeZone(request.getTimeZone());
        ZoneId userZone = ZoneId.of(resolvedTimeZone);

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(clock.nowInstant())
                .timeZone(resolvedTimeZone)
                .build();

        user = userRepository.save(user);

        LocalDate weekStart = resolveWeekStart(request.getWeekStart(), userZone);
        planningService.createWeeklyPlan(WeeklyPlan.builder()
                .userId(user.getId())
                .weekStart(weekStart)
                .build());

        createOnboardingGoals(user.getId(), request.getGoals());

        String token = jwtService.generateToken(user.getId());

        // Notify system that a user was created
        eventPublisher.publish(UserCreated.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .createdAt(clock.nowInstant())
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
        private String timeZone;
        private LocalDate weekStart;
        private List<OnboardingGoalRequest> goals;
    }

    @Data
    @Builder
    public static class AuthResponse {
        private String token;
        private String userId;
    }

    private String resolveTimeZone(String timeZone) {
        if (!StringUtils.hasText(timeZone)) {
            return TimeConstants.ZONE_ID.getId();
        }
        try {
            return ZoneId.of(timeZone).getId();
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid time zone: " + timeZone);
        }
    }

    private LocalDate resolveWeekStart(LocalDate requestedWeekStart, ZoneId zoneId) {
        LocalDate baseDate = requestedWeekStart != null ? requestedWeekStart : clock.today(zoneId);
        return baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void createOnboardingGoals(String userId, List<OnboardingGoalRequest> goals) {
        if (goals == null || goals.isEmpty()) {
            return;
        }

        for (OnboardingGoalRequest request : goals) {
            if (request == null || !StringUtils.hasText(request.getTitle())) {
                continue;
            }
            Goal goal = Goal.builder()
                    .title(request.getTitle())
                    .horizon(request.getHorizon())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .status(request.getStatus())
                    .userId(userId)
                    .build();
            goalService.createGoal(goal, userId);
        }
    }

    @Data
    public static class OnboardingGoalRequest {
        private String title;
        private Goal.Horizon horizon;
        private LocalDate startDate;
        private LocalDate endDate;
        private Goal.Status status;
    }
}
