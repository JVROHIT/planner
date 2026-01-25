# FocusFlow Redesign Plan

## Overview

This document provides a detailed, multi-agent parallel-phase plan to redesign the FocusFlow project based on:
- **code-principle.md** constraints (max 300 lines/class, 90 lines/method, strict layering)
- **Fixed timezone**: Asia/Kolkata (UTC+5:30) - NO system clock usage
- **Minimal code**: Remove all unused/placeholder classes
- **Comments everywhere**: Every class, method, and complex logic block must be documented
- **Robust exception handling**: Domain-specific exception classes with meaningful messages

---

## Architecture Constraints Summary

| Constraint | Rule |
|-----------|------|
| Max class size | 300 lines |
| Max method size | 90 lines |
| Layers allowed | Controller → Service → Repository (NO extra layers) |
| Timezone | Always `Asia/Kolkata` (ZoneId constant, never system default) |
| Lombok required | `@Getter @Setter @AllArgsConstructor @Builder` on all models |
| Strings | No magic strings - use `*Constants` classes |
| Logging | Guarded debug logs with `LogUtil.isDebugEnabled()` |
| Exceptions | Domain-specific, typed, never generic `Exception` |
| Tests | Required for all business services |
| Configuration | **100% environment-driven** - all settings from `.env` file |
| Secrets | Never hardcoded - must come from environment variables |
| Fail-fast | Application MUST refuse to start if required env vars missing |
| Health checks | `/actuator/health` must verify MongoDB and Redis connectivity |

---

## Files/Classes to DELETE (Unused/Placeholder)

Before starting any phase, these files must be removed:

| File | Reason |
|------|--------|
| `domain/nudge/IdleMorningRule.java` | Empty placeholder - not implemented |
| `domain/nudge/MissedDayRule.java` | Empty placeholder - not implemented |
| `domain/nudge/StreakAtRiskRule.java` | Empty placeholder - not implemented |
| `domain/nudge/NudgeRule.java` | Interface for deleted rules |
| `domain/nudge/NudgeService.java` | Placeholder implementation with empty context |
| `domain/nudge/Nudge.java` | Entity for deleted nudge system |
| `domain/nudge/NudgeRepository.java` | Repository for deleted nudge system |
| `infra/mongo/MongoNudgeRepository.java` | MongoDB impl for deleted nudge system |
| `infra/push/PushDispatcher.java` | Empty stub - not implemented |
| `infra/push/PushGateway.java` | Empty stub - not implemented |
| `domain/common/enums/` | Empty directory |
| `events/InProcessEventPublisher.java` | Redundant - SpringDomainEventPublisher is sufficient |

**Total: 12 files/directories to delete**

---

## Phase 1: Foundation (Parallel Agents)

All tasks in Phase 1 can run simultaneously. No dependencies between them.

### Agent 1.1: Exception Hierarchy Creation

**Location**: `src/main/java/com/personal/planner/domain/common/exception/`

**Task**: Create a comprehensive, domain-specific exception hierarchy.

**Files to create**:

```
domain/common/exception/
├── FocusFlowException.java          # Base exception (abstract)
├── EntityNotFoundException.java      # When entity not found by ID
├── TaskNotFoundException.java        # Extends EntityNotFoundException
├── GoalNotFoundException.java        # Extends EntityNotFoundException
├── KeyResultNotFoundException.java   # Extends EntityNotFoundException
├── DailyPlanNotFoundException.java   # Extends EntityNotFoundException
├── WeeklyPlanNotFoundException.java  # Extends EntityNotFoundException
├── UserNotFoundException.java        # Extends EntityNotFoundException
├── DomainViolationException.java     # Move existing, add proper comments
├── AuthenticationException.java      # Login/JWT failures
├── AuthorizationException.java       # Ownership/permission failures
├── InvalidRequestException.java      # Validation failures
└── EventProcessingException.java     # Event handler failures
```

**Implementation requirements**:
1. All exceptions extend `FocusFlowException` (which extends `RuntimeException`)
2. Each exception must have:
   - Javadoc explaining when to throw it
   - Constructor accepting `String message`
   - Constructor accepting `String message, Throwable cause`
3. `EntityNotFoundException` should accept `String entityType, String entityId`
4. Delete old `DomainViolationException.java` after moving to new package

**Example structure**:
```java
/**
 * Base exception for all FocusFlow domain errors.
 * All custom exceptions must extend this class.
 */
public abstract class FocusFlowException extends RuntimeException {
    protected FocusFlowException(String message) {
        super(message);
    }
    protected FocusFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

### Agent 1.2: Constants Classes Creation

**Location**: `src/main/java/com/personal/planner/domain/common/constants/`

**Task**: Create constants classes to eliminate all magic strings and numbers.

**Files to create**:

```
domain/common/constants/
├── TimeConstants.java      # Timezone, date formats
├── EventConstants.java     # Event consumer names
├── AnalyticsConstants.java # Trend thresholds, snapshot window
└── SecurityConstants.java  # JWT settings keys, auth paths
```

**TimeConstants.java**:
```java
/**
 * Time-related constants for FocusFlow.
 * CRITICAL: All time operations MUST use ZONE_ID, never system default.
 */
public final class TimeConstants {
    private TimeConstants() {} // Prevent instantiation

    /** The ONLY timezone used in FocusFlow - Asia/Kolkata (UTC+5:30) */
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Kolkata");

    /** Standard date format for API responses */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** Standard datetime format for API responses */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
}
```

**EventConstants.java**:
```java
/**
 * Event consumer identifiers for idempotency tracking.
 */
public final class EventConstants {
    private EventConstants() {}

    public static final String CONSUMER_GOAL = "GOAL";
    public static final String CONSUMER_STREAK = "STREAK";
    public static final String CONSUMER_SNAPSHOT = "SNAPSHOT";
    public static final String CONSUMER_AUDIT = "AUDIT";
}
```

**AnalyticsConstants.java**:
```java
/**
 * Constants for analytics calculations.
 */
public final class AnalyticsConstants {
    private AnalyticsConstants() {}

    /** Number of days to look back for trend calculation */
    public static final int TREND_WINDOW_DAYS = 7;

    /** Threshold for trend direction (percentage points) */
    public static final double TREND_THRESHOLD = 0.02;
}
```

---

### Agent 1.3: Clock Provider Refactoring

**Location**: `src/main/java/com/personal/planner/domain/common/`

**Task**: Refactor `ClockProvider` to ALWAYS use Asia/Kolkata timezone.

**Changes**:

1. **Modify `ClockProvider.java`**:
```java
/**
 * Provides current time operations for the application.
 * CRITICAL: All implementations MUST use Asia/Kolkata timezone.
 * Never use system default timezone.
 */
public interface ClockProvider {

    /**
     * Returns current instant in Asia/Kolkata timezone.
     * @return current LocalDateTime in Asia/Kolkata
     */
    LocalDateTime now();

    /**
     * Returns current date in Asia/Kolkata timezone.
     * @return current LocalDate in Asia/Kolkata
     */
    LocalDate today();

    /**
     * Returns the zone ID (always Asia/Kolkata).
     * @return ZoneId for Asia/Kolkata
     */
    default ZoneId getZoneId() {
        return TimeConstants.ZONE_ID;
    }
}
```

2. **Modify `SystemClockProvider.java`**:
```java
/**
 * Production implementation of ClockProvider.
 * Uses real system time but ALWAYS in Asia/Kolkata timezone.
 */
@Component
public class SystemClockProvider implements ClockProvider {

    @Override
    public LocalDateTime now() {
        // Always use Asia/Kolkata, never system default
        return LocalDateTime.now(TimeConstants.ZONE_ID);
    }

    @Override
    public LocalDate today() {
        // Always use Asia/Kolkata, never system default
        return LocalDate.now(TimeConstants.ZONE_ID);
    }
}
```

3. **Modify `FixedClockProvider.java`** (in test):
```java
/**
 * Test implementation of ClockProvider with fixed time.
 * Allows tests to control time for deterministic behavior.
 */
public class FixedClockProvider implements ClockProvider {
    private LocalDateTime fixedTime;

    public FixedClockProvider(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public void setFixedTime(LocalDateTime time) {
        this.fixedTime = time;
    }

    @Override
    public LocalDateTime now() {
        return this.fixedTime;
    }

    @Override
    public LocalDate today() {
        return this.fixedTime.toLocalDate();
    }
}
```

---

### Agent 1.4: Logging Utility Creation

**Location**: `src/main/java/com/personal/planner/domain/common/util/`

**Task**: Create `LogUtil` class for guarded debug logging.

**File**: `LogUtil.java`
```java
/**
 * Utility class for performance-optimized logging.
 * Debug logs are guarded to prevent expensive string operations when disabled.
 */
public final class LogUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LogUtil.class);

    private LogUtil() {} // Prevent instantiation

    /**
     * Check if debug logging is enabled before expensive operations.
     * @return true if debug level is enabled
     */
    public static boolean isDebugEnabled() {
        return LOG.isDebugEnabled();
    }

    /**
     * Log debug message with lazy evaluation.
     * @param logger the logger to use
     * @param messageSupplier supplier for the message (only evaluated if debug enabled)
     */
    public static void debug(Logger logger, Supplier<String> messageSupplier) {
        if (logger.isDebugEnabled()) {
            logger.debug(messageSupplier.get());
        }
    }
}
```

---

## Phase 2: Domain Layer Refactoring (Parallel Agents)

All tasks in Phase 2 can run simultaneously. Depends on Phase 1 completion.

### Agent 2.1: Entity Refactoring

**Task**: Add Javadoc comments to all entities, fix Lombok annotations, ensure immutability where required.

**Files to modify**:

1. **`domain/task/Task.java`**:
   - Add class-level Javadoc explaining Task is an "intent unit"
   - Add field-level comments
   - Ensure Lombok annotations match code-principle: `@Getter @Setter @AllArgsConstructor @Builder`
   - Add `@NoArgsConstructor(access = AccessLevel.PROTECTED)` for JPA/Mongo

2. **`domain/plan/DailyPlan.java`**:
   - Add class-level Javadoc explaining "execution truth" concept
   - Document the `closed` invariant (immutable after close)
   - Add comments to `ensureNotClosed()` method
   - Document `TaskExecution` inner class

3. **`domain/plan/WeeklyPlan.java`**:
   - Add class-level Javadoc explaining "editable intent grid"
   - Document the `taskGrid` structure (DayOfWeek → List<TaskId>)

4. **`domain/goal/Goal.java`**:
   - Add class-level Javadoc explaining "directional objective"
   - Document horizon types (MONTH, QUARTER, YEAR)

5. **`domain/goal/KeyResult.java`**:
   - Add class-level Javadoc explaining evaluation types
   - Document protected setters pattern and why it exists
   - Add comments to `applyProgress`, `updateProgress`, `markMilestoneCompleted`

6. **`domain/streak/StreakState.java`**:
   - Add class-level Javadoc explaining "derived interpretation"
   - Document that this is never edited via UI

7. **`domain/analytics/GoalSnapshot.java`**:
   - Add class-level Javadoc explaining "immutable historical fact"
   - Document append-only constraint

8. **`domain/user/User.java`**:
   - Add class-level Javadoc
   - Ensure password field has proper protection

9. **`domain/preference/UserPreference.java`**:
   - **CRITICAL FIX**: Add `@Document(collection = "userPreference")` annotation
   - Add class-level Javadoc
   - Document default values

10. **`domain/common/EventReceipt.java`**:
    - Add class-level Javadoc explaining idempotency guarantee
    - Document consumer tracking

11. **`domain/common/AuditEvent.java`**:
    - Add class-level Javadoc explaining immutable audit log
    - Document Type enum values

---

### Agent 2.2: Task Domain Services

**Task**: Refactor `TaskService.java` with comments, proper exception handling, and logging.

**Changes**:

1. Add class-level Javadoc explaining service responsibility
2. Add method-level Javadoc for all public methods
3. Replace generic exceptions with domain-specific ones:
   - Use `TaskNotFoundException` when task not found
   - Use `AuthorizationException` when user doesn't own task
4. Add guarded debug logging for important operations
5. Add ownership validation before update/delete operations

**Example**:
```java
/**
 * Service for managing Task entities (intent units).
 * Handles creation, update, deletion, and completion of tasks.
 * Does NOT store execution history - that belongs to DailyPlan.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;

    /**
     * Creates a new task for the specified user.
     * Publishes TaskCreated event for downstream listeners.
     *
     * @param userId the user creating the task
     * @param description task description
     * @param goalId optional goal this task contributes to
     * @param keyResultId optional key result this task contributes to
     * @param contribution numeric contribution for accumulative KRs
     * @return the created task
     */
    @Transactional
    public Task createTask(String userId, String description,
                          String goalId, String keyResultId, long contribution) {
        // ... implementation with debug logging
    }

    /**
     * Deletes a task after verifying ownership.
     *
     * @param taskId the task to delete
     * @param userId the user requesting deletion
     * @throws TaskNotFoundException if task does not exist
     * @throws AuthorizationException if user does not own the task
     */
    @Transactional
    public void deleteTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task", taskId));

        // Ownership check - SECURITY CRITICAL
        if (!task.getUserId().equals(userId)) {
            throw new AuthorizationException(
                "User " + userId + " does not own task " + taskId);
        }

        taskRepository.delete(task);

        if (LogUtil.isDebugEnabled()) {
            LOG.debug("[TaskService] Deleted task: {} for user: {}", taskId, userId);
        }
    }
}
```

---

### Agent 2.3: Plan Domain Services

**Task**: Refactor `PlanningService.java`, `DayCloseService.java`, `DailyPlanQueryService.java`.

**Changes for PlanningService**:

1. Add comprehensive Javadoc
2. **CRITICAL**: Replace all timezone logic with `TimeConstants.ZONE_ID`
3. Extract complex temporal logic to private helper methods
4. Add proper exception handling
5. Remove `while` loop for week start calculation - use `TemporalAdjusters`

**Extract temporal logic**:
```java
/**
 * Calculates the next week start date based on user preferences.
 * Uses Asia/Kolkata timezone.
 *
 * @param currentDate the reference date
 * @param startOfWeek the user's preferred start of week
 * @return the next week's start date
 */
private LocalDate calculateNextWeekStart(LocalDate currentDate, DayOfWeek startOfWeek) {
    // Use TemporalAdjusters instead of while loop
    return currentDate.with(TemporalAdjusters.next(startOfWeek));
}
```

**Changes for DayCloseService**:

1. Add class-level Javadoc explaining orchestration role
2. Add comprehensive comments
3. Handle case where DailyPlan doesn't exist (create it first)
4. Add proper exception handling

**Changes for DailyPlanQueryService**:

1. Add class-level and method-level Javadoc
2. Add proper exception handling for missing data

---

### Agent 2.4: Goal Domain Services

**Task**: Refactor `GoalService.java`, `KeyResultEvaluator.java`, and evaluator strategies.

**Changes for GoalService**:

1. Add comprehensive Javadoc
2. Add ownership validation for all operations
3. Replace magic strings with `EventConstants`
4. Add proper exception handling:
   - `GoalNotFoundException` when goal not found
   - `KeyResultNotFoundException` when KR not found
   - `AuthorizationException` when user doesn't own goal

**Changes for KeyResultEvaluator**:

1. Add class-level Javadoc explaining strategy dispatcher pattern
2. Document each evaluation type

**FIX AccumulativeKREvaluator**:
```java
// CRITICAL BUG FIX: Wrong import
// Change from:
import org.apache.el.stream.Optional;
// To:
import java.util.Optional;
```

**Changes for HabitKREvaluator**:

1. Add comprehensive comments explaining habit tracking logic
2. Document the DailyPlan inspection process

---

### Agent 2.5: Analytics Domain Services

**Task**: Refactor `SnapshotService.java`, `TrendCalculatorService.java`, `GoalQueryService.java`.

**CRITICAL FIX for TrendCalculatorService**:
```java
/**
 * Calculates trend direction from goal snapshots.
 * Compares current progress to progress N days ago.
 */
