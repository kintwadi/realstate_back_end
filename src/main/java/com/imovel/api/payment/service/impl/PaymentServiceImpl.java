package com.imovel.api.payment.service.impl;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.audit.PaymentAuditLogger;
import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.payment.monitoring.PaymentMonitoringService;
import io.micrometer.core.instrument.Timer;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.factory.PaymentGatewayFactory;
import com.imovel.api.payment.gateway.PaymentGatewayInterface;
import com.imovel.api.payment.model.Payment;
import com.imovel.api.payment.model.enums.PaymentStatus;
import com.imovel.api.payment.repository.PaymentRepository;
import com.imovel.api.payment.service.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.imovel.api.pagination.Pagination;
import com.imovel.api.pagination.PaginationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PaymentMonitoringService monitoringService;
    // ApiLogger is a utility class with static methods, no instantiation needed
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                             PaymentGatewayFactory paymentGatewayFactory,
                             PaymentMonitoringService monitoringService) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.monitoringService = monitoringService;
    }
    
    @Override
    public ApplicationResponse<PaymentResponse> processPayment(PaymentRequest paymentRequest, Long userId) {
        Timer.Sample timerSample = monitoringService.startTimer();
        String gateway = paymentRequest.getGateway().toString();
        
        try {
            ApiLogger.info("Processing payment for user: " + userId + ", gateway: " + paymentRequest.getGateway());
            
            // Log payment initiation
            PaymentAuditLogger.logPaymentInitiated(userId, paymentRequest.getAmount(), 
                paymentRequest.getCurrency(), paymentRequest.getGateway().toString());
            
            // Validate payment request
            ApplicationResponse<String> validationResult = validatePaymentRequest(paymentRequest);
            if (!validationResult.isSuccess()) {
                PaymentAuditLogger.logPaymentValidationFailed(userId, validationResult.getError().getMessage());
                return ApplicationResponse.error(validationResult.getError());
            }
            
            // Get payment gateway
            PaymentGatewayInterface paymentGateway = paymentGatewayFactory.getPaymentGateway(paymentRequest.getGateway());
            if (paymentGateway == null) {
                PaymentAuditLogger.logPaymentValidationFailed(userId, "Unsupported payment gateway: " + paymentRequest.getGateway());
                return ApplicationResponse.error(new ErrorCode(5200L,
                    "Unsupported payment gateway: " + paymentRequest.getGateway(),
                    HttpStatus.BAD_REQUEST));
            }
            
            // Create payment entity
            Payment payment = new Payment(
                userId,
                paymentRequest.getAmount(),
                paymentRequest.getCurrency(),
                paymentRequest.getQuantity(),
                paymentRequest.getName(),
                paymentRequest.getGatewayEnum(),
                paymentRequest.getMethodEnum()
            );
            payment.setDescription(paymentRequest.getDescription());
            
            // Save payment to database
            payment = paymentRepository.save(payment);
            
            // Log payment created
            PaymentAuditLogger.logPaymentCreated(payment.getId(), userId, payment.getAmount(), 
                payment.getCurrency(), payment.getGateway().toString());
            
            // Process payment through gateway
            ApplicationResponse<PaymentResponse> result = paymentGateway.processPayment(payment, userId);
            
            if (!result.isSuccess()) {
                // Update payment status to failed if gateway processing failed
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason(result.getError().getMessage());
                paymentRepository.save(payment);
                
                // Log payment failure
                PaymentAuditLogger.logPaymentFailed(payment.getId(), userId, result.getError().getMessage());
                
                // Record monitoring metrics
                monitoringService.stopPaymentTimer(timerSample, gateway, false);
                monitoringService.recordPaymentFailure(gateway, result.getError().getMessage(), 
                    paymentRequest.getAmount());
            } else {
                // Log payment success
                PaymentAuditLogger.logPaymentCompleted(payment.getId(), userId, payment.getAmount(), 
                    payment.getCurrency(), result.getData().getGatewayPaymentId());
                
                // Record monitoring metrics
                monitoringService.stopPaymentTimer(timerSample, gateway, true);
                monitoringService.recordPaymentSuccess(gateway, paymentRequest.getAmount(), 
                    paymentRequest.getCurrency());
            }
            
            return result;
            
        } catch (Exception e) {
            ApiLogger.error("Error processing payment: " + e.getMessage(), e);
            PaymentAuditLogger.logPaymentError(userId, e.getMessage());
            
            // Record monitoring metrics for exception
            monitoringService.stopPaymentTimer(timerSample, gateway, false);
            monitoringService.recordPaymentFailure(gateway, "system_error", paymentRequest.getAmount());
            
            return ApplicationResponse.error(new ErrorCode(ApiCode.PAYMENT_GATEWAY_ERROR.getCode(), 
                ApiCode.PAYMENT_GATEWAY_ERROR.getMessage(), ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse<PaymentResponse> getPaymentById(Long paymentId, Long userId) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ApplicationResponse.error(new ErrorCode(5201L,
                    "Payment not found",
                    HttpStatus.NOT_FOUND));
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if user has access to this payment
            if (!payment.getUserId().equals(userId)) {
                return ApplicationResponse.error(new ErrorCode(5202L,
                    "Access denied to payment",
                    HttpStatus.FORBIDDEN));
            }
            
            PaymentResponse response = convertToPaymentResponse(payment);
            return ApplicationResponse.success(response);
            
        } catch (Exception e) {
            ApiLogger.error("Error retrieving payment by ID", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse<PaginationResult<PaymentResponse>> getUserPayments(Long userId, Pagination pagination, String sortBy, String sortDirection) {
        try {
            // Build the base query
            String baseQuery = "SELECT p FROM Payment p WHERE p.userId = :userId";
            String countQuery = "SELECT COUNT(p) FROM Payment p WHERE p.userId = :userId";
            
            // Add sorting
            if (sortBy != null && !sortBy.isEmpty()) {
                String direction = "desc".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
                baseQuery += " ORDER BY p." + sortBy + " " + direction;
            }
            
            // Get total count
            TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
            countTypedQuery.setParameter("userId", userId);
            Long totalRecords = countTypedQuery.getSingleResult();
            
            // Get paginated results
            TypedQuery<Payment> query = entityManager.createQuery(baseQuery, Payment.class);
            query.setParameter("userId", userId);
            query.setFirstResult((pagination.getPageNumber() - 1) * pagination.getPageSize());
            query.setMaxResults(pagination.getPageSize());
            
            List<Payment> payments = query.getResultList();
            List<PaymentResponse> paymentResponses = payments.stream()
                    .map(this::convertToPaymentResponse)
                    .toList();
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalRecords / pagination.getPageSize());
            
            PaginationResult<PaymentResponse> result = new PaginationResult<>();
            result.setCurrentPageNumber(pagination.getPageNumber());
            result.setLastPageNumber(totalPages);
            result.setPageSize(pagination.getPageSize());
            result.setTotalRecords(totalRecords);
            result.setRecords(paymentResponses);
            
            return ApplicationResponse.success(result);
            
        } catch (Exception e) {
            ApiLogger.error("Error retrieving user payments", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    public ApplicationResponse<PaymentResponse> processRefund(Long paymentId, BigDecimal refundAmount, 
                                                            String reason, Long userId) {
        Timer.Sample timerSample = monitoringService.startTimer();
        String gateway = null;
        
        try {
            ApiLogger.info("Processing refund for payment: " + paymentId + ", amount: " + refundAmount);
            
            // Log refund initiation
            PaymentAuditLogger.logRefundInitiated(paymentId, userId, refundAmount, reason);
            
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                PaymentAuditLogger.logRefundFailed(paymentId, userId, "Payment not found");
                return ApplicationResponse.error(new ErrorCode(5203L,
                    "Payment not found",
                    HttpStatus.NOT_FOUND));
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if user has access to this payment
            if (!payment.getUserId().equals(userId)) {
                PaymentAuditLogger.logRefundFailed(paymentId, userId, "Access denied to payment");
                return ApplicationResponse.error(new ErrorCode(5204L,
                    "Access denied to payment",
                    HttpStatus.FORBIDDEN));
            }
            
            // Validate refund amount
            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                PaymentAuditLogger.logRefundFailed(paymentId, userId, "Refund amount exceeds original payment amount");
                return ApplicationResponse.error(new ErrorCode(5205L,
                    "Refund amount cannot exceed original payment amount",
                    HttpStatus.BAD_REQUEST));
            }
            
            // Check if payment can be refunded
            if (!canBeRefunded(payment.getStatus())) {
                PaymentAuditLogger.logRefundFailed(paymentId, userId, "Payment cannot be refunded in current status: " + payment.getStatus());
                return ApplicationResponse.error(new ErrorCode(5206L,
                    "Payment cannot be refunded in current status: " + payment.getStatus(),
                    HttpStatus.BAD_REQUEST));
            }
            
            // Get payment gateway and process refund
            PaymentGatewayInterface gatewayInterface = paymentGatewayFactory.getPaymentGateway(payment.getGateway().getValue());
            gateway = payment.getGateway().getValue();
            ApplicationResponse<PaymentResponse> result = gatewayInterface.processRefund(payment, refundAmount, reason);
            
            if (result.isSuccess()) {
                PaymentAuditLogger.logRefundCompleted(paymentId, userId, refundAmount, 
                    result.getData().getGatewayPaymentId());
                
                // Record monitoring metrics
                monitoringService.stopRefundTimer(timerSample, gateway, true);
                monitoringService.recordPaymentRefund(gateway, refundAmount, payment.getCurrency());
            } else {
                PaymentAuditLogger.logRefundFailed(paymentId, userId, result.getError().getMessage());
                
                // Record monitoring metrics
                monitoringService.stopRefundTimer(timerSample, gateway, false);
            }
            
            return result;
            
        } catch (Exception e) {
            ApiLogger.error("Error processing refund", e);
            PaymentAuditLogger.logRefundFailed(paymentId, userId, e.getMessage());
            
            // Record monitoring metrics for exception
            if (gateway != null) {
                monitoringService.stopRefundTimer(timerSample, gateway, false);
            }
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    public ApplicationResponse<PaymentResponse> cancelPayment(Long paymentId, Long userId) {
        try {
            ApiLogger.info("Cancelling payment: " + paymentId);
            
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ApplicationResponse.error(new ErrorCode(5207L,
                    "Payment not found",
                    HttpStatus.NOT_FOUND));
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if user has access to this payment
            if (!payment.getUserId().equals(userId)) {
                return ApplicationResponse.error(new ErrorCode(5208L,
                    "Access denied to payment",
                    HttpStatus.FORBIDDEN));
            }
            
            // Check if payment can be cancelled
            if (!canBeCancelled(payment.getStatus())) {
                return ApplicationResponse.error(new ErrorCode(5209L,
                    "Payment cannot be cancelled in current status: " + payment.getStatus(),
                    HttpStatus.BAD_REQUEST));
            }
            
            // Get payment gateway and cancel payment
            PaymentGatewayInterface gateway = paymentGatewayFactory.getPaymentGateway(payment.getGateway().getValue());
            return gateway.cancelPayment(payment);
            
        } catch (Exception e) {
            ApiLogger.error("Error cancelling payment", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    public ApplicationResponse<PaymentResponse> verifyPaymentStatus(Long paymentId, Long userId) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ApplicationResponse.error(new ErrorCode(5210L,
                    "Payment not found",
                    HttpStatus.NOT_FOUND));
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if user has access to this payment
            if (!payment.getUserId().equals(userId)) {
                return ApplicationResponse.error(new ErrorCode(5211L,
                    "Access denied to payment",
                    HttpStatus.FORBIDDEN));
            }
            
            if (payment.getGatewayPaymentId() == null) {
                return ApplicationResponse.error(new ErrorCode(5212L,
                    "Payment has no gateway payment ID",
                    HttpStatus.BAD_REQUEST));
            }
            
            // Get payment gateway and verify status
            PaymentGatewayInterface gateway = paymentGatewayFactory.getPaymentGateway(payment.getGateway().getValue());
            return gateway.verifyPaymentStatus(payment.getGatewayPaymentId());
            
        } catch (Exception e) {
            ApiLogger.error("Error verifying payment status", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse<PaymentStatistics> getPaymentStatistics(Long userId, LocalDateTime startDate, 
                                                                      LocalDateTime endDate) {
        try {
            List<Payment> payments = paymentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
            
            PaymentStatistics stats = calculateStatistics(payments);
            return ApplicationResponse.success(stats);
            
        } catch (Exception e) {
            ApiLogger.error("Error calculating payment statistics", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    @Override
    public ApplicationResponse<String> handleWebhook(String gateway, String webhookPayload, String signature) {
        try {
            ApiLogger.info("Handling webhook for gateway: " + gateway);
            
            PaymentGatewayInterface paymentGateway = paymentGatewayFactory.getPaymentGateway(gateway);
            if (paymentGateway == null) {
                return ApplicationResponse.error(new ErrorCode(5213L,
                    "Unsupported payment gateway for webhook: " + gateway,
                    HttpStatus.BAD_REQUEST));
            }
            
            return paymentGateway.handleWebhook(webhookPayload, signature);
            
        } catch (Exception e) {
            ApiLogger.error("Error handling webhook", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(), 
                ApiCode.SYSTEM_ERROR.getMessage(), ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }
    
    // Helper methods
    private ApplicationResponse<String> validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ApplicationResponse.error(new ErrorCode(5214L,
                "Invalid payment amount",
                HttpStatus.BAD_REQUEST));
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApplicationResponse.error(new ErrorCode(5215L,
                "Customer name is required",
                HttpStatus.BAD_REQUEST));
        }
        
        if (request.getCurrency() == null || request.getCurrency().length() != 3) {
            return ApplicationResponse.error(new ErrorCode(5216L,
                "Invalid currency code",
                HttpStatus.BAD_REQUEST));
        }
        
        if (request.getGateway() == null || request.getGateway().trim().isEmpty()) {
            return ApplicationResponse.error(new ErrorCode(5217L,
                "Payment gateway is required",
                HttpStatus.BAD_REQUEST));
        }
        
        if (request.getMethod() == null || request.getMethod().trim().isEmpty()) {
            return ApplicationResponse.error(new ErrorCode(5218L,
                "Payment method is required",
                HttpStatus.BAD_REQUEST));
        }
        
        return ApplicationResponse.success("Validation passed");
    }
    
    private boolean canBeRefunded(PaymentStatus status) {
        return status == PaymentStatus.SUCCEEDED || status == PaymentStatus.PARTIALLY_REFUNDED;
    }
    
    private boolean canBeCancelled(PaymentStatus status) {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }
    
    private PaymentResponse convertToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setQuantity(payment.getQuantity());
        response.setCustomerName(payment.getCustomerName());
        response.setGateway(payment.getGateway());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setGatewayPaymentId(payment.getGatewayPaymentId());
        response.setDescription(payment.getDescription());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
    
    private PaymentStatistics calculateStatistics(List<Payment> payments) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal successfulAmount = BigDecimal.ZERO;
        BigDecimal failedAmount = BigDecimal.ZERO;
        BigDecimal refundedAmount = BigDecimal.ZERO;
        
        long totalCount = payments.size();
        long successfulCount = 0;
        long failedCount = 0;
        long refundedCount = 0;
        
        for (Payment payment : payments) {
            totalAmount = totalAmount.add(payment.getAmount());
            
            switch (payment.getStatus()) {
                case SUCCEEDED:
                    successfulAmount = successfulAmount.add(payment.getAmount());
                    successfulCount++;
                    break;
                case FAILED:
                    failedAmount = failedAmount.add(payment.getAmount());
                    failedCount++;
                    break;
                case REFUNDED:
                case PARTIALLY_REFUNDED:
                    refundedAmount = refundedAmount.add(payment.getAmount());
                    refundedCount++;
                    break;
            }
        }
        
        return new PaymentStatistics(totalAmount, successfulAmount, failedAmount, refundedAmount,
                                   totalCount, successfulCount, failedCount, refundedCount);
    }
}