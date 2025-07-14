package com.imovel.api.response;

import com.imovel.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * Standardized response format for all API responses.
 * 
 * @param <T> Type of the data payload
 */
public class ApplicationResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorCode error;
    private Instant timestamp;

    public ApplicationResponse(){
        
    }

    // Private constructor to enforce use of factory methods
    public ApplicationResponse(boolean success, T data, String message, ErrorCode error) {
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
    public static <T> ApplicationResponse<T> success(T data) {
        return new ApplicationResponse<>(true, data, null, null);
    }

    /**
     * Creates a success response with data and message
     * 
     * @param data The successful response data
     * @param message Descriptive success message
     * @return StandardResponse with success status, data, and message
     */
    public static <T> ApplicationResponse<T> success(T data, String message) {
        return new ApplicationResponse<>(true, data, message, null);
    }

    /**
     * Creates a success response with message only
     * 
     * @param message Descriptive success message
     * @return StandardResponse with success status and message
     */
    public static <T> ApplicationResponse<T> success(String message) {
        return new ApplicationResponse<>(true, null, message, null);
    }

    /**
     * Creates an error response with error code and message
     * 
     * @param code Error code
     * @param message Error message
     * @return StandardResponse with error details
     */
    public static <T> ApplicationResponse<T> error(long code, String message, HttpStatus status)
    {
        return new ApplicationResponse<>(false, null, null, new ErrorCode(code, message,status));
    }

    /**
     * Creates an error response with ErrorResponse object
     * 
     * @param error ErrorResponse containing code and message
     * @return StandardResponse with error details
     */
    public static <T> ApplicationResponse<T> error(ErrorCode error) {
        return new ApplicationResponse<>(false, null, null, error);
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

    public ErrorCode getError() {
        return error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }
}