# Code Design & Engineering Principles

This document defines the **non-negotiable engineering standards** for this codebase.  
Every contributor is expected to follow these rules strictly.

The goals are:

- Long-term maintainability
- Predictable structure
- High signal-to-noise code
- Low cognitive load
- Easy onboarding
- Production-grade reliability

---

## 1. Size Constraints

1. No class should exceed **300 lines**.
2. No method should exceed **90 lines**.
3. If a class or method grows beyond this:
    - Extract responsibilities.
    - Introduce helpers or utilities.
    - Split into smaller, focused components.

These limits exist to enforce **Single Responsibility** and **readability**.

---

## 2. Separation of Concerns (Layered Architecture)

Only the following layers are allowed:

### a. Controller Layer
- Responsible for:
    - Accepting requests
    - Basic validation of request parameters
    - Mapping request → DTO
    - Handling errors and translating them into HTTP responses
- Must:
    - Handle `400`, `429`, `500`, etc. gracefully
    - Wrap logic in try/catch
    - Never contain business logic
- Purpose: *UI-facing contract + graceful failure*

### b. Business Service Layer
- Responsible for:
    - Pure business logic
    - Orchestration of flows
    - Domain rules and decisions
- Must:
    - Not know about HTTP, controllers, or UI
    - Not talk directly to the database

### c. Repository Layer
- Responsible for:
    - Interacting with the database
    - Queries and persistence only
- Must:
    - Not contain business rules
    - Not perform transformations beyond basic mapping

### d. DTOs & Adapters
- Use DTOs and Adapters when crossing layers.
- Each layer communicates via explicit contracts.
- No leaking of internal models across boundaries.

### e. No Extra Layers
- Only these layers are allowed.
- Do not introduce arbitrary “manager”, “handler”, or “helper” layers unless justified.

---

## 3. Code Quality

1. Clean code is **mandatory**.
2. Every class must have:
    - A clear purpose
    - A single responsibility
3. Methods must:
    - Do one thing
    - Be readable without mental gymnastics
4. Prefer:
    - Small methods
    - Clear naming
    - Explicit flow over cleverness

---

## 4. Design Patterns

- Use established design patterns where appropriate.
- Follow **Gang of Four** principles.
- Patterns must:
    - Reduce coupling
    - Increase clarity
    - Solve real problems (not decorative)

Avoid pattern abuse.

---

## 5. Constants & Strings

1. **No magic strings** in code.
2. All strings must be:
    - `static final` constants, or
    - Moved to a dedicated `*Constants` class if many exist.
3. Environment-specific values:
    - Must come from environment variables
    - Never be hardcoded

---

## 6. Models & Lombok

All models must use Lombok with:

```java
@Getter
@Setter
@AllArgsConstructor
@Builder
```
This is mandatory for:
- Entities
- DTOs
- Domain models
---
### 7. Logging

Services must include debug logs for traceability.

Debug logs must be guarded:
```java
if (LogUtil.isDebugEnabled()) {
    Log.debug("[<ClassName>] Message: {}", variable);
}
```


Any:
- String concatenation
- JSON serialization
- Expensive formatting

Must be wrapped in utilities that check debug state first.

No wasted compute when debug is disabled.

---

### 8. Naming Conventions

All class names must be Capitalized.

All modules and folders must be camelCase.

Names must be:

- Intent-revealing
- Domain-aligned
- Unambiguous

---

### 9. Module Structure

Modules must not be bloated.

Responsibilities must be distributed effectively.

Avoid:
- God modules
- Catch-all packages
- Dumping unrelated features together

Structure should mirror business domains, not technical convenience.

---

### 10. Engineering References

Coding principles must align with:
- Gang of Four design patterns
- Effective Java best practices
Favor:
- Immutability
- Defensive copying
- Explicit null handling
- Fail-fast behavior
---
### 11. Testing
Every business service must have unit tests.
Tests must:
- Cover happy paths
- Cover edge cases
- Cover failure paths

Controllers must have:
- Basic integration tests for request/response contracts

No feature is “done” without tests.

---
### 12. Error Handling
Business layer throws domain-specific exceptions.
Controllers translate exceptions into HTTP responses.
No generic Exception propagation across layers.
Errors must be:
- Typed
- Meaningful
- Actionable

---
### 13. Dependency Rules
Controllers depend on Services.
Services depend on Repositories.
Repositories depend on infrastructure.
Reverse dependencies are forbidden.
No layer may reach upwards.

---
### 14. Configuration & Secrets

All private keys, tokens, and secrets must be driven by environment variables.
No secrets may be committed to the repository.
Provide .env.example for documentation.
Fail fast if required environment variables are missing.