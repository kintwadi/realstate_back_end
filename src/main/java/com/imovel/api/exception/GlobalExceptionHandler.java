package com.imovel.api.exception;

import com.imovel.api.response.ErrorResponse;
import com.imovel.api.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<StandardResponse<?>> handleApplicationException(ApplicationException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(StandardResponse.error(ex.getErrorResponse()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR", 
                "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(errorResponse));
    }
}