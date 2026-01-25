package com.personal.planner.api;

import com.personal.planner.domain.user.UserRepository;
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
 * All responses are wrapped in {@link ApiResponse} for consistent error handling.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID to delete
     * @return success response wrapped in ApiResponse
     * @deprecated This endpoint is deprecated and will be removed.
     */
    @DeleteMapping("/{id}")
    @Deprecated(forRemoval = true)
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
