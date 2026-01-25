package com.personal.planner.api;

import com.personal.planner.domain.common.exception.AuthenticationException;
import com.personal.planner.domain.common.exception.AuthorizationException;
import com.personal.planner.domain.common.exception.DomainViolationException;
import com.personal.planner.domain.common.exception.EntityNotFoundException;
import com.personal.planner.domain.common.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST API.
 * Translates domain exceptions to HTTP responses using {@link ApiResponse}.
 * 
 * <p>Controllers should NOT catch exceptions that this handler manages.
 * Let exceptions propagate and this handler will translate them to appropriate
 * HTTP status codes with consistent error response format.</p>
 * 
 * <p>Exception mapping:
 * <ul>
 *   <li>EntityNotFoundException → 404 NOT FOUND</li>
 *   <li>AuthorizationException → 403 FORBIDDEN</li>
 *   <li>DomainViolationException → 409 CONFLICT</li>
 *   <li>InvalidRequestException → 400 BAD REQUEST (or 409 if duplicate email)</li>
 *   <li>AuthenticationException → 401 UNAUTHORIZED</li>
 *   <li>All other exceptions → 500 INTERNAL SERVER ERROR</li>
 * </ul>
 * </p>
 * 
 * <p>All error responses follow the standard {@link ApiResponse} format with
 * {@code success=false}, {@code errorCode}, and {@code message} fields.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle entity not found errors.
     * Returns 404 NOT FOUND.
     * 
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        LOG.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    /**
     * Handle authorization errors (ownership/permission failures).
     * Returns 403 FORBIDDEN.
     * 
     * @param ex the exception
     * @return error response with 403 status
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(AuthorizationException ex) {
        LOG.warn("Authorization failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", ex.getMessage()));
    }

    /**
     * Handle domain violations (e.g., modifying closed plan).
     * Returns 409 CONFLICT.
     * 
     * @param ex the exception
     * @return error response with 409 status
     */
    @ExceptionHandler(DomainViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DomainViolationException ex) {
        LOG.warn("Domain violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", ex.getMessage()));
    }

    /**
     * Handle validation errors.
     * Returns 400 BAD REQUEST, or 409 CONFLICT for duplicate email registration.
     * 
     * @param ex the exception
     * @return error response with 400 or 409 status
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(InvalidRequestException ex) {
        // Check if this is a duplicate email registration (409)
        if (ex.getMessage() != null && ex.getMessage().contains("Email already exists")) {
            LOG.warn("Duplicate registration attempt: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("CONFLICT", ex.getMessage()));
        }
        
        // Otherwise, return 400 for validation errors
        LOG.debug("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
    }

    /**
     * Handle authentication errors (login failures).
     * Returns 401 UNAUTHORIZED.
     * 
     * @param ex the exception
     * @return error response with 401 status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(AuthenticationException ex) {
        LOG.debug("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", ex.getMessage()));
    }

    /**
     * Catch-all for unexpected errors.
     * Returns 500 INTERNAL SERVER ERROR.
     * Does NOT expose internal details to client.
     * 
     * @param ex the exception
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        LOG.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
