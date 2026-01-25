package com.personal.planner.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Standard API response wrapper for all endpoints.
 * 
 * <p>Provides a consistent response format across all API endpoints:
 * <ul>
 *   <li>{@code success} - boolean indicating if the request was successful</li>
 *   <li>{@code data} - the response payload (null on error)</li>
 *   <li>{@code errorCode} - error code (null on success)</li>
 *   <li>{@code message} - human-readable message (null on success)</li>
 * </ul>
 * </p>
 * 
 * <p>Example success response:</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "errorCode": null,
 *   "message": null
 * }
 * </pre>
 * 
 * <p>Example error response:</p>
 * <pre>
 * {
 *   "success": false,
 *   "data": null,
 *   "errorCode": "NOT_FOUND",
 *   "message": "Task not found: abc123"
 * }
 * </pre>
 *
 * @param <T> the type of the response data
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the request was successful.
     */
    private final boolean success;

    /**
     * The response payload. Null on error responses.
     */
    private final T data;

    /**
     * Error code for categorizing the error. Null on success.
     */
    private final String errorCode;

    /**
     * Human-readable error message. Null on success.
     */
    private final String message;

    /**
     * Creates a successful response with the given data.
     *
     * @param data the response data
     * @param <T> the type of the data
     * @return a successful API response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with no data (for void operations).
     *
     * @return a successful API response with null data
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }

    /**
     * Creates an error response with the given error code and message.
     *
     * @param errorCode the error code
     * @param message the error message
     * @param <T> the type parameter (will be null)
     * @return an error API response
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
