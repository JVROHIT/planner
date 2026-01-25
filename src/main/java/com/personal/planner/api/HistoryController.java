package com.personal.planner.api;

import com.personal.planner.domain.common.AuditEvent;
import com.personal.planner.domain.common.AuditQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for accessing the neutral timeline of what happened.
 * 
 * <p>Controllers do not compute history. They expose facts.
 * All responses are wrapped in {@link ApiResponse} for consistent error handling.</p>
 * 
 * <p>All endpoints are user-scoped via authentication.
 * SECURITY: userId comes from authentication, never from request parameters.</p>
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final AuditQueryService auditQueryService;

    public HistoryController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    /**
     * Retrieves recent audit events for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param limit maximum number of events to return (default: 50)
     * @return list of recent audit events wrapped in ApiResponse
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getRecent(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(auditQueryService.getRecent(userId, limit)));
    }

    /**
     * Retrieves audit events for a specific date for the authenticated user.
     * 
     * @param userId the authenticated user ID (from JWT)
     * @param date the date to retrieve events for
     * @return list of audit events for the specified date wrapped in ApiResponse
     */
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getForDate(
            @AuthenticationPrincipal String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(auditQueryService.getForDate(userId, date)));
    }
}