public Trend calculateTrend(List<GoalSnapshot> snapshots) {
    // CRITICAL FIX: Bounds checking before index access
    if (snapshots == null || snapshots.isEmpty()) {
        return Trend.FLAT;
    }

    GoalSnapshot latest = snapshots.get(0);

    // Safe index calculation - use minimum of window size or available snapshots
    int previousIndex = Math.min(
        AnalyticsConstants.TREND_WINDOW_DAYS - 1,
        snapshots.size() - 1
    );

    // Handle case where we only have one snapshot
    if (previousIndex == 0) {
        return Trend.FLAT;
    }

    GoalSnapshot previous = snapshots.get(previousIndex);

    double delta = latest.getActualProgress() - previous.getActualProgress();

    if (delta > AnalyticsConstants.TREND_THRESHOLD) {
        return Trend.UP;
    } else if (delta < -AnalyticsConstants.TREND_THRESHOLD) {
        return Trend.DOWN;
    }
    return Trend.FLAT;
}
```

**Changes for SnapshotService**:

1. Add comprehensive Javadoc
2. Replace magic strings with constants
3. Add idempotency check using `EventConstants.CONSUMER_SNAPSHOT`

---

### Agent 2.6: Streak Domain Services

**Task**: Refactor `StreakService.java`, `StreakQueryService.java`.

**Changes**:

1. Add comprehensive Javadoc explaining behavioral continuity concept
2. Replace magic strings with `EventConstants.CONSUMER_STREAK`
3. Add proper exception handling
4. Document streak calculation rules

---

### Agent 2.7: Audit Service Refactoring

**Task**: Refactor `AuditService.java` to handle all event types properly.

**Changes**:

1. Add idempotency check (currently missing)
2. Replace magic strings with `EventConstants.CONSUMER_AUDIT`
3. Add logging for unknown event types instead of silent drop
4. Add comprehensive comments
5. Consider using a map-based approach instead of if-else chain

```java
/**
 * Records domain events into immutable audit log.
 * CRITICAL: Must handle ALL DomainEvent types or log warning.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository auditRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;

    // Map event class to audit type for cleaner handling
    private static final Map<Class<? extends DomainEvent>, AuditEvent.Type> EVENT_TYPE_MAP = Map.of(
        TaskCreated.class, AuditEvent.Type.TASK_CREATED,
        TaskCompleted.class, AuditEvent.Type.TASK_COMPLETED,
        DayClosed.class, AuditEvent.Type.DAY_CLOSED,
        WeeklyPlanUpdated.class, AuditEvent.Type.WEEKLY_PLAN_UPDATED
    );

    @EventListener
    @Transactional
    public void record(DomainEvent event) {
        // Idempotency check - CRITICAL FIX
        if (eventReceiptRepository.findByEventIdAndConsumer(
                event.eventId(), EventConstants.CONSUMER_AUDIT).isPresent()) {
            return;
        }

        AuditEvent.Type type = EVENT_TYPE_MAP.get(event.getClass());

        if (type == null) {
            // Log warning instead of silent drop
            LOG.warn("[AuditService] Unknown event type: {}", event.getClass().getName());
            return;
        }

        // ... create and save audit event

        // Record receipt
        eventReceiptRepository.save(
            EventReceipt.of(event.eventId(), EventConstants.CONSUMER_AUDIT, clock.now()));
    }
}
```

---

## Phase 3: API Layer Refactoring (Parallel Agents)

All tasks in Phase 3 can run simultaneously. Depends on Phase 2 completion.

### Agent 3.1: Controller Security Fixes

**Task**: Add ownership validation to all controllers.

**Files to modify**:

1. **`TaskController.java`**:
   - Add `@AuthenticationPrincipal` to get userId
   - Pass userId to service for ownership validation
   - Wrap all operations in try-catch with proper HTTP responses

2. **`GoalController.java`**:
   - Add ownership validation before update/delete
   - Fix `updateGoal()` to verify existing goal ownership

3. **`StreakController.java`**:
   - **CRITICAL FIX**: Remove `userId` query parameter
   - Get userId from `@AuthenticationPrincipal`
   - Users must only see their own streak

4. **`HistoryController.java`**:
   - Ensure userId from authentication is used

**Example fix for StreakController**:
```java
/**
 * Controller for streak-related endpoints.
 * All endpoints are user-scoped via authentication.
 */
@RestController
@RequestMapping("/api/streak")
@RequiredArgsConstructor
public class StreakController {

    private final StreakQueryService streakQueryService;

