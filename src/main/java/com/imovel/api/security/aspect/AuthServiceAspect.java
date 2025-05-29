package com.imovel.api.security.aspect;

import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.User;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.security.PasswordManager;
import com.imovel.api.services.AuthDetailsService;
import com.imovel.api.services.AuthService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Aspect for handling authentication-related cross-cutting concerns.
 * Provides around advice for user registration and login processes.
 */
@Aspect
@Component
public class AuthServiceAspect {

    // Error messages and codes
    private static final String PASSWORD_RESET_FAILED = "PASSWORD_RESET_0001";
    private static final String USER_NOT_FOUND_OR_PASSWORD_NOT_MATCH = "User does not exist or Old password does not match";
    public static final String AUTH_DETAILS_NOT_FOUND_FOR_USER = "Auth details not found for user";

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

    /**
     * Around advice for user registration process.
     * Handles creation of authentication details after successful user registration.
     *
     * @param joinPoint The proceeding join point
     * @return Optional<User> containing the registered user if successful
     * @throws Throwable if an error occurs during processing
     */
    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.registerUser()")
    public Object registerUser(final ProceedingJoinPoint joinPoint) throws Throwable {
        UserRegistrationRequest request = (UserRegistrationRequest) joinPoint.getArgs()[0];

        Optional<User> user = (Optional<User>) joinPoint.proceed();

        if (user.isEmpty()) {
            return Optional.empty();
        }

        // Create and save authentication details for the new user
        AuthDetails authDetails = passwordManager.createAuthDetails(request.getPassword());
        authDetails.setUserId(user.get().getId());
        authDetailsService.save(authDetails);

        return user;
    }

    /**
     * Around advice for user login process.
     * Verifies user credentials before proceeding with login.
     *
     * @param joinPoint The proceeding join point
     * @return The result of the login process if credentials are valid
     * @throws Throwable if an error occurs during processing
     */
    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.loginUser()")
    public Object verifyPasswordBeforeLogin(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        String password = (String) args[1];

        Optional<User> user = authService.findByEmail(email);

        if (user.isEmpty() || !verifyUserPassword(user.get().getId(), password)) {

            return Optional.empty();
        }


        return joinPoint.proceed();
    }



    /**
     * Interceptor method that verifies the user's current password before allowing a password change.
     *
     * @param joinPoint The proceeding join point for the intercepted method
     * @return StandardResponse if verification fails, otherwise proceeds with the original method
     * @throws Throwable if an error occurs during processing
     */
    @Around("com.imovel.api.security.aspect.pointcut.PointCuts.changeUserPassword()")
    public Object verifyPasswordBeforeChange(final ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract method arguments
        final Object[] args = joinPoint.getArgs();
        final PasswordChangeRequest passwordChangeRequest = (PasswordChangeRequest) args[0];

        // Retrieve user by email
        final Optional<User> userOptional = authService.findByEmail(passwordChangeRequest.getEmail());

        // Verify user exists and current password is correct
        if (userOptional.isEmpty() || !isPasswordValid(userOptional.get().getId(), passwordChangeRequest.getOldPassword())) {
            return new StandardResponse(
                    PASSWORD_RESET_FAILED,
                    USER_NOT_FOUND_OR_PASSWORD_NOT_MATCH,
                    null
            );
        }

        // Prepare new authentication details
        final User user = userOptional.get();
        final AuthDetails newAuthDetails = passwordManager.createAuthDetails(passwordChangeRequest.getNewPassword());

        // Update existing authentication details
        final AuthDetails currentAuthDetails = authDetailsService.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException(AUTH_DETAILS_NOT_FOUND_FOR_USER));

        currentAuthDetails.setHash(newAuthDetails.getHash());
        currentAuthDetails.setSalt(newAuthDetails.getSalt());

        // Persist updated credentials
        authDetailsService.save(currentAuthDetails);

        // Proceed with original operation
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
        return authDetailsService.findByUserId(userId)
                .map(authDetails -> passwordManager.verifyPassword(
                        password,
                        authDetails.getHash(),
                        authDetails.getSalt()))
                .orElse(false);
    }

}