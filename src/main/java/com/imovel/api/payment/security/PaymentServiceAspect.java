package com.imovel.api.payment.security;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.CurrentUser;
import com.imovel.api.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class PaymentServiceAspect {

//    @Autowired
//    private SessionManager sessionManager;
//
//    @Autowired
//    private HttpSession httpSession;
//
////    @Autowired
////    public PaymentServiceAspect(SessionManager sessionManager, HttpSession httpSession) {
////        this.sessionManager = sessionManager;
////        this.httpSession = httpSession;
////    }
//
//    // Pointcut for all PaymentService methods except handleWebhook
//    @Pointcut("execution(* com.imovel.api.payment.service.impl.PaymentServiceImpl.*(..)) && " +
//              "!execution(* com.imovel.api.payment.service.impl.PaymentServiceImpl.handleWebhook(..))")
//    public void paymentServiceMethods() {}
//
//    @Around("paymentServiceMethods()")
//    public Object authorizePaymentAccess(ProceedingJoinPoint joinPoint) throws Throwable {
//        String methodName = joinPoint.getSignature().getName();
//        ApiLogger.info("PaymentServiceAspect: Authorizing access for method: " + methodName);
//
//        try {
//            // Get current authenticated user from session
//            CurrentUser currentUser = sessionManager.getCurrentUser(httpSession);
//
//            // Extract userId from method arguments using reflection for better accuracy
//            Long userIdFromRequest = extractUserIdFromMethodParameters(joinPoint);
//
//            if (userIdFromRequest == null) {
//                ApiLogger.warn("PaymentServiceAspect: User ID not found in method parameters for method: " + methodName);
//                return createErrorResponse(ApiCode.INVALID_REQUEST.getCode(),
//                    "User ID parameter is required",
//                    HttpStatus.BAD_REQUEST);
//            }
//
//            // Validate that the authenticated user matches the requested user
//            if (!currentUser.getUserId().equals(userIdFromRequest)) {
//                ApiLogger.warn("PaymentServiceAspect: User ID mismatch. Authenticated: " +
//                    currentUser.getUserId() + ", Requested: " + userIdFromRequest);
//                return createErrorResponse(ApiCode.ACCESS_DENIED.getCode(),
//                    "Access denied: You can only access your own payment data",
//                    HttpStatus.FORBIDDEN);
//            }
//
//            ApiLogger.info("PaymentServiceAspect: Authorization successful for user: " + currentUser.getUserId());
//
//            // If validation passes, proceed with the method execution
//            return joinPoint.proceed();
//
//        } catch (IllegalStateException e) {
//            ApiLogger.error("PaymentServiceAspect: Authentication error for method " + methodName + ": " + e.getMessage());
//            return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
//                "Authentication required: Invalid or missing session token",
//                HttpStatus.UNAUTHORIZED);
//        } catch (Exception e) {
//            ApiLogger.error("PaymentServiceAspect: Unexpected error during authorization for method " + methodName + ": " + e.getMessage(), e);
//            return createErrorResponse(ApiCode.SYSTEM_ERROR.getCode(),
//                "Authorization validation failed",
//                HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * Extract userId from method parameters using reflection
//     * More robust approach that checks parameter names and types
//     */
//    private Long extractUserIdFromMethodParameters(ProceedingJoinPoint joinPoint) {
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Method method = signature.getMethod();
//        Parameter[] parameters = method.getParameters();
//        Object[] args = joinPoint.getArgs();
//
//        // Look for userId parameter by name and type
//        for (int i = 0; i < parameters.length; i++) {
//            Parameter parameter = parameters[i];
//            Object arg = args[i];
//
//            // Check if parameter is named "userId" and is of type Long
//            if (("userId".equals(parameter.getName()) || "id".equals(parameter.getName()))
//                && arg instanceof Long) {
//                return (Long) arg;
//            }
//        }
//        // Fallback: look for any Long parameter (for backward compatibility)
//        for (Object arg : args) {
//            if (arg instanceof Long) {
//                return (Long) arg;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Create standardized error response
//     */
//    private ApplicationResponse<?> createErrorResponse(int code, String message, HttpStatus status) {
//        ErrorCode errorCode = new ErrorCode(code, message, status);
//        return ApplicationResponse.error(errorCode);
//    }
}