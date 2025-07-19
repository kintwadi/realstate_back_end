package com.imovel.api.security.aspect;

import com.imovel.api.error.ApiCode;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.util.Util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Aspect for handling validation logic for authentication endpoints.
 * Performs validation checks for both user registration and login requests.
 */
@Aspect
@Component
public class AuthControllerAspect {

    /**
     * Pointcut for the user registration endpoint.
     * Targets the register method in AuthController.
     *
     * <p>Usage: Apply advice to validate or process user registration requests.</p>
     */
    @Pointcut("execution(* com.imovel.api.controller.AuthController.registerValidation(..))")
    public static void registerValidation() {
        // Pointcut method - implementation will be provided by AspectJ
    }

    /**
     * Pointcut for the user login endpoint.
     * Targets the login method in AuthController.
     *
     * <p>Usage: Apply advice to validate or process user authentication requests.</p>
     */
    @Pointcut("execution(* com.imovel.api.controller.AuthController.authenticateUser(..))")
    public static void authenticateUser() {
        // Pointcut method - implementation will be provided by AspectJ
    }

    @Pointcut("execution(* com.imovel.api.controller.AuthController.initiatePasswordReset(..))")
    public static void initiatePasswordReset() {
        // Pointcut method - implementation will be provided by AspectJ
    }

    /**
     * Validates user registration requests before processing.
     *
     * @param joinPoint The proceeding join point
     * @return ResponseEntity with error if validation fails, or proceeds otherwise
     * @throws Throwable if proceeding join point throws an exception
     */
    @Around("registerValidation()")
    public Object registerValidation(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // Validate request payload
        if (args.length == 0 || !(args[0] instanceof UserRegistrationRequest)) {
            return AspectErrorResponse.createErrorResponse(ApiCode.INVALID_PAYLOAD.getMessage(), ApiCode.INVALID_PAYLOAD.getCode(), HttpStatus.CONFLICT);
        }

        UserRegistrationRequest request = (UserRegistrationRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getPassword())) {
            return AspectErrorResponse.createErrorResponse(ApiCode.REQUIRED_FIELD_MISSING.getMessage(), ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }

        // Validate email format
        if (Util.isEmailInvalid(request.getEmail())) {
            return AspectErrorResponse.createErrorResponse(ApiCode.INVALID_EMAIL.getMessage(), ApiCode.INVALID_EMAIL.getCode(), HttpStatus.BAD_REQUEST);
        }

        return joinPoint.proceed();
    }

    /**
     * Validates user login requests before processing.
     *
     * @param joinPoint The proceeding join point
     * @return ResponseEntity with error if validation fails, or proceeds otherwise
     * @throws Throwable if proceeding join point throws an exception
     */

//    @Around("authenticateUser()")
//    public Object authenticateUser(final ProceedingJoinPoint joinPoint) throws Throwable {
//        Object[] args = joinPoint.getArgs();
//
//        // Validate request payload (implicit in aspect pointcut)
//        UserLoginRequest request = (UserLoginRequest) args[0];
//
//        // Validate required fields
//        if (areFieldsMissing(request.getEmail(), request.getPassword())) {
//            return AspectErrorResponse.createErrorResponse(ApiCode.REQUIRED_FIELD_MISSING.getMessage(), ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
//        }
//
//        // Validate email format
//        if (Util.isEmailInvalid(request.getEmail())) {
//            return AspectErrorResponse.createErrorResponse(ApiCode.INVALID_EMAIL.getMessage(), ApiCode.INVALID_EMAIL.getCode(), HttpStatus.BAD_REQUEST);
//        }
//
//        return joinPoint.proceed();
//    }
    @Around("initiatePasswordReset()")
    public Object resetPassword(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        PasswordChangeRequest request = (PasswordChangeRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getOldPassword())) {
            return AspectErrorResponse.createErrorResponse(ApiCode.REQUIRED_FIELD_MISSING.getMessage(), ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }
        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getNewPassword())) {
            return AspectErrorResponse.createErrorResponse(ApiCode.REQUIRED_FIELD_MISSING.getMessage(), ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }
        // Validate email format
        if (Util.isEmailInvalid(request.getEmail())) {
            return AspectErrorResponse.createErrorResponse(ApiCode.INVALID_EMAIL.getMessage(), ApiCode.INVALID_EMAIL.getCode(), HttpStatus.BAD_REQUEST);
        }

        return joinPoint.proceed();
    }

    /**
     * Checks if required fields are missing or empty.
     *
     * @param email The email to check
     * @param password The password to check
     * @return true if either field is null or empty, false otherwise
     */
    private static boolean areFieldsMissing(final String email, final String password) {
        return email == null || email.isEmpty() ||
                password == null || password.isEmpty();
    }

}