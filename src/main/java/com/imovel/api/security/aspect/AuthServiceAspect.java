package com.imovel.api.security.aspect;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.AuthenticationException;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.User;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.security.PasswordManager;
import com.imovel.api.services.AuthDetailsService;
import com.imovel.api.services.AuthService;
import com.imovel.api.util.Util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Aspect for handling authentication-related cross-cutting concerns.
 * Provides around advice for user registration and login processes.
 */
@Aspect
@Component
public class AuthServiceAspect {

    private final PasswordManager passwordManager;
    private final AuthDetailsService authDetailsService;
    private final AuthService authService;

    /**
     * Constructor for dependency injection.
     *
     * @param passwordManager     Handles password hashing and verification
     * @param authDetailsService  Service for auth details operations
     * @param authService         Main authentication service
     */
    @Autowired
    public AuthServiceAspect(PasswordManager passwordManager,
                             AuthDetailsService authDetailsService,
                             AuthService authService) {
        this.passwordManager = passwordManager;
        this.authDetailsService = authDetailsService;
        this.authService = authService;
    }

    @Pointcut("execution(* com.imovel.api.services.AuthService.loginUser(..))")
    public static void loginUser() {
        // Pointcut method - implementation will be provided by AspectJ
    }

    @Pointcut("execution(* com.imovel.api.services.AuthService.registerUser(..))")
    public static void registerUser() {
        // Pointcut method - implementation will be provided by AspectJ
    }

    @Pointcut("execution(* com.imovel.api.services.AuthService.changeUserPassword(..))")
    public static void changeUserPassword() {
        // Pointcut method - implementation will be provided by AspectJ
    }

    /**
     * Around advice for user registration process.
     * Handles creation of authentication details after successful user registration.
     *
     * @param joinPoint The proceeding join point
     * @return StandardResponse containing the registered user if successful
     * @throws Throwable if an error occurs during processing
     */
    @Around("registerUser()")
    public Object registerUser(final ProceedingJoinPoint joinPoint) throws Throwable {
        UserRegistrationRequest request = (UserRegistrationRequest) joinPoint.getArgs()[0];

        // Validate email format
        if (Util.isEmailInvalid(request.getEmail())) {

            return StandardResponse.error(ApiCode.INVALID_EMAIL.getCode(), ApiCode.INVALID_EMAIL.getMessage(), HttpStatus.BAD_REQUEST);
        }

        StandardResponse<User> response = (StandardResponse<User>) joinPoint.proceed();

        if (!response.isSuccess() || response.getData() == null) {
            return response;
        }

        // Create and save authentication details for the new user
        AuthDetails authDetails = passwordManager.createAuthDetails(request.getPassword());
        authDetails.setUserId(response.getData().getId());
        authDetailsService.save(authDetails);

        return response;
    }

    /**
     * Around advice for user login process.
     * Verifies user credentials before proceeding with login.
     *
     * @param joinPoint The proceeding join point
     * @return StandardResponse with login result if credentials are valid
     * @throws Throwable if an error occurs during processing
     */
    @Around("loginUser()")
    public Object loginUser(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        String password = (String) args[1];

        Optional<User> optionalUser = authService.findByEmail(email);

        if (!optionalUser.isPresent() || !verifyUserPassword(optionalUser.get().getId(), password)) {
            throw new AuthenticationException(ApiCode.INVALID_CREDENTIALS.getCode(), ApiCode.INVALID_CREDENTIALS.getMessage());
        }

        return joinPoint.proceed();
    }

    /**
     * Interceptor method that verifies the user's current password before allowing a password change.
     *
     * @param joinPoint The proceeding join point for the intercepted method
     * @return StandardResponse indicating the result of the password change operation
     * @throws Throwable if an error occurs during processing
     */
    @Around("changeUserPassword()")
    public Object verifyPasswordBeforeChange(final ProceedingJoinPoint joinPoint) throws Throwable {
        final PasswordChangeRequest passwordChangeRequest = (PasswordChangeRequest) joinPoint.getArgs()[0];

        final Optional<User> optionalUser = authService.findByEmail(passwordChangeRequest.getEmail());

        if (!optionalUser.isPresent()) {
            return StandardResponse.error(ApiCode.PASSWORD_RESET_FAILED.getCode(), ApiCode.PASSWORD_RESET_FAILED.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (!isPasswordValid(optionalUser.get().getId(), passwordChangeRequest.getOldPassword())) {
            return StandardResponse.error(ApiCode.PASSWORD_RESET_FAILED.getCode(), ApiCode.PASSWORD_RESET_FAILED.getMessage(),HttpStatus.BAD_REQUEST);
        }

        // Update authentication details
        final AuthDetails newAuthDetails = passwordManager.createAuthDetails(passwordChangeRequest.getNewPassword());
        final AuthDetails currentAuthDetails = authDetailsService.findByUserId(optionalUser.get().getId())
                .getData();

        currentAuthDetails.setHash(newAuthDetails.getHash());
        currentAuthDetails.setSalt(newAuthDetails.getSalt());

        authDetailsService.save(currentAuthDetails);

        return joinPoint.proceed();
    }
    /**
     * Verifies if the provided password matches the user's current password.
     *
     * @param userId The ID of the user to verify
     * @param password The password to verify
     * @return true if the password is valid, false otherwise
     */
    private boolean isPasswordValid(Long userId, String password) {
        return verifyUserPassword(userId, password);
    }

    /**
     * Verifies if the provided password matches the stored credentials for a user.
     *
     * @param userId   The ID of the user to verify
     * @param password The password to verify
     * @return true if the password is valid, false otherwise
     */
    private boolean verifyUserPassword(Long userId, String password) {
        StandardResponse<AuthDetails> authDetailsResponse = authDetailsService.findByUserId(userId);
        if (!authDetailsResponse.isSuccess()) {
            return false;
        }

        AuthDetails authDetails = authDetailsResponse.getData();
        return passwordManager.verifyPassword(password, authDetails.getHash(), authDetails.getSalt());
    }
}