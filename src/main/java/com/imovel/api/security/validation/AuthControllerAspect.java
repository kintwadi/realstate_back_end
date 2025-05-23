package com.imovel.api.security.validation;

import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Aspect
@Component
public class AuthControllerAspect {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Around("execution(* com.imovel.api.controller.AuthController.register(..))")
    public Object validateEmail(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || !(args[0] instanceof UserRegistrationRequest))
        {
            return new ResponseEntity<>(
                    new StandardResponse<>("Invalid request payload", "REGISTRATION_001", null),
                    HttpStatus.BAD_REQUEST);
        }

        UserRegistrationRequest request = (UserRegistrationRequest) args[0];

        if (isValidInput(request)) {
            return new ResponseEntity<>(
                    new StandardResponse<>("Missing required fields", "REGISTRATION_002", null),
                    HttpStatus.BAD_REQUEST);
        }

        if (isValidEmail(request)) {
            return new ResponseEntity<>(
                    new StandardResponse<>("Invalid email format", "REGISTRATION_003", null),
                    HttpStatus.BAD_REQUEST);
        }

        return joinPoint.proceed(); // Continue with method execution if valid
    }

    private static boolean isValidEmail(UserRegistrationRequest request) {
        return !EMAIL_PATTERN.matcher(request.getEmail()).matches();
    }

    private static boolean isValidInput(UserRegistrationRequest request) {
        return request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty() ||
                request.getName() == null || request.getName().isEmpty();
    }
}
