package com.imovel.api.error;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ErrorCode {
    private String code;
    private String message;
    private Instant timestamp;
    private HttpStatus status;

    public ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
        this.status = status;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }
}