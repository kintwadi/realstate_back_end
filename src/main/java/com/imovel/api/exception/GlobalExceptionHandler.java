package com.imovel.api.exception;

import com.imovel.api.error.ApiCode;
import com.imovel.api.response.StandardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<StandardResponse<Object>> handleApiException(ApiException ex) {
        return new ResponseEntity<>(
                StandardResponse.error(ex.getErrorCode()),
                ex.getErrorCode().getStatus()
        );
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<StandardResponse<Object>> handleAuthorizationException(AuthorizationException ex) {
        return new ResponseEntity<>(
                StandardResponse.error(ex.getErrorCode()),
                ex.getErrorCode().getStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String errorMessage = "Validation Failed: " + details;

        return new ResponseEntity<>(
                StandardResponse.error(ApiCode.VALIDATION_ERROR.getCode(), errorMessage, ApiCode.VALIDATION_ERROR.getHttpStatus()),
                ApiCode.VALIDATION_ERROR.getHttpStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();

        ApiCode code = ApiCode.SYSTEM_ERROR;
        return new ResponseEntity<>(
                StandardResponse.error(code.getCode(), code.getMessage(), code.getHttpStatus()),
                code.getHttpStatus()
        );
    }
}
