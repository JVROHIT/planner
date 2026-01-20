package com.personal.planner.api;

import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.domain.task.TaskService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing task intent.
 * <p>
 * "Controllers do not contain business logic."
 * "They validate input, call domain services, and shape responses."
 * "They must never compute analytics, streaks, or goal progress."
 * </p>
 * <p>
 * Boundaries:
 * - Can create/update/delete Tasks.
 * - Must never touch DailyPlan, WeeklyPlan, streaks, or analytics.
 * </p>
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    public TaskController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam String userId) {
        // // Call Query Logic (direct Repository access allowed for pure reads in this
        // slice)
        return ResponseEntity.ok(taskRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        // validate input
        // map to domain
        // delegate to service
        // map to response DTO
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody TaskRequest request) {
        // validate input
        // delegate to service
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        // delegate to service
        return ResponseEntity.noContent().build();
    }

    /**
     * DTO for incoming Task requests.
     */
    @Data
    public static class TaskRequest {
        private String description;
        private String userId;
    }

    /**
     * DTO for Task responses.
     */
    @Data
    public static class TaskResponse {
        private String id;
        private String description;
        private boolean completed;
    }
}
