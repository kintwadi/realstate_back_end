package com.imovel.api.response;

import java.time.Instant;

/**
 * Standardized response format for all API responses.
 * 
 * @param <T> Type of the data payload
 */
public class StandardResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorResponse error;
    private Instant timestamp;

    // Private constructor to enforce use of factory methods
    private StandardResponse(boolean success, T data, String message, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.timestamp = Instant.now();
    }

    // Factory methods

    /**
     * Creates a success response with data only
     * 
     * @param data The successful response data
     * @return StandardResponse with success status and data
     */
    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(true, data, null, null);
    }

    /**
     * Creates a success response with data and message
     * 
     * @param data The successful response data
     * @param message Descriptive success message
     * @return StandardResponse with success status, data, and message
     */
    public static <T> StandardResponse<T> success(T data, String message) {
        return new StandardResponse<>(true, data, message, null);
    }

    /**
     * Creates a success response with message only
     * 
     * @param message Descriptive success message
     * @return StandardResponse with success status and message
     */
    public static <T> StandardResponse<T> success(String message) {
        return new StandardResponse<>(true, null, message, null);
    }

    /**
     * Creates an error response with error code and message
     * 
     * @param code Error code
     * @param message Error message
     * @return StandardResponse with error details
     */
    public static <T> StandardResponse<T> error(String code, String message) {
        return new StandardResponse<>(false, null, null, new ErrorResponse(code, message));
    }

    /**
     * Creates an error response with ErrorResponse object
     * 
     * @param error ErrorResponse containing code and message
     * @return StandardResponse with error details
     */
    public static <T> StandardResponse<T> error(ErrorResponse error) {
        return new StandardResponse<>(false, null, null, error);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public ErrorResponse getError() {
        return error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    // Setters (if needed for flexibility)
    public void setData(T data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setError(ErrorResponse error) {
        this.error = error;
    }
}