package com.imovel.api.security.aspect;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.User;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.security.PasswordManager;
import com.imovel.api.services.AuthDetailsService;
import com.imovel.api.services.AuthService;
import com.imovel.api.util.Util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Aspect for handling authentication-related cross-cutting concerns.
 * Provides around advice for user registration and login processes.
 */
@Aspect
@Component
public class AuthServiceAspect {

    private PasswordManager passwordManager;
    private AuthDetailsService authDetailsService;
    private AuthService authService;

    /**
     * Default constructor for AspectJ instantiation.
     */
    public AuthServiceAspect() {
        // Default constructor for AspectJ
    }

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

    @Pointcut("execution(* com.imovel.api.services.TokenService.login(..))")
    public static void loginUser() {
        // Pointcut method - implementation will be provided by AspectJ
    }

//    @Pointcut("execution(* com.imovel.api.services.AuthService.registerUser(..))")
//    public static void registerUser() {
//        // Pointcut method - implementation will be provided by AspectJ
//    }

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
//    @Around("registerUser()")
//    public Object registerUser(final ProceedingJoinPoint joinPoint) throws Throwable {
//        UserRegistrationRequest request = (UserRegistrationRequest) joinPoint.getArgs()[0];
//
//        if (request.getPassword().isBlank()) {
//            return ApplicationResponse.error(ApiCode.INVALID_CREDENTIALS.getCode(),
//                    ApiCode.INVALID_CREDENTIALS.getMessage(),
//                    HttpStatus.BAD_REQUEST);
//        }
//        // Validate email format
//        if (Util.isEmailInvalid(request.getEmail())) {
//
//            return ApplicationResponse.error(ApiCode.INVALID_EMAIL.getCode(),
//                    ApiCode.INVALID_EMAIL.getMessage(),
//                    HttpStatus.BAD_REQUEST);
//        }
//
//        return joinPoint.proceed();
//    }

    /**
     * Around advice for user login process.
     * Logs authentication attempts and provides debugging information.
     *
     * @param joinPoint The proceeding join point
     * @return StandardResponse with login result
     * @throws Throwable if an error occurs during processing
     */
    @Around("loginUser()")
    public Object loginUser(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        UserLoginRequest loginRequest = (UserLoginRequest) args[0];
        String email = loginRequest.getEmail();

        ApiLogger.debug("AuthServiceAspect.loginUser: Starting authentication for email: " + email);

        try {
            Object result = joinPoint.proceed();
            ApiLogger.debug("AuthServiceAspect.loginUser: Login attempt completed for email: " + email);
            return result;
        } catch (Exception e) {
            ApiLogger.error("AuthServiceAspect.loginUser: Exception during login for email: " + email + ", Exception: " + e.getMessage(), e);
            throw e;
        }
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
            ErrorCode errorCode = new ErrorCode(ApiCode.PASSWORD_RESET_FAILED.getCode(), ApiCode.PASSWORD_RESET_FAILED.getMessage(), HttpStatus.BAD_REQUEST);
            return ApplicationResponse.error(errorCode);
        }

        if (!isPasswordValid(optionalUser.get().getId(), passwordChangeRequest.getOldPassword())) {
            ErrorCode errorCode = new ErrorCode(ApiCode.PASSWORD_RESET_FAILED.getCode(), ApiCode.PASSWORD_RESET_FAILED.getMessage(), HttpStatus.BAD_REQUEST);
            return ApplicationResponse.error(errorCode);
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
        ApiLogger.debug("AuthServiceAspect.verifyUserPassword: Verifying password for user ID: " + userId);
        
        ApplicationResponse<AuthDetails> authDetailsResponse = authDetailsService.findByUserId(userId);
        if (!authDetailsResponse.isSuccess()) {
            ApiLogger.debug("AuthServiceAspect.verifyUserPassword: Failed to retrieve auth details for user ID: " + userId + ", response: " + authDetailsResponse.getMessage());
            return false;
        }
        
        AuthDetails authDetails = authDetailsResponse.getData();
        if (authDetails == null) {
            ApiLogger.debug("AuthServiceAspect.verifyUserPassword: Auth details is null for user ID: " + userId);
            return false;
        }
        
        ApiLogger.debug("AuthServiceAspect.verifyUserPassword: Auth details found, verifying password with PasswordManager");
        boolean result = passwordManager.verifyPassword(password, authDetails.getHash(), authDetails.getSalt());
        ApiLogger.debug("AuthServiceAspect.verifyUserPassword: Password verification result: " + result);
        
        return result;
    }


}
