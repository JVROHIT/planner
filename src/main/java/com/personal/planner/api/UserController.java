package com.personal.planner.api;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.personal.planner.domain.user.UserRepository;

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
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @DeleteMapping("/{id}")
    @Deprecated(forRemoval = true)
    public void deleteUser(@PathVariable String id) {
        // delegate to service
        userRepository.deleteById(id);
    }
}
