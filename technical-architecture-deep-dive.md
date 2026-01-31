# FocusFlow Technical Architecture Deep Dive

## Table of Contents
1. [System Overview](#1-system-overview)
2. [Domain Model Deep Dive](#2-domain-model-deep-dive)
3. [Event-Driven Architecture](#3-event-driven-architecture)
4. [Service Layer Analysis](#4-service-layer-analysis)
5. [Data Persistence Strategy](#5-data-persistence-strategy)
6. [Security & Authentication](#6-security--authentication)
7. [API Design Patterns](#7-api-design-patterns)
8. [Testing Strategy](#8-testing-strategy)
9. [Performance Considerations](#9-performance-considerations)
10. [Deployment & Operations](#10-deployment--operations)

---

## 1. System Overview

### 1.1 Core Architecture Principles

FocusFlow implements a **CQRS with Event Sourcing** pattern with the following key characteristics:

- **Command Query Responsibility Segregation**: Separate write models (intent) from read models (analytics)
- **Event-Driven Communication**: All cross-domain communication via immutable domain events
- **Temporal Immutability**: Historical facts are never mutated, only appended
- **Layered Domain Boundaries**: Strict separation between intent, execution, and interpretation

### 1.4 Time Zone Policy (User-Scoped)

FocusFlow is time-layered, so **date boundaries must be user-scoped**:

- **Default timezone (system-wide):** Asia/Kolkata (UTC+5:30) for all users unless explicitly overridden at registration.
- **User override (registration only):** If a timezone is provided at registration, all day/week boundaries for that user use that timezone.
- **Storage:** Timezone is stored as an IANA zone ID (e.g., `Asia/Kolkata`, `America/Los_Angeles`).
- **Usage:** DailyPlan dates, DayClosed boundaries, streaks, and goal snapshots use the user's timezone.
- **No inference / no per-request override:** Timezone is not inferred from locale/device and is not overridden by request headers.
- **Non-goal:** Changing timezone after registration is out of scope for this release.

### 1.2 Technology Stack

```yaml
Backend Framework: Spring Boot 4.0.1
Language: Java 17
Database: MongoDB (primary)
Cache: Redis
Security: Spring Security with JWT
Build Tool: Gradle
Annotations: Lombok 1.18.30
Testing: JUnit 5, Spring Boot Test
```

### 1.3 Module Structure

```
com.personal.planner/
├── api/                    # REST Controllers (Layer 1)
├── domain/                 # Business Logic (Layer 2)
│   ├── task/              # Intent Units
│   ├── plan/              # Structure & Execution Truth
│   ├── goal/              # Directional Evaluation
│   ├── streak/            # Behavioral Continuity
│   ├── analytics/         # Historical Interpretation
│   ├── preference/        # User Configuration
│   ├── nudge/             # Behavioral Triggers
│   └── common/            # Shared Domain Infrastructure
├── events/                # Domain Events (Immutable Facts)
└── infrastructure/         # External Integrations
```

---

## 2. Domain Model Deep Dive

### 2.1 Task Domain (Intent Units)

#### 2.1.1 Core Entity Structure

```java
@Document(collection = "task")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {
    @Id private String id;
    private String userId;
    private String description;
    private boolean completed;
    private String goalId;        // Optional: Links to directional evaluation
    private String keyResultId;   // Optional: Specific KR this contributes to
    private long contribution;    // Quantitative impact for ACCUMULATIVE KRs
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 2.1.2 Business Rules

- **Immutability of Past**: Tasks represent current intent, not historical execution
- **Goal Linking**: Tasks can optionally contribute to specific KeyResults
- **Contribution Semantics**: `contribution` field quantifies impact for accumulative progress
- **State Management**: `completed` flag represents intent fulfillment, not daily execution

#### 2.1.3 Service Implementation

```java
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final DomainEventPublisher eventPublisher;
    private final ClockProvider clock;
    
    @Transactional
    public Task createTask(CreateTaskCommand command) {
        Task task = Task.builder()
            .id(UUID.randomUUID().toString())
            .userId(command.getUserId())
            .description(command.getDescription())
            .goalId(command.getGoalId())
            .keyResultId(command.getKeyResultId())
            .contribution(command.getContribution())
            .createdAt(clock.now())
            .build();
            
        Task saved = taskRepository.save(task);
        
        // Publish domain event for cross-domain effects
        eventPublisher.publish(TaskCreated.builder()
            .taskId(saved.getId())
            .userId(saved.getUserId())
            .occurredAt(clock.now())
            .eventId(UUID.randomUUID().toString())
            .build());
            
        return saved;
    }
    
    @Transactional
    public void completeTask(String taskId, String userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
            
        task.setCompleted(true);
        task.setUpdatedAt(clock.now());
        taskRepository.save(task);
        
        // Event-driven progress evaluation
        eventPublisher.publish(TaskCompleted.builder()
            .taskId(taskId)
            .userId(userId)
            .goalId(task.getGoalId())
            .keyResultId(task.getKeyResultId())
            .contribution(task.getContribution())
            .completedAt(clock.now())
            .eventId(UUID.randomUUID().toString())
            .build());
    }
}
```

### 2.2 Plan Domain (Structure & Execution Truth)

#### 2.2.1 WeeklyPlan Architecture

```java
@Document(collection = "weeklyPlan")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyPlan {
    @Id private String id;
    private String userId;
    private int weekNumber;           // ISO week number
    private int year;                  // ISO week-based year
    
    @Builder.Default
    private Map<DayOfWeek, List<String>> taskGrid = new HashMap<>();
    
    // Weekly plan is always mutable - represents future intent
    private LocalDateTime lastModified;
    
    public List<String> getTasksFor(DayOfWeek dayOfWeek) {
        return taskGrid.getOrDefault(dayOfWeek, Collections.emptyList());
    }
    
    public void addTask(DayOfWeek dayOfWeek, String taskId) {
        taskGrid.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(taskId);
        this.lastModified = LocalDateTime.now();
    }
    
    public void removeTask(DayOfWeek dayOfWeek, String taskId) {
        List<String> tasks = taskGrid.get(dayOfWeek);
        if (tasks != null) {
            tasks.remove(taskId);
            this.lastModified = LocalDateTime.now();
        }
    }
}
```

#### 2.2.2 DailyPlan Execution Truth

```java
@Document(collection = "dailyPlan")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DailyPlan {
    @Id private String id;
    private String userId;
    private LocalDate day;
    private boolean closed;
    
    @Builder.Default
    private List<TaskExecution> tasks = new ArrayList<>();
    
    // Immutable execution records
    @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class TaskExecution {
        private String taskId;
        private String title;           // Denormalized for historical accuracy
        private ExecutionStatus status;
        private LocalDateTime completedAt; // Null if not completed
        
        protected void setCompleted(boolean completed) {
            this.status = completed ? ExecutionStatus.COMPLETED : ExecutionStatus.MISSED;
            this.completedAt = completed ? LocalDateTime.now() : null;
        }
    }
    
    public enum ExecutionStatus {
        PENDING, COMPLETED, MISSED
    }
    
    // Immutable access pattern
    public List<TaskExecution> getTasks() {
        return Collections.unmodifiableList(tasks);
    }
    
    // Domain enforcement
    public void close() {
        this.closed = true;
    }
    
    public void markCompleted(String taskId) {
        ensureNotClosed();
        tasks.stream()
            .filter(t -> t.getTaskId().equals(taskId))
            .findFirst()
            .ifPresent(t -> t.setCompleted(true));
    }
    
    private void ensureNotClosed() {
        if (closed) {
            throw new DomainViolationException(
                "Historical truth cannot be rewritten. Plan is closed for date: " + day);
        }
    }
}
```

#### 2.2.3 Planning Service Implementation

```java
@Service
@RequiredArgsConstructor
public class PlanningService {
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final TaskRepository taskRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final ClockProvider clock;
    private final DomainEventPublisher eventPublisher;
    
    /**
     * Materializes DailyPlan from WeeklyPlan intent
     * Idempotent operation - safe to call multiple times
     */
    @Transactional
    public void materializeDay(LocalDate date, String userId) {
        // Skip if already exists (idempotency)
        if (dailyPlanRepository.findByUserIdAndDay(userId, date).isPresent()) {
            return;
        }
        
        // Get weekly plan for this date
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        
        WeeklyPlan weeklyPlan = weeklyPlanRepository
            .findByUserAndWeek(userId, week, year)
            .orElseThrow(() -> new WeeklyPlanNotFoundException(userId, week, year));
        
        // Materialize task executions
        List<String> taskIds = weeklyPlan.getTasksFor(date.getDayOfWeek());
        List<Task> tasks = taskRepository.findAllById(taskIds);
        
        List<DailyPlan.TaskExecution> executions = tasks.stream()
            .map(task -> DailyPlan.TaskExecution.builder()
                .taskId(task.getId())
                .title(task.getDescription())
                .status(DailyPlan.ExecutionStatus.PENDING)
                .build())
            .collect(Collectors.toList());
        
        DailyPlan dailyPlan = DailyPlan.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .day(date)
            .closed(false)
            .tasks(executions)
            .build();
        
        dailyPlanRepository.save(dailyPlan);
    }
    
    /**
     * Closes a daily plan and publishes immutable fact
     */
    @Transactional
    public void closeDay(LocalDate date, String userId) {
        DailyPlan plan = dailyPlanRepository.findByUserIdAndDay(userId, date)
            .orElseThrow(() -> new DailyPlanNotFoundException(userId, date));
            
        plan.close();
        dailyPlanRepository.save(plan);
        
        // Publish immutable historical fact
        eventPublisher.publish(DayClosed.builder()
            .day(date)
            .userId(userId)
            .closedAt(clock.now())
            .eventId(UUID.randomUUID().toString())
            .build());
    }
}
```

### 2.3 Goal Domain (Directional Evaluation)

#### 2.3.1 KeyResult Evaluation Strategies

```java
@Document(collection = "keyResult")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KeyResult {
    @Id @Setter(AccessLevel.PROTECTED) private String id;
    @Setter(AccessLevel.PROTECTED) private String goalId;
    @Setter(AccessLevel.PROTECTED) private String title;
    @Setter(AccessLevel.PROTECTED) private Type type;
    @Setter(AccessLevel.PROTECTED) private double currentValue;
    @Setter(AccessLevel.PROTECTED) private double targetValue;
    @Setter(AccessLevel.PROTECTED) private double progress;
    
    public enum Type {
        ACCUMULATIVE, HABIT, MILESTONE
    }
    
    // Protected mutation methods for evaluators only
    protected void applyProgress(double delta) {
        this.currentValue += delta;
        this.progress = Math.min(1.0, this.currentValue / this.targetValue);
    }
    
    protected void updateProgress(double absoluteValue) {
        this.currentValue = absoluteValue;
        this.progress = Math.min(1.0, this.currentValue / this.targetValue);
    }
    
    protected void markMilestoneCompleted() {
        this.currentValue = this.targetValue;
        this.progress = 1.0;
    }
}
```

#### 2.3.2 Strategy Pattern for Evaluation

```java
public interface KeyResultEvaluator {
    void evaluate(KeyResult keyResult, DomainEvent event);
}

@Component
public class AccumulativeKREvaluator implements KeyResultEvaluator {
    
    @Override
    public void evaluate(KeyResult keyResult, DomainEvent event) {
        if (event instanceof TaskCompleted taskCompleted) {
            if (keyResult.getId().equals(taskCompleted.getKeyResultId())) {
                keyResult.applyProgress(taskCompleted.getContribution());
            }
        }
    }
}

@Component
public class HabitKREvaluator implements KeyResultEvaluator {
    
    @Override
    public void evaluate(KeyResult keyResult, DomainEvent event) {
        if (event instanceof DayClosed dayClosed) {
            // Check if any relevant tasks were completed on this day
            // This would require access to DailyPlan for the day
            // Implementation depends on habit tracking strategy
        }
    }
}

@Component
public class MilestoneKREvaluator implements KeyResultEvaluator {
    
    @Override
    public void evaluate(KeyResult keyResult, DomainEvent event) {
        if (event instanceof TaskCompleted taskCompleted) {
            if (keyResult.getId().equals(taskCompleted.getKeyResultId())) {
                keyResult.markMilestoneCompleted();
            }
        }
    }
}
```

#### 2.3.3 Goal Service Event Processing

```java
@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final Map<KeyResult.Type, KeyResultEvaluator> evaluators;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;
    
    private static final String CONSUMER_NAME = "GOAL";
    
    @EventListener
    @Transactional
    public void on(DomainEvent event) {
        // Idempotency check
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }
        
        // Process all user's goals and key results
        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            keyResultRepository.findByGoalId(goal.getId()).forEach(keyResult -> {
                KeyResultEvaluator evaluator = evaluators.get(keyResult.getType());
                if (evaluator != null) {
                    evaluator.evaluate(keyResult, event);
                    keyResultRepository.save(keyResult);
                }
            });
        });
        
        // Record event processing
        eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
    }
}
```

### 2.4 User & Preferences (Time Zone)

User identity includes an optional timezone used to compute all time boundaries.

```java
@Document(collection = "user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id private String id;
    private String email;
    private String passwordHash;
    private String timeZone;      // IANA zone id, defaults to Asia/Kolkata
    private LocalDateTime createdAt;
}
```

**Rules:**
- If `timeZone` is not provided at registration, default to `Asia/Kolkata`.
- Daily/weekly boundaries use the user's timezone for all date computations, otherwise IST.
- Timezone is set only at registration; no inference from locale/device and no per-request override.
- Timezone changes after registration are out of scope for this release.

---

## 3. Event-Driven Architecture

### 3.1 Domain Event Design

```java
public interface DomainEvent {
    Instant occurredAt();
    String eventId();
    String userId();
}

// Immutable event implementations
@Getter @NoArgsConstructor(access = AccessLevel.PRIVATE) @AllArgsConstructor @Builder
public class TaskCompleted implements DomainEvent {
    private String id;
    private String taskId;
    private String userId;
    private String goalId;
    private String keyResultId;
    private long contribution;
    private Instant completedAt;
    
    @Override public Instant occurredAt() { return completedAt; }
    @Override public String eventId() { return id; }
    @Override public String userId() { return userId; }
}

@Getter @NoArgsConstructor(access = AccessLevel.PRIVATE) @AllArgsConstructor @Builder
public class DayClosed implements DomainEvent {
    private String id;
    private LocalDate day;
    private String userId;
    private Instant closedAt;
    
    @Override public Instant occurredAt() { return closedAt; }
    @Override public String eventId() { return id; }
    @Override public String userId() { return userId; }
}
```

### 3.2 Event Publisher Implementation

```java
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

### 3.3 Event Receipt Pattern (Idempotency)

```java
@Document(collection = "eventReceipt")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor @Builder
public class EventReceipt {
    @Id private String id;
    private String eventId;
    private String consumer;        // Service name that processed this event
    private Instant processedAt;
    
    public static EventReceipt of(String eventId, String consumer, Instant processedAt) {
        return EventReceipt.builder()
            .eventId(eventId)
            .consumer(consumer)
            .processedAt(processedAt)
            .build();
    }
}
```

---

## 4. Service Layer Analysis

### 4.1 Streak Service (Behavioral Continuity)

```java
@Service
@RequiredArgsConstructor
public class StreakService {
    private final StreakRepository streakRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;
    
    private static final String CONSUMER_NAME = "STREAK";
    
    @EventListener
    @Transactional
    public void on(DayClosed event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }
        
        DailyPlan plan = dailyPlanRepository.findByUserIdAndDay(event.userId(), event.getDay())
            .orElseThrow(() -> new DailyPlanNotFoundException(event.userId(), event.getDay()));
        
        StreakState state = streakRepository.findByUserId(event.userId())
            .orElseGet(() -> StreakState.builder()
                .userId(event.userId())
                .currentStreak(0)
                .build());
        
        // Calculate streak based on completion ratio
        long totalTasks = plan.getTasks().size();
        long completedTasks = plan.getTasks().stream()
            .filter(task -> task.getStatus() == TaskExecution.ExecutionStatus.COMPLETED)
            .count();
        
        if (totalTasks > 0 && completedTasks == totalTasks) {
            state.setCurrentStreak(state.getCurrentStreak() + 1);
        } else {
            state.setCurrentStreak(0);
        }
        
        streakRepository.save(state);
        eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
    }
}
```

### 4.2 Analytics Service (Historical Interpretation)

```java
@Service
@RequiredArgsConstructor
public class SnapshotService {
    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final GoalSnapshotRepository snapshotRepository;
    private final EventReceiptRepository eventReceiptRepository;
    private final ClockProvider clock;
    
    private static final String CONSUMER_NAME = "SNAPSHOT";
    
    @EventListener
    @Transactional
    public void on(DayClosed event) {
        if (eventReceiptRepository.findByEventIdAndConsumer(event.eventId(), CONSUMER_NAME).isPresent()) {
            return;
        }
        
        goalRepository.findByUserId(event.userId()).forEach(goal -> {
            // Calculate actual progress from all key results
            double actualProgress = keyResultRepository.findByGoalId(goal.getId()).stream()
                .mapToDouble(KeyResult::getProgress)
                .average()
                .orElse(0.0);
            
            // Calculate expected progress based on time elapsed
            double expectedProgress = calculateExpectedProgress(goal, event.getDay());
            
            GoalSnapshot snapshot = GoalSnapshot.builder()
                .goalId(goal.getId())
                .actualProgress(actualProgress)
                .expectedProgress(expectedProgress)
                .snapshottedAt(clock.now())
                .build();
            
            snapshotRepository.save(snapshot);
        });
        
        eventReceiptRepository.save(EventReceipt.of(event.eventId(), CONSUMER_NAME, clock.now()));
    }
    
    private double calculateExpectedProgress(Goal goal, LocalDate currentDate) {
        // Implementation depends on goal time horizon
        // This is a simplified version
        LocalDate start = goal.getStartDate();
        LocalDate end = goal.getEndDate();
        
        if (currentDate.isBefore(start)) return 0.0;
        if (currentDate.isAfter(end)) return 1.0;
        
        long totalDays = ChronoUnit.DAYS.between(start, end);
        long elapsedDays = ChronoUnit.DAYS.between(start, currentDate);
        
        return (double) elapsedDays / totalDays;
    }
}
```

---

## 5. Data Persistence Strategy

### 5.1 MongoDB Collection Design

```yaml
Collections:
  task:
    - Index: {userId: 1, createdAt: -1}
    - Index: {goalId: 1}
    - Index: {keyResultId: 1}
    
  weeklyPlan:
    - Index: {userId: 1, weekNumber: 1, year: 1} (unique)
    
  dailyPlan:
    - Index: {userId: 1, day: 1} (unique)
    - Index: {userId: 1, day: -1} (for recent queries)
    
  goal:
    - Index: {userId: 1, status: 1}
    - Index: {userId: 1, endDate: -1}
    
  keyResult:
    - Index: {goalId: 1}
    - Index: {goalId: 1, type: 1}
    
  goalSnapshot:
    - Index: {goalId: 1, snapshottedAt: -1}
    - Index: {snapshottedAt: -1} (TTL for old snapshots)
    
  streakState:
    - Index: {userId: 1} (unique)
    
  eventReceipt:
    - Index: {eventId: 1, consumer: 1} (unique)
    - Index: {consumer: 1, processedAt: -1} (TTL for cleanup)
```

### 5.2 Repository Pattern Implementation

```java
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserId(String userId);
    Optional<Task> findByIdAndUserId(String id, String userId);
    List<Task> findByGoalId(String goalId);
    List<Task> findByKeyResultId(String keyResultId);
    
    @Query("{ 'userId': ?0, 'completed': false }")
    List<Task> findPendingTasksByUserId(String userId);
}

@Repository
public interface DailyPlanRepository extends MongoRepository<DailyPlan, String> {
    Optional<DailyPlan> findByUserIdAndDay(String userId, LocalDate day);
    List<DailyPlan> findByUserIdAndDayBetween(String userId, LocalDate start, LocalDate end);
    List<DailyPlan> findByUserIdAndClosed(String userId, boolean closed);
}
```

---

## 6. Security & Authentication

User registration accepts an optional `timeZone` field (IANA zone ID) and an optional `weekStart` date for initial weekly plan generation. If `timeZone` is omitted, the system defaults to `Asia/Kolkata`. If `weekStart` is omitted, the system uses the current week (based on the user's timezone and Monday start-of-week). The timezone value is stored on the user record and is used to compute all day/week boundaries for that user. Timezone is not inferred from locale/device and is not overridden by request headers. Registration can also include an optional list of initial goals to bootstrap direction during onboarding.

### 6.1 JWT Implementation

```java
@Component
public class JwtTokenProvider {
    private final String secretKey;
    private final long validityInMilliseconds;
    
    public JwtTokenProvider(@Value("${security.jwt.secret}") String secretKey,
                          @Value("${security.jwt.expiration}") long validityInMilliseconds) {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }
    
    public String createToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 6.2 Security Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/dashboard/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 7. API Design Patterns

### 7.1 Controller Structure

```java
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;
    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        try {
            Task task = taskService.createTask(taskMapper.toCommand(request));
            return ResponseEntity.ok(taskMapper.toResponse(task));
        } catch (DomainException e) {
            return ResponseEntity.badRequest().body(TaskResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(TaskResponse.error("Internal server error"));
        }
    }
    
    @PutMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable String taskId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        taskService.completeTask(taskId, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@RequestParam(required = false) String goalId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Task> tasks = goalId != null 
            ? taskService.getTasksByGoal(userId, goalId)
            : taskService.getTasksByUser(userId);
        return ResponseEntity.ok(tasks.stream()
            .map(taskMapper::toResponse)
            .collect(Collectors.toList()));
    }
}
```

### 7.2 DTO Pattern

```java
@Data @Builder
public class CreateTaskRequest {
    @NotBlank private String description;
    private String goalId;
    private String keyResultId;
    private long contribution;
}

@Data @Builder
public class TaskResponse {
    private String id;
    private String description;
    private boolean completed;
    private String goalId;
    private String keyResultId;
    private long contribution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static TaskResponse error(String message) {
        return TaskResponse.builder()
            .description("ERROR: " + message)
            .build();
    }
}

@Mapper(componentModel = "spring")
public interface TaskMapper {
    CreateTaskCommand toCommand(CreateTaskRequest request);
    TaskResponse toResponse(Task task);
}
```

---

## 8. Testing Strategy

### 8.1 Unit Testing with FixedClock

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock private TaskRepository taskRepository;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private ClockProvider clock;
    
    @InjectMocks private TaskService taskService;
    
    private FixedClockProvider fixedClock;
    
    @BeforeEach
    void setUp() {
        fixedClock = new FixedClockProvider();
        fixedClock.setFixedInstant(Instant.parse("2026-01-20T10:00:00Z"));
        when(clock.now()).thenReturn(fixedClock.now());
    }
    
    @Test
    @DisplayName("Should create task and publish event")
    void createTask_ShouldPublishEvent() {
        // Given
        CreateTaskCommand command = CreateTaskCommand.builder()
            .userId("user1")
            .description("Test task")
            .goalId("goal1")
            .contribution(5)
            .build();
        
        Task savedTask = Task.builder()
            .id("task1")
            .userId("user1")
            .description("Test task")
            .goalId("goal1")
            .contribution(5)
            .completed(false)
            .createdAt(fixedClock.now())
            .build();
        
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        
        // When
        Task result = taskService.createTask(command);
        
        // Then
        assertThat(result.getId()).isEqualTo("task1");
        verify(eventPublisher).publish(argThat(event -> 
            event instanceof TaskCreated && 
            ((TaskCreated) event).getTaskId().equals("task1")
        ));
    }
}
```

### 8.2 Integration Testing

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_focusflow",
    "security.jwt.secret=test-secret-key-for-testing-only"
})
class TaskControllerIntegrationTest {
    
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private TaskRepository taskRepository;
    
    @Autowired private JwtTokenProvider jwtTokenProvider;
    
    private String authToken;
    
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        authToken = "Bearer " + jwtTokenProvider.createToken("test-user");
    }
    
    @Test
    @DisplayName("Should create and retrieve task")
    void createAndRetrieveTask() {
        // Create task
        CreateTaskRequest request = CreateTaskRequest.builder()
            .description("Integration test task")
            .contribution(10)
            .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken.substring(7));
        HttpEntity<CreateTaskRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<TaskResponse> createResponse = restTemplate.postForEntity(
            "/api/tasks", entity, TaskResponse.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody().getDescription()).isEqualTo("Integration test task");
        
        // Retrieve task
        ResponseEntity<TaskResponse[]> getResponse = restTemplate.exchange(
            "/api/tasks", HttpMethod.GET, entity, TaskResponse[].class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).hasSize(1);
        assertThat(getResponse.getBody()[0].getId()).isEqualTo(createResponse.getBody().getId());
    }
}
```

---

## 9. Performance Considerations

### 9.1 Database Optimization

```java
// Repository optimization for dashboard queries
@Repository
public interface DailyPlanRepository extends MongoRepository<DailyPlan, String> {
    
    // Optimized query for today's plan
    @Query(value = "{ 'userId': ?0, 'day': ?1 }", 
           fields = "{ 'tasks': 1, 'closed': 1 }")
    Optional<DailyPlan> findTodayPlanOptimized(String userId, LocalDate today);
    
    // Aggregation for weekly stats
    @Aggregation(pipeline = {
        "{ $match: { 'userId': ?0, 'day': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { '_id': null, 'totalTasks': { $sum: { $size: '$tasks' } }, " +
        "'completedTasks': { $sum: { $size: { $filter: { input: '$tasks', cond: { $eq: ['$$this.status', 'COMPLETED'] } } } } } } }"
    })
    WeeklyStats calculateWeeklyStats(String userId, LocalDate weekStart, LocalDate weekEnd);
}
```

### 9.2 Caching Strategy

```java
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DailyPlanQueryService dailyPlanQueryService;
    private final StreakQueryService streakQueryService;
    private final GoalQueryService goalQueryService;
    private final CacheManager cacheManager;
    
    @Cacheable(value = "dashboard", key = "#userId + ':today'")
    public DashboardResponse getTodayDashboard(String userId) {
        // Build dashboard response
        return DashboardResponse.builder()
            .userId(userId)
            .todayPlan(dailyPlanQueryService.getToday(userId))
            .currentStreak(streakQueryService.getCurrent(userId))
            .goalSummaries(goalQueryService.getActiveGoals(userId))
            .build();
    }
    
    @CacheEvict(value = "dashboard", key = "#userId + ':today'")
    public void evictTodayCache(String userId) {
        // Cache eviction handled by annotation
    }
}
```

---

## 10. Deployment & Operations

### 10.1 Docker Configuration

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY build/libs/focusflow-*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV MONGODB_URI=mongodb://mongodb:27017/focusflow
ENV REDIS_URI=redis://redis:6379

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 10.2 Docker Compose

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - MONGODB_URI=mongodb://mongodb:27017/focusflow
      - REDIS_URI=redis://redis:6379
      - SECURITY_JWT_SECRET=your-secret-key-here
    depends_on:
      - mongodb
      - redis

  mongodb:
    image: mongo:6.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    environment:
      - MONGO_INITDB_DATABASE=focusflow

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  mongodb_data:
  redis_data:
```

### 10.3 Monitoring & Health Checks

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // Check MongoDB connectivity
            mongoTemplate.executeCommand("{ ping: 1 }");
            
            // Check Redis connectivity
            redisTemplate.opsForValue().set("health-check", "ok", Duration.ofSeconds(10));
            
            return Health.up()
                .withDetail("database", "MongoDB is responsive")
                .withDetail("cache", "Redis is responsive")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## Conclusion

This technical deep dive provides a comprehensive understanding of the FocusFlow system's architecture, from domain modeling through deployment considerations. The system embodies strict architectural principles:

- **Temporal Immutability**: Historical facts are never rewritten
- **Event-Driven Design**: Cross-domain communication via immutable events
- **Layered Separation**: Clear boundaries between intent, execution, and interpretation
- **Domain-Driven Design**: Rich domain models with encapsulated business logic

The architecture ensures long-term maintainability while providing a solid foundation for scaling and feature evolution.
