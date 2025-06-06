package com.imovel.api.security.aspect;

import com.imovel.api.error.ApiCode;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
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
            return createErrorResponse("Invalid payload", ApiCode.INVALID_PAYLOAD.getCode(), HttpStatus.CONFLICT);
        }

        UserRegistrationRequest request = (UserRegistrationRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getPassword())) {
            return createErrorResponse("Missing required fields", ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }

        // Validate email format
        if (isEmailInvalid(request.getEmail())) {
            return createErrorResponse("Email not valid", ApiCode.INVALID_EMAIL.getCode(), HttpStatus.BAD_REQUEST);
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

    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.authenticateUser()")
    public Object authenticateUser(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // Validate request payload (implicit in aspect pointcut)
        UserLoginRequest request = (UserLoginRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getPassword())) {
            return createErrorResponse("Missing required fields", ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }

        // Validate email format
        if (isEmailInvalid(request.getEmail())) {
            return createErrorResponse("Email not valid", ApiCode.INVALID_EMAIL.getCode(), HttpStatus.BAD_REQUEST);
        }
        return joinPoint.proceed();
    }

    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.changeUserPassword()")
    public Object resetPassword(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        PasswordChangeRequest request = (PasswordChangeRequest) args[0];

        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getOldPassword())) {
            return createErrorResponse("Missing required fields", ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }
        // Validate required fields
        if (areFieldsMissing(request.getEmail(), request.getNewPassword())) {
            return createErrorResponse("Missing required fields", ApiCode.REQUIRED_FIELD_MISSING.getCode(), HttpStatus.BAD_REQUEST);
        }
        // Validate email format
        if (isEmailInvalid(request.getEmail())) {
            return createErrorResponse("Email not valid", ApiCode.INVALID_EMAIL.getCode(), HttpStatus.BAD_REQUEST);
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
            final String message, final long code, final HttpStatus status)
    {
        StandardResponse<String> standardResponse = StandardResponse.error(code,message,status);
        return new ResponseEntity<>(standardResponse,status);
    }
}