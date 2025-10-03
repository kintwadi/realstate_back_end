package com.imovel.api.security.aspect;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.exception.ApiException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.response.ApplicationResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Aspect for handling exceptions thrown by controller methods.
 * This aspect provides centralized exception handling and converts exceptions
 * into standardized API responses with appropriate HTTP status codes.
 */
@Aspect
@Component
public class ExceptionHandlingAspect {

//    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
//    public void controllerMethods() {}

    @Pointcut("execution(* (@org.springframework.web.bind.annotation.RestController *).*(..))")
    public void controllerMethods() {}
    /**
     * Advice that wraps around controller methods to handle exceptions.
     *
     * @param joinPoint The proceeding join point representing the intercepted method
     * @return ResponseEntity containing a standardized error response
     * @throws Throwable if an unexpected error occurs during processing
     */
    @Around("controllerMethods()")
    public Object handleAuthExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Proceed with the original method execution
            return joinPoint.proceed();

        } catch (ApiException ex) {
            // Handle known application-specific exceptions
            ApiLogger.error("ExceptionHandlingAspect", "ApiException caught: " + ex.getMessage(), ex);
            ErrorCode errorCode = new ErrorCode(
                    ex.getErrorCode().getCode(),
                    ex.getErrorCode().getMessage(),
                    ex.getErrorCode().getStatus()
            );

            return ApplicationResponse.error(errorCode);

        } catch (Exception ex) {
            // Handle all other unexpected exceptions
            ApiLogger.error("ExceptionHandlingAspect", "Unexpected exception caught in " + joinPoint.getSignature().toShortString() + ": " + ex.getClass().getSimpleName() + " - " + ex.getMessage(), ex);
            ErrorCode errorCode = new ErrorCode(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    ex.getLocalizedMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

            return ApplicationResponse.error(errorCode);
        }
    }
}