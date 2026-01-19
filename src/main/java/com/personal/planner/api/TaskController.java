package com.personal.planner.api;

import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing tasks.
 * Supports: create task, update task, delete task.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @PostMapping
    public void createTask() {
        // Method to create a task
    }

    @PutMapping("/{id}")
    public void updateTask(@PathVariable String id) {
        // Method to update a task
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable String id) {
        // Method to delete a task
    }
}
