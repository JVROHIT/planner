package com.personal.planner.api;

import com.personal.planner.domain.common.ClockProvider;
import com.personal.planner.domain.plan.WeeklyPlan;
import com.personal.planner.domain.plan.WeeklyPlanRepository;
import com.personal.planner.domain.streak.StreakRepository;
import com.personal.planner.domain.streak.StreakState;
import com.personal.planner.domain.user.User;
import com.personal.planner.domain.user.UserRepository;
import com.personal.planner.infra.security.JwtService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.IsoFields;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final StreakRepository streakRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ClockProvider clock;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .build();

        user = userRepository.save(user);

        // Initialize: Empty WeeklyPlan for current week
        int week = clock.today().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = clock.today().get(IsoFields.WEEK_BASED_YEAR);

        WeeklyPlan initialPlan = WeeklyPlan.builder()
                .userId(user.getId())
                .weekNumber(week)
                .year(year)
                .build();
        weeklyPlanRepository.save(initialPlan);

        // Initialize: Empty StreakState
        StreakState initialState = StreakState.builder()
                .userId(user.getId())
                .currentStreak(0)
                .build();
        streakRepository.save(initialState);

        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.ok(AuthResponse.builder().token(token).userId(user.getId()).build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .<ResponseEntity<?>>map(user -> {
                    String token = jwtService.generateToken(user.getId());
                    return ResponseEntity.ok(AuthResponse.builder().token(token).userId(user.getId()).build());
                })
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
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
