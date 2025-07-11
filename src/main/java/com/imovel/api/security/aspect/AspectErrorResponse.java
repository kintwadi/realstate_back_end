package com.imovel.api.security.aspect;

import com.imovel.api.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AspectErrorResponse {


    /**
     * Creates a standardized error response.
     *
     * @param message The error message
     * @param code The error code
     * @param status The HTTP status
     * @return ResponseEntity containing the error response
     */
    public static ResponseEntity<StandardResponse<String>> createErrorResponse(
            final String message, final long code, final HttpStatus status)
    {
        StandardResponse<String> standardResponse = StandardResponse.error(code,message,status);
        return new ResponseEntity<>(standardResponse,status);
    }
}
