package com.personal.planner.api;

import com.personal.planner.domain.common.exception.AuthenticationException;
import com.personal.planner.domain.common.exception.AuthorizationException;
import com.personal.planner.domain.common.exception.DomainViolationException;
import com.personal.planner.domain.common.exception.EntityNotFoundException;
import com.personal.planner.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GlobalExceptionHandler HTTP status code mapping.
 *
 * <p>Verifies that domain exceptions are correctly mapped to appropriate
 * HTTP status codes according to the API contract. All responses use
 * {@link ApiResponse} for consistent error formatting.</p>
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("EntityNotFoundException should map to 404 NOT FOUND")
    void entityNotFoundExceptionMapsTo404() {
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Entity not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("AuthorizationException should map to 403 FORBIDDEN")
    void authorizationExceptionMapsTo403() {
        AuthorizationException ex = new AuthorizationException("Access denied");
        ResponseEntity<ApiResponse<Void>> response = handler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("FORBIDDEN", response.getBody().getErrorCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    @DisplayName("DomainViolationException should map to 409 CONFLICT")
    void domainViolationExceptionMapsTo409() {
        DomainViolationException ex = new DomainViolationException("Cannot modify closed plan");
        ResponseEntity<ApiResponse<Void>> response = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("CONFLICT", response.getBody().getErrorCode());
        assertEquals("Cannot modify closed plan", response.getBody().getMessage());
    }

    @Test
    @DisplayName("InvalidRequestException should map to 400 BAD REQUEST")
    void invalidRequestExceptionMapsTo400() {
        InvalidRequestException ex = new InvalidRequestException("Invalid input");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("BAD_REQUEST", response.getBody().getErrorCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    @DisplayName("InvalidRequestException with duplicate email should map to 409 CONFLICT")
    void invalidRequestExceptionWithDuplicateEmailMapsTo409() {
        InvalidRequestException ex = new InvalidRequestException("Email already exists");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("CONFLICT", response.getBody().getErrorCode());
        assertEquals("Email already exists", response.getBody().getMessage());
    }

    @Test
    @DisplayName("AuthenticationException should map to 401 UNAUTHORIZED")
    void authenticationExceptionMapsTo401() {
        AuthenticationException ex = new AuthenticationException("Invalid credentials");
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorized(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("UNAUTHORIZED", response.getBody().getErrorCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Unexpected exceptions should map to 500 INTERNAL SERVER ERROR")
    void unexpectedExceptionMapsTo500() {
        RuntimeException ex = new RuntimeException("Unexpected error");
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
