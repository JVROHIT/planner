package com.personal.planner.api;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user management and preferences.
 * <p>
 * "Controllers do not contain business logic."
 * "They validate input, call domain services, and shape responses."
 * "They must never compute analytics, streaks, or goal progress."
 * </p>
 * <p>
 * Boundaries:
 * - Authentication and preferences only.
 * - No domain behavior.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        // validate input
        // delegate to service
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // delegate to service
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<?> modifyPreferences(@PathVariable String id, @RequestBody PreferencesRequest request) {
        // validate input
        // delegate to service
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        // delegate to service
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class RegistrationRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class PreferencesRequest {
        private String theme;
        private boolean notificationsEnabled;
    }
}