    /**
     * Get current streak for authenticated user.
     * SECURITY: userId from authentication, never from request params.
     */
    @GetMapping
    public ResponseEntity<StreakResponse> getStreak(
            @AuthenticationPrincipal String userId) {
        try {
            StreakState state = streakQueryService.getCurrent(userId);
            return ResponseEntity.ok(new StreakResponse(state.getCurrentStreak()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StreakResponse(0));
        }
    }
}
```

---

### Agent 3.2: Dashboard Controller Split

**Task**: Split `DashboardController.java` (163 lines) into smaller controllers.

**Current structure** (problematic):
- `/api/dashboard/today` - today's tasks, streak, goals summary
- `/api/dashboard/week` - week view with day progress
- `/api/dashboard/goals` - detailed goal information

**New structure**:

1. **`DashboardTodayController.java`** (~50 lines):
   - Endpoint: `GET /api/dashboard/today`
   - Returns: today's tasks, completion ratio, current streak, active goals summary

2. **`DashboardWeekController.java`** (~50 lines):
   - Endpoint: `GET /api/dashboard/week`
   - Returns: 7-day view with per-day completion stats

3. **`DashboardGoalsController.java`** (~60 lines):
   - Endpoint: `GET /api/dashboard/goals`
   - Returns: detailed goal progress with snapshots and trends

**Each controller must have**:
- Class-level Javadoc
- Proper exception handling with HTTP status codes
- `@AuthenticationPrincipal` for user identification

---

### Agent 3.3: Auth Controller Refactoring

**Task**: Refactor `AuthController.java` with proper exception handling.

**Changes**:

1. Add class-level and method-level Javadoc
2. Use domain-specific exceptions:
   - `AuthenticationException` for login failures
   - `InvalidRequestException` for validation errors
3. Add proper HTTP status codes:
   - 400 for validation errors
   - 401 for authentication failures
   - 409 for duplicate email registration
4. Add input validation comments

---

### Agent 3.4: Plan Controller Refactoring

**Task**: Refactor `PlanController.java` with comments and exception handling.

**Changes**:

1. Add comprehensive Javadoc
2. Handle `WeeklyPlanNotFoundException` with 404
3. Handle `DailyPlanNotFoundException` with 404
4. Handle `DomainViolationException` (closed plan) with 409 Conflict
5. Add timezone comment: all dates are interpreted in Asia/Kolkata

---

### Agent 3.5: Global Exception Handler

**Task**: Create `GlobalExceptionHandler.java` for centralized exception handling.

**Location**: `src/main/java/com/personal/planner/api/`

```java
/**
 * Global exception handler for REST API.
 * Translates domain exceptions to HTTP responses.
 * Controllers should NOT catch exceptions that this handler manages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle entity not found errors.
     * Returns 404 NOT FOUND.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        LOG.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    /**
     * Handle authorization errors.
     * Returns 403 FORBIDDEN.
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AuthorizationException ex) {
        LOG.warn("Authorization failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("FORBIDDEN", ex.getMessage()));
    }

    /**
     * Handle domain violations (e.g., modifying closed plan).
     * Returns 409 CONFLICT.
     */
    @ExceptionHandler(DomainViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DomainViolationException ex) {
        LOG.warn("Domain violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    /**
     * Handle validation errors.
     * Returns 400 BAD REQUEST.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(InvalidRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    /**
     * Handle authentication errors.
     * Returns 401 UNAUTHORIZED.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    /**
     * Catch-all for unexpected errors.
     * Returns 500 INTERNAL SERVER ERROR.
     * Does NOT expose internal details to client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        LOG.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    /**
     * Standard error response DTO.
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private final String code;
        private final String message;
    }
}
```

---

## Phase 4: Infrastructure Layer Refactoring (Parallel Agents)

### Agent 4.1: Environment Configuration Setup

**Task**: Create `.env.example` and update `application.properties` to be fully environment-driven.

**CRITICAL**: All configuration MUST come from environment variables. No hardcoded values.

**File 1**: Create `.env.example` at project root:
```properties
# ===========================================
# FocusFlow Environment Configuration
# ===========================================
# Copy this file to .env and fill in values
# NEVER commit .env to version control
# ===========================================

# ===========================================
# APPLICATION SETTINGS
# ===========================================
APP_NAME=FocusFlow
APP_PORT=8080

# ===========================================
# MONGODB CONFIGURATION
# ===========================================
# MongoDB connection URI
# Format: mongodb://[username:password@]host:port/database
MONGODB_URI=mongodb://localhost:27017/focusflow
MONGODB_DATABASE=focusflow

# ===========================================
# REDIS CONFIGURATION
# ===========================================
# Redis connection settings
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
# Redis database index (0-15)
REDIS_DATABASE=0
# Connection timeout in milliseconds
REDIS_TIMEOUT=2000

# ===========================================
# JWT SECURITY CONFIGURATION
# ===========================================
# CRITICAL: Use a strong, unique secret in production (min 256 bits)
# Generate with: openssl rand -base64 32
JWT_SECRET=CHANGE_ME_IN_PRODUCTION_USE_STRONG_SECRET_MIN_32_CHARS
# Token expiration in milliseconds (default: 24 hours = 86400000)
JWT_EXPIRATION_MS=86400000

# ===========================================
# LOGGING CONFIGURATION
# ===========================================
# Log levels: TRACE, DEBUG, INFO, WARN, ERROR
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_MONGO=WARN
LOG_LEVEL_REDIS=WARN
```

**File 2**: Update `src/main/resources/application.properties`:
```properties
# ===========================================
# FocusFlow Application Configuration
# ===========================================
# All values are driven by environment variables
# See .env.example for required variables
# ===========================================

# Application
spring.application.name=${APP_NAME:FocusFlow}
server.port=${APP_PORT:8080}

# ===========================================
# MongoDB Configuration
# ===========================================
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/focusflow}
spring.data.mongodb.database=${MONGODB_DATABASE:focusflow}
# Auto-index creation (disable in production for performance)
spring.data.mongodb.auto-index-creation=true

# ===========================================
# Redis Configuration
# ===========================================
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.database=${REDIS_DATABASE:0}
spring.data.redis.timeout=${REDIS_TIMEOUT:2000}ms
# Enable keyspace notifications for expiration events
# CRITICAL: Redis must have 'notify-keyspace-events Ex' configured

# ===========================================
# JWT Security Configuration
# ===========================================
security.jwt.secret=${JWT_SECRET:CHANGE_ME_IN_PRODUCTION}
security.jwt.expiration=${JWT_EXPIRATION_MS:86400000}

# ===========================================
# Logging Configuration
# ===========================================
logging.level.root=${LOG_LEVEL_ROOT:INFO}
logging.level.com.personal.planner=${LOG_LEVEL_APP:DEBUG}
logging.level.org.springframework.data.mongodb=${LOG_LEVEL_MONGO:WARN}
logging.level.org.springframework.data.redis=${LOG_LEVEL_REDIS:WARN}

# ===========================================
# Actuator (Health Checks)
# ===========================================
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when_authorized
```

**File 3**: Create `src/main/resources/application-test.properties` for tests:
```properties
# Test environment configuration
# Uses embedded/mock databases where possible

spring.data.mongodb.uri=mongodb://localhost:27017/focusflow_test
spring.data.mongodb.database=focusflow_test
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=1
security.jwt.secret=test-secret-key-for-testing-only-min-32-characters
security.jwt.expiration=3600000
```

---

### Agent 4.2: MongoDB Configuration Class

**Task**: Create proper MongoDB configuration with environment-driven settings and fail-fast validation.

**Location**: `src/main/java/com/personal/planner/infra/mongo/MongoConfig.java`

**Implementation**:
```java
package com.personal.planner.infra.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB configuration for FocusFlow.
 *
 * All connection settings are driven by environment variables:
 * - MONGODB_URI: Full connection URI
 * - MONGODB_DATABASE: Database name
 *
 * This configuration validates required settings on startup
 * and fails fast if MongoDB is not properly configured.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.personal.planner.infra.mongo")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * Validates MongoDB configuration on startup.
     * Fails fast if required environment variables are missing.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    @PostConstruct
    public void validateConfiguration() {
        LOG.info("[MongoConfig] Validating MongoDB configuration...");

        // Fail fast if URI is missing or default
        if (!StringUtils.hasText(mongoUri)) {
            throw new IllegalStateException(
                "MONGODB_URI environment variable is required. " +
                "Set it in .env file or environment variables."
            );
        }

        // Fail fast if database name is missing
        if (!StringUtils.hasText(databaseName)) {
            throw new IllegalStateException(
                "MONGODB_DATABASE environment variable is required. " +
                "Set it in .env file or environment variables."
            );
        }

        LOG.info("[MongoConfig] MongoDB configured for database: {}", databaseName);
    }

    /**
     * Returns the database name for MongoDB operations.
     *
     * @return the configured database name
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * Creates and configures the MongoDB client.
     * Includes connection pooling and timeout settings.
     *
     * @return configured MongoClient instance
     */
    @Override
    public MongoClient mongoClient() {
        LOG.info("[MongoConfig] Creating MongoDB client...");

        ConnectionString connectionString = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            // Connection pool settings
            .applyToConnectionPoolSettings(builder -> builder
                .maxSize(50)                    // Max connections in pool
                .minSize(5)                     // Min connections to maintain
                .maxWaitTime(30, TimeUnit.SECONDS)
            )
            // Socket settings
            .applyToSocketSettings(builder -> builder
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
            )
            // Server selection timeout
            .applyToClusterSettings(builder -> builder
                .serverSelectionTimeout(30, TimeUnit.SECONDS)
            )
            .build();

        return MongoClients.create(settings);
    }

    /**
     * Configures custom type conversions for MongoDB.
     * Add converters here for custom types (e.g., ZonedDateTime).
     *
     * @return custom conversions configuration
     */
    @Bean
    public MongoCustomConversions customConversions() {
        // Add custom converters if needed (e.g., for LocalDateTime with timezone)
        return new MongoCustomConversions(java.util.Collections.emptyList());
    }
}
```

---

### Agent 4.3: Redis Configuration Class

**Task**: Refactor `RedisConfig.java` with environment-driven settings, fail-fast validation, and proper connection configuration.

**Location**: `src/main/java/com/personal/planner/infra/redis/RedisConfig.java`

**Implementation**:
```java
package com.personal.planner.infra.redis;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis configuration for FocusFlow.
 *
 * All connection settings are driven by environment variables:
 * - REDIS_HOST: Redis server hostname
 * - REDIS_PORT: Redis server port
 * - REDIS_PASSWORD: Redis password (optional)
 * - REDIS_DATABASE: Redis database index (0-15)
 * - REDIS_TIMEOUT: Connection timeout in milliseconds
 *
 * This configuration validates required settings on startup
 * and fails fast if Redis is not properly configured.
 *
 * IMPORTANT: Redis must have keyspace notifications enabled for
 * expiration events. Configure Redis with: notify-keyspace-events Ex
 */
@Configuration
public class RedisConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.timeout:2000}")
    private long redisTimeout;

    /**
     * Validates Redis configuration on startup.
     * Fails fast if required environment variables are missing.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    @PostConstruct
    public void validateConfiguration() {
        LOG.info("[RedisConfig] Validating Redis configuration...");

        // Fail fast if host is missing
        if (!StringUtils.hasText(redisHost)) {
            throw new IllegalStateException(
                "REDIS_HOST environment variable is required. " +
                "Set it in .env file or environment variables."
            );
        }

        // Validate port range
        if (redisPort < 1 || redisPort > 65535) {
            throw new IllegalStateException(
                "REDIS_PORT must be between 1 and 65535. Current value: " + redisPort
            );
        }

        // Validate database index
        if (redisDatabase < 0 || redisDatabase > 15) {
            throw new IllegalStateException(
                "REDIS_DATABASE must be between 0 and 15. Current value: " + redisDatabase
            );
        }

        LOG.info("[RedisConfig] Redis configured: {}:{} database={}",
            redisHost, redisPort, redisDatabase);
    }

    /**
     * Creates the Redis connection factory with configured settings.
     * Uses Lettuce client for non-blocking I/O.
     *
     * @return configured LettuceConnectionFactory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LOG.info("[RedisConfig] Creating Redis connection factory...");

        // Configure Redis server connection
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisHost);
        serverConfig.setPort(redisPort);
        serverConfig.setDatabase(redisDatabase);

        // Set password if provided
        if (StringUtils.hasText(redisPassword)) {
            serverConfig.setPassword(redisPassword);
        }

        // Configure Lettuce client options
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisTimeout))
            .shutdownTimeout(Duration.ZERO)
            .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    /**
     * Creates RedisTemplate for generic object serialization.
     * Uses JSON serialization for values, String for keys.
     *
     * @param connectionFactory the Redis connection factory
     * @return configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates StringRedisTemplate for simple string operations.
     *
     * @param connectionFactory the Redis connection factory
     * @return configured StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Creates Redis message listener container for pub/sub and keyspace events.
     * Used for handling key expiration events in scheduling.
     *
     * @param connectionFactory the Redis connection factory
     * @return configured RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
```

---

### Agent 4.4: Redis Scheduling Service Refactoring

**Task**: Refactor `RedisSchedulingService.java` to use constants and simplify.

**Changes**:

1. **CRITICAL**: Replace all timezone calculations with `TimeConstants.ZONE_ID`
2. Add comprehensive Javadoc
3. Extract complex scheduling logic to private methods
4. Add proper exception handling
5. Keep under 90 lines (currently 83, but needs comments)

---

### Agent 4.5: Security Infrastructure Refactoring

**Task**: Refactor security classes to use environment variables and add Javadoc.

**Files**:

1. **`JwtService.java`**:
   - Use `@Value("${security.jwt.secret}")` (already from env)
   - Use `@Value("${security.jwt.expiration}")` (already from env)
   - Add `@PostConstruct` validation to fail fast if secret is weak
   - Add comprehensive Javadoc

   **Add validation**:
   ```java
   @PostConstruct
   public void validateConfiguration() {
       // Fail fast if JWT secret is too short or default
       if (jwtSecret == null || jwtSecret.length() < 32) {
           throw new IllegalStateException(
               "JWT_SECRET must be at least 32 characters. " +
               "Set a strong secret in .env file."
           );
       }

       if (jwtSecret.contains("CHANGE_ME")) {
           throw new IllegalStateException(
               "JWT_SECRET contains default value. " +
               "Set a unique secret in .env file for security."
           );
       }
   }
   ```

2. **`JwtAuthenticationFilter.java`**: Document filter chain behavior
3. **`SecurityConfig.java`**: Document security rules and endpoints

---

### Agent 4.6: MongoDB Repository Documentation

**Task**: Add Javadoc to all MongoDB repository implementations.

**Files** (all in `infra/mongo/`):
- Add class-level Javadoc explaining repository purpose
- Document any custom query methods
- Ensure all repositories use proper Spring Data conventions

---

### Agent 4.7: Infrastructure Health Check

**Task**: Create health indicator for infrastructure validation.

**Location**: `src/main/java/com/personal/planner/infra/health/InfrastructureHealthIndicator.java`

**Implementation**:
```java
package com.personal.planner.infra.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Health indicator for FocusFlow infrastructure components.
 *
 * Checks connectivity to:
 * - MongoDB: Executes ping command
 * - Redis: Sets and gets a test key
 *
 * Exposed via /actuator/health endpoint.
 */
@Component
public class InfrastructureHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(InfrastructureHealthIndicator.class);
    private static final String REDIS_HEALTH_KEY = "health:check";

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;

    public InfrastructureHealthIndicator(
            MongoTemplate mongoTemplate,
            StringRedisTemplate redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Performs health check on MongoDB and Redis.
     *
     * @return Health status with details for each component
     */
    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        // Check MongoDB
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            builder.withDetail("mongodb", "Connected");
        } catch (Exception e) {
            LOG.error("[HealthCheck] MongoDB check failed", e);
            builder.down().withDetail("mongodb", "Disconnected: " + e.getMessage());
        }

        // Check Redis
        try {
            redisTemplate.opsForValue().set(REDIS_HEALTH_KEY, "ok");
            String value = redisTemplate.opsForValue().get(REDIS_HEALTH_KEY);
            if ("ok".equals(value)) {
                builder.withDetail("redis", "Connected");
            } else {
                builder.down().withDetail("redis", "Read/Write failed");
            }
        } catch (Exception e) {
            LOG.error("[HealthCheck] Redis check failed", e);
            builder.down().withDetail("redis", "Disconnected: " + e.getMessage());
        }

        return builder.build();
    }
}
```

---

## Phase 5: Event System Refactoring (Single Agent)

### Agent 5.1: Event Classes Documentation

**Task**: Add comprehensive Javadoc to all event classes.

**Files**:
- `DomainEvent.java`: Document interface contract
- `TaskCreated.java`: Document when this event is emitted
- `TaskCompleted.java`: Document payload fields and purpose
- `DayClosed.java`: Document immutability implications
- `WeeklyPlanUpdated.java`: Document when emitted
- `UserCreated.java`: Document when emitted
- `SpringDomainEventPublisher.java`: Document Spring integration

**Delete**:
- `InProcessEventPublisher.java` (redundant)

---

## Phase 6: Test Updates (Parallel Agents)

### Agent 6.1: Update Existing Tests

**Task**: Update tests to use new exception classes and constants.

**Files**:
- `ArchitecturalInvariantsTest.java`
- `IdempotencyProofTest.java`
- `GoalEventReactionTest.java`
- `DayCloseImmutabilityTest.java`
- `PlanningTemporalTest.java`
- `StreakDerivationTest.java`
- `TaskExecutionFlowTest.java`
- `SnapshotAppendOnlyTest.java`

**Changes**:
- Update imports for moved exception classes
- Use constants instead of magic strings
- Add test for TrendCalculatorService bounds checking

---

### Agent 6.2: Add Missing Tests

**Task**: Add unit tests for new components.

**Tests to create**:
1. `ExceptionHierarchyTest.java`: Test exception messages and inheritance
2. `GlobalExceptionHandlerTest.java`: Test HTTP status code mapping
3. `ClockProviderTest.java`: Verify Asia/Kolkata timezone is always used
4. `TrendCalculatorBoundsTest.java`: Test edge cases (empty list, single snapshot)

---

### Agent 6.3: Infrastructure Configuration Tests

**Task**: Add tests for environment-driven configuration and fail-fast behavior.

**Tests to create**:

1. **`MongoConfigTest.java`**:
```java
/**
 * Tests for MongoDB configuration validation.
 */
@SpringBootTest
class MongoConfigTest {

    @Test
    @DisplayName("Should fail fast when MONGODB_URI is missing")
    void shouldFailWhenMongoUriMissing() {
        // Test that application fails to start without MONGODB_URI
    }

    @Test
    @DisplayName("Should connect to MongoDB with valid configuration")
    void shouldConnectWithValidConfig() {
        // Test successful MongoDB connection
    }
}
```

2. **`RedisConfigTest.java`**:
```java
/**
 * Tests for Redis configuration validation.
 */
@SpringBootTest
class RedisConfigTest {

    @Test
    @DisplayName("Should fail fast when REDIS_HOST is missing")
    void shouldFailWhenRedisHostMissing() {
        // Test that application fails to start without REDIS_HOST
    }

    @Test
    @DisplayName("Should validate REDIS_DATABASE is between 0-15")
    void shouldValidateRedisDatabaseRange() {
        // Test database index validation
    }

    @Test
    @DisplayName("Should connect to Redis with valid configuration")
    void shouldConnectWithValidConfig() {
        // Test successful Redis connection
    }
}
```

3. **`JwtConfigTest.java`**:
```java
/**
 * Tests for JWT configuration validation.
 */
@SpringBootTest
class JwtConfigTest {

    @Test
    @DisplayName("Should fail fast when JWT_SECRET is too short")
    void shouldFailWhenJwtSecretTooShort() {
        // Test that secret < 32 chars fails
    }

    @Test
    @DisplayName("Should fail fast when JWT_SECRET contains default value")
    void shouldFailWhenJwtSecretIsDefault() {
        // Test that CHANGE_ME in secret fails
    }
}
```

4. **`InfrastructureHealthIndicatorTest.java`**:
```java
/**
 * Tests for infrastructure health check.
 */
@SpringBootTest
class InfrastructureHealthIndicatorTest {

    @Autowired
    private InfrastructureHealthIndicator healthIndicator;

    @Test
    @DisplayName("Should report healthy when MongoDB and Redis are connected")
    void shouldReportHealthyWhenConnected() {
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("mongodb");
        assertThat(health.getDetails()).containsKey("redis");
    }
}
```

---

## Phase 7: Final Cleanup (Single Agent)

### Agent 7.1: Delete Unused Files

**Task**: Remove all files marked for deletion at the start of this plan.

**Execute deletions**:
```bash
# Nudge system (completely unused)
rm -rf src/main/java/com/personal/planner/domain/nudge/

# Push system (stubs only)
rm -rf src/main/java/com/personal/planner/infra/push/

# Redundant event publisher
rm src/main/java/com/personal/planner/events/InProcessEventPublisher.java

# Empty directory
rm -rf src/main/java/com/personal/planner/domain/common/enums/

# MongoDB repository for deleted nudge
rm src/main/java/com/personal/planner/infra/mongo/MongoNudgeRepository.java
```

---

### Agent 7.2: Code Quality Verification

**Task**: Verify all code follows principles.

**Checklist**:
- [ ] No class exceeds 300 lines
- [ ] No method exceeds 90 lines
- [ ] All classes have Javadoc
- [ ] All public methods have Javadoc
- [ ] No magic strings (all in Constants classes)
- [ ] All exceptions are domain-specific
- [ ] All timezone operations use `TimeConstants.ZONE_ID`
- [ ] All models have required Lombok annotations
- [ ] Debug logging is guarded
- [ ] No unused imports
- [ ] All tests pass

---

## Dependency Graph

```
Phase 1 (Foundation) - All parallel
    ├── Agent 1.1: Exceptions
    ├── Agent 1.2: Constants
    ├── Agent 1.3: ClockProvider
    └── Agent 1.4: LogUtil
           │
           ▼
Phase 2 (Domain) - All parallel, depends on Phase 1
    ├── Agent 2.1: Entities
    ├── Agent 2.2: Task Services
    ├── Agent 2.3: Plan Services
    ├── Agent 2.4: Goal Services
    ├── Agent 2.5: Analytics Services
    ├── Agent 2.6: Streak Services
    └── Agent 2.7: Audit Service
           │
           ▼
Phase 3 (API) - All parallel, depends on Phase 2
    ├── Agent 3.1: Security Fixes
    ├── Agent 3.2: Dashboard Split
    ├── Agent 3.3: Auth Controller
    ├── Agent 3.4: Plan Controller
    └── Agent 3.5: Exception Handler
           │
           ▼
Phase 4 (Infrastructure) - All parallel, depends on Phase 1
    ├── Agent 4.1: Environment Config (.env.example, application.properties)
    ├── Agent 4.2: MongoDB Config (MongoConfig.java with fail-fast)
    ├── Agent 4.3: Redis Config (RedisConfig.java with fail-fast)
    ├── Agent 4.4: Redis Scheduling Service
    ├── Agent 4.5: Security Infrastructure (JWT validation)
    ├── Agent 4.6: MongoDB Repository Docs
    └── Agent 4.7: Health Check Indicator
           │
           ▼
Phase 5 (Events) - Single agent, depends on Phase 1
    └── Agent 5.1: Event Docs
           │
           ▼
Phase 6 (Tests) - Parallel, depends on Phases 2-5
    ├── Agent 6.1: Update Tests
    ├── Agent 6.2: Add Tests
    └── Agent 6.3: Infrastructure Config Tests
           │
           ▼
Phase 7 (Cleanup) - Sequential, depends on all
    ├── Agent 7.1: Delete Files
    └── Agent 7.2: Verification
```

---

## Success Criteria

1. **All tests pass** after refactoring
2. **No security vulnerabilities** (ownership checks in place)
3. **Zero magic strings** (all in Constants)
4. **Consistent timezone** (Asia/Kolkata everywhere)
5. **Comprehensive comments** (every class and public method)
6. **Typed exceptions** (no generic Exception usage)
7. **Size limits respected** (300 lines/class, 90 lines/method)
8. **Clean package structure** (no bloated modules)
9. **Unused code removed** (nudge system, push stubs)
10. **Build succeeds** with `./gradlew build`
11. **Environment-driven**: All config from .env, fail-fast on missing vars
12. **Health checks pass**: `/actuator/health` shows MongoDB and Redis connected

---

## Estimated Agent Count

| Phase | Parallel Agents | Sequential |
|-------|----------------|------------|
| Phase 1 | 4 | - |
| Phase 2 | 7 | - |
| Phase 3 | 5 | - |
| Phase 4 | 7 | - |
| Phase 5 | - | 1 |
| Phase 6 | 3 | - |
| Phase 7 | - | 2 |
| **Total** | **26** | **3** |

---

## Notes for Orchestrator

1. **Phase gating**: Do NOT start Phase N+1 until Phase N completes successfully
2. **Parallel execution**: Within each phase, agents can run simultaneously
3. **Dependency injection**: Services in Phase 2 depend on exceptions from Phase 1
4. **Test validation**: Run `./gradlew test` after each phase
5. **Build validation**: Run `./gradlew build` after Phase 7
6. **Git commits**: Create one commit per phase with descriptive message
7. **Environment setup**: Before running application, copy `.env.example` to `.env` and configure values
8. **Infrastructure validation**: After Phase 4, verify `/actuator/health` returns healthy status
9. **Fail-fast behavior**: Application MUST refuse to start if required env vars are missing