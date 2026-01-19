package com.personal.planner.api;

import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing users.
 * Supports: user registration, modifying preferences, deleting user, and login.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/register")
    public void registerUser() {
        // Method to register a new user
    }

    @PutMapping("/preferences")
    public void modifyPreferences() {
        // Method to modify user preferences
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        // Method to delete a user
    }

    @PostMapping("/login")
    public void login() {
        // Method for user login
    }
}
