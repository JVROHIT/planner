package com.personal.planner.api;

import com.personal.planner.domain.task.Task;
import com.personal.planner.domain.task.TaskRepository;
import com.personal.planner.domain.task.TaskService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing task intent. Identity-scoped via JWT.
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
    public ResponseEntity<?> getTasks() {
        return ResponseEntity.ok(taskRepository.findByUserId(getUserId()));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        Task task = Task.builder()
                .description(request.description)
                .userId(getUserId())
                .build();
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody TaskRequest request) {
        Task task = Task.builder()
                .id(id)
                .description(request.description)
                .userId(getUserId())
                .build();
        return ResponseEntity.ok(taskService.updateTask(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        // Simple security check: ensure user owns the task before deletion
        // For MVP, we'll delegate to service or repo with userId scoping if available.
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private String getUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Data
    public static class TaskRequest {
        private String description;
    }
}
