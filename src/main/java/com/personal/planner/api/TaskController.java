package com.personal.planner.api;

import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.domain.task.TaskService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing task intent (user's planned tasks).
 * All operations are scoped to the authenticated user.
 * 
 * <p>This controller handles CRUD operations for tasks. Ownership validation
 * is performed by the domain service layer.</p>
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

    /**
     * Retrieves all tasks for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @return list of tasks belonging to the user
     */
    @GetMapping
    public ResponseEntity<?> getTasks(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(taskRepository.findByUserId(userId));
    }

    /**
     * Creates a new task for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param request the task creation request
     * @return the created task
     */
    @PostMapping
    public ResponseEntity<?> createTask(@AuthenticationPrincipal String userId, @RequestBody TaskRequest request) {
        Task task = Task.builder()
                .description(request.description)
                .userId(userId)
                .goalId(request.getGoalId())
                .keyResultId(request.getKeyResultId())
                .build();
        return ResponseEntity.ok(taskService.createTask(task));
    }

    /**
     * Updates an existing task.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the task ID to update
     * @param request the task update request
     * @return the updated task
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@AuthenticationPrincipal String userId, 
                                       @PathVariable String id, 
                                       @RequestBody TaskRequest request) {
        Task task = Task.builder()
                .id(id)
                .description(request.description)
                .userId(userId)
                .goalId(request.getGoalId())
                .keyResultId(request.getKeyResultId())
                .build();
        return ResponseEntity.ok(taskService.updateTask(task));
    }

    /**
     * Deletes a task after verifying ownership.
     * Ownership validation is performed by the service layer.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param id the task ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal String userId, @PathVariable String id) {
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class TaskRequest {
        private String description;
        private String goalId;
        private String keyResultId;
    }
}
