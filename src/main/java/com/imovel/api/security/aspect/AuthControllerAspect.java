package com.imovel.api.security.aspect;

import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.ErrorResponse;
import com.imovel.api.response.StandardResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Aspect for handling validation logic for authentication endpoints.
 * Performs validation checks for both user registration and login requests.
 */
@Aspect
@Component
public class AuthControllerAspect {

    // Regex pattern for validating email format
    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Error codes for consistent error responses
    private static final String INVALID_PAYLOAD_CODE = "REGISTRATION_001";
    private static final String MISSING_FIELDS_CODE = "REGISTRATION_002";
    private static final String INVALID_EMAIL_CODE = "REGISTRATION_003";

    // Error messages
    private static final String INVALID_PAYLOAD_MSG = "Invalid request payload";
    private static final String MISSING_FIELDS_MSG = "Missing required fields";
    private static final String INVALID_EMAIL_MSG = "Invalid email format";

    /**
     * Validates user registration requests before processing.
     *
     * @param joinPoint The proceeding join point
     * @return ResponseEntity with error if validation fails, or proceeds otherwise
     * @throws Throwable if proceeding join point throws an exception
     */
    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.registerValidation()")
    public Object registerValidation(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // Validate request payload
        if (args.length == 0 || !(args[0] instanceof UserRegistrationRequest)) {
            return createErrorResponse(INVALID_PAYLOAD_MSG, INVALID_PAYLOAD_CODE, HttpStatus.BAD_REQUEST);
        }

        UserRegistrationRequest request = (UserRegistrationRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getPassword())) {
            return createErrorResponse(MISSING_FIELDS_MSG, MISSING_FIELDS_CODE, HttpStatus.BAD_REQUEST);
        }

        // Validate email format
        if (isEmailInvalid(request.getEmail())) {
            return createErrorResponse(INVALID_EMAIL_MSG, INVALID_EMAIL_CODE, HttpStatus.BAD_REQUEST);
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

    public Object loginValidation(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // Validate request payload (implicit in aspect pointcut)
        UserLoginRequest request = (UserLoginRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getPassword())) {
            return createErrorResponse(MISSING_FIELDS_MSG, MISSING_FIELDS_CODE, HttpStatus.BAD_REQUEST);
        }

        // Validate email format
        if (isEmailInvalid(request.getEmail())) {
            return createErrorResponse(INVALID_EMAIL_MSG, INVALID_EMAIL_CODE, HttpStatus.BAD_REQUEST);
        }



        return joinPoint.proceed();
    }

    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.resetPassword()")
    public Object resetPassword(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        PasswordChangeRequest request = (PasswordChangeRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getOldPassword())) {
            return createErrorResponse(MISSING_FIELDS_MSG, MISSING_FIELDS_CODE, HttpStatus.BAD_REQUEST);
        }
        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getNewPassword())) {
            return createErrorResponse(MISSING_FIELDS_MSG, MISSING_FIELDS_CODE, HttpStatus.BAD_REQUEST);
        }
        // Validate email format
        if (isEmailInvalid(request.getEmail())) {
            return createErrorResponse(INVALID_EMAIL_MSG, INVALID_EMAIL_CODE, HttpStatus.BAD_REQUEST);
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

    /**
     * Validates email format against regex pattern.
     *
     * @param email The email to validate
     * @return true if email doesn't match pattern, false otherwise
     */
    private static boolean isEmailInvalid(final String email) {
        return !EMAIL_REGEX_PATTERN.matcher(email).matches();
    }

    /**
     * Creates a standardized error response.
     *
     * @param message The error message
     * @param code The error code
     * @param status The HTTP status
     * @return ResponseEntity containing the error response
     */
    private ResponseEntity<StandardResponse<String>> createErrorResponse(
            final String message, final String code, final HttpStatus status)
    {
        StandardResponse<String> standardResponse = StandardResponse.error(code,message);
        return new ResponseEntity<>(standardResponse,status);
    }
}