package com.personal.planner.api;

import com.personal.planner.domain.common.AuditQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controller for accessing the neutral timeline of what happened.
 * <p>
 * "Controllers do not compute history. They expose facts."
 * </p>
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final AuditQueryService auditQueryService;

    public HistoryController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecent(@RequestParam String userId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditQueryService.getRecent(userId, limit));
    }

    @GetMapping("/{date}")
    public ResponseEntity<?> getForDate(@RequestParam String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(auditQueryService.getForDate(userId, date));
    }
}
