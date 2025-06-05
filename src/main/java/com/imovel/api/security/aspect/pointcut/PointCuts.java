package com.imovel.api.security.aspect.pointcut;

import org.aspectj.lang.annotation.Pointcut;

/**
 * Defines pointcuts for AOP (Aspect-Oriented Programming) advice targeting specific methods
 * in the application. These pointcuts are used to apply cross-cutting concerns like validation,
 * logging, or security checks at specific join points in the application flow.
 *
 * <p>Each pointcut defines a specific method or set of methods where advice should be applied.</p>
 */
public class PointCuts {

   @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

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

}