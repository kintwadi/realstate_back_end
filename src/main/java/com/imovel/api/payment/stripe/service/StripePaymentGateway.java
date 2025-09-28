package com.imovel.api.payment.stripe.service;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.exception.PaymentProcessingException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.audit.PaymentAuditLogger;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.gateway.PaymentGatewayInterface;
import com.imovel.api.payment.model.Payment;
import com.imovel.api.payment.model.enums.PaymentStatus;
import com.imovel.api.payment.repository.PaymentRepository;
import com.imovel.api.payment.stripe.config.StripeConfig;
import com.imovel.api.response.ApplicationResponse;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.*;

@Service
public class StripePaymentGateway implements PaymentGatewayInterface {

    private final PaymentRepository paymentRepository;
    private final StripeConfig stripeConfig;
    private final WebhookHelper webhookHelper;

    @Value("${stripe.public-key}")
    private String stripePublicKey;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    // ApiLogger is a utility class with static methods, no instantiation needed

    // Supported currencies by Stripe
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "SEK", "NOK", "DKK",
        "PLN", "CZK", "HUF", "BGN", "RON", "HRK", "BRL", "MXN", "SGD", "HKD",
        "INR", "MYR", "THB", "PHP", "IDR", "KRW", "TWD", "NZD", "ZAR"
    );

    // Minimum amounts per currency (in smallest currency unit)
    private static final Map<String, BigDecimal> MINIMUM_AMOUNTS = Map.of(
        "USD", new BigDecimal("0.50"),
        "EUR", new BigDecimal("0.50"),
        "GBP", new BigDecimal("0.30"),
        "CAD", new BigDecimal("0.50"),
        "AUD", new BigDecimal("0.50"),
        "JPY", new BigDecimal("50"),
        "INR", new BigDecimal("0.50")
    );

    @Autowired
    public StripePaymentGateway(PaymentRepository paymentRepository, StripeConfig stripeConfig,WebhookHelper webhookHelper) {
        this.paymentRepository = paymentRepository;
        this.stripeConfig = stripeConfig;
        this.webhookHelper = webhookHelper;
    }

    @PostConstruct
    private void initializeStripe() {
        try {
            if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
                Stripe.apiKey = stripeSecretKey;

                // ✅ CORRECT WAY: Set API version through request options
                if (stripeConfig.getApiVersion() != null && !stripeConfig.getApiVersion().isEmpty()) {
                    // The API version is set per request, but you can validate it's supported
                    ApiLogger.info("Configured Stripe API version: " + stripeConfig.getApiVersion());
                }

                ApiLogger.info("Stripe API initialized successfully with secret key from application.properties");
            } else {
                ApiLogger.info("Stripe secret key not configured in application.properties");
            }
        } catch (Exception e) {
            ApiLogger.error("Failed to initialize Stripe API", e);
        }
    }

    @Override
//    public ApplicationResponse<PaymentResponse> processPayment(Payment payment, Long userId) {
//        try {
//            ApiLogger.info("Processing Stripe payment for amount: " + payment.getAmount());
//
//            // Validate currency support
//            if (!supportsCurrency(payment.getCurrency())) {
//                return ApplicationResponse.error(new ErrorCode(5100L,
//                    "Currency " + payment.getCurrency() + " is not supported by Stripe",
//                    HttpStatus.BAD_REQUEST));
//            }
//
//            // Validate minimum amount
//            if (payment.getAmount().compareTo(getMinimumAmount(payment.getCurrency())) < 0) {
//                return ApplicationResponse.error(new ErrorCode(5101L,
//                    "Amount is below minimum for currency " + payment.getCurrency(),
//                    HttpStatus.BAD_REQUEST));
//            }
//
//            // Create PaymentIntent
//            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                .setAmount(convertToSmallestUnit(payment.getAmount(), payment.getCurrency()))
//                .setCurrency(payment.getCurrency().toLowerCase())
//                .setDescription(payment.getDescription())
//                .putMetadata("user_id", userId.toString())
//                .putMetadata("payment_id", payment.getId().toString())
//                .putMetadata("customer_name", payment.getCustomerName())
//                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
//                .build();
//
//            PaymentIntent paymentIntent = PaymentIntent.create(params);
//
//            // Update payment with Stripe payment intent ID
//            payment.setGatewayPaymentId(paymentIntent.getId());
//            payment.setStatus(PaymentStatus.PROCESSING);
//            payment = paymentRepository.save(payment);
//
//            ApiLogger.info("Stripe PaymentIntent created: " + paymentIntent.getId());
//
//            PaymentResponse response = convertToPaymentResponse(payment);
//            return ApplicationResponse.success(response, "Payment intent created successfully");
//
//        } catch (StripeException e) {
//            ApiLogger.error("Stripe payment failed for payment ID: " + payment.getId(), e);
//            payment.setStatus(PaymentStatus.FAILED);
//            payment.setFailureReason(e.getMessage());
//            paymentRepository.save(payment);
//            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
//        } catch (Exception e) {
//            ApiLogger.error("Unexpected error during payment processing for payment ID: " + payment.getId(), e);
//            payment.setStatus(PaymentStatus.FAILED);
//            payment.setFailureReason("Internal error");
//            paymentRepository.save(payment);
//
//            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(),
//                ApiCode.SYSTEM_ERROR.getMessage(),
//                ApiCode.SYSTEM_ERROR.getHttpStatus()));
//        }
//    }
    public ApplicationResponse<PaymentResponse> processPayment(Payment payment, Long userId) {
        try {
            ApiLogger.info("Processing Stripe payment for amount: " + payment.getAmount());

            // Validate currency support
            if (!supportsCurrency(payment.getCurrency())) {
                return ApplicationResponse.error(new ErrorCode(5100L,
                        "Currency " + payment.getCurrency() + " is not supported by Stripe",
                        HttpStatus.BAD_REQUEST));
            }

            // Validate minimum amount
            if (payment.getAmount().compareTo(getMinimumAmount(payment.getCurrency())) < 0) {
                return ApplicationResponse.error(new ErrorCode(5101L,
                        "Amount is below minimum for currency " + payment.getCurrency(),
                        HttpStatus.BAD_REQUEST));
            }

            // Create PaymentIntent with automatic payment methods (removes confirmation_method)
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(convertToSmallestUnit(payment.getAmount(), payment.getCurrency()))
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .setDescription(payment.getDescription())
                    .putMetadata("user_id", userId.toString())
                    .putMetadata("payment_id", payment.getId().toString())
                    .putMetadata("customer_name", payment.getCustomerName())

                    // Use automatic payment methods with redirects disabled
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )

                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Update payment with Stripe payment intent ID
            payment.setGatewayPaymentId(paymentIntent.getId());
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setClientSecret(paymentIntent.getClientSecret());
            payment = paymentRepository.save(payment);

            ApiLogger.info("Stripe PaymentIntent created: " + paymentIntent.getId() + " with status: " + paymentIntent.getStatus());

            PaymentResponse response = convertToPaymentResponse(payment);
            return ApplicationResponse.success(response, "Payment intent created successfully");

        } catch (StripeException e) {
            ApiLogger.error("Stripe payment failed for payment ID: " + payment.getId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        } catch (Exception e) {
            ApiLogger.error("Unexpected error during payment processing for payment ID: " + payment.getId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Internal error");
            paymentRepository.save(payment);

            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(),
                    ApiCode.SYSTEM_ERROR.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }


    @Override
    public ApplicationResponse<PaymentResponse> processRefund(Payment payment, BigDecimal refundAmount, String reason) {
        try {
            ApiLogger.info("Processing Stripe refund for payment: " + payment.getId() + ", amount: " + refundAmount);

            if (payment.getGatewayPaymentId() == null) {
                return ApplicationResponse.error(new ErrorCode(5103L,
                    "Cannot refund payment without gateway payment ID",
                    HttpStatus.BAD_REQUEST));
            }

            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(payment.getGatewayPaymentId())
                .setAmount(convertToSmallestUnit(refundAmount, payment.getCurrency()))
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("original_payment_id", payment.getId().toString())
                .putMetadata("refund_reason", reason)
                .build();

            Refund refund = Refund.create(params);

            // Update payment status
            if (refundAmount.compareTo(payment.getAmount()) == 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }
            payment = paymentRepository.save(payment);

            ApiLogger.info("Stripe refund created: " + refund.getId());

            PaymentResponse response = convertToPaymentResponse(payment);
            return ApplicationResponse.success(response, "Refund processed successfully");

        } catch (StripeException e) {
            ApiLogger.error("Stripe refund failed for payment ID: " + payment.getId(), e);
            throw new PaymentProcessingException("Refund processing failed: " + e.getMessage());
        } catch (Exception e) {
            ApiLogger.error("Unexpected error during refund processing for payment ID: " + payment.getId(), e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(),
                ApiCode.SYSTEM_ERROR.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }

    @Override
    public ApplicationResponse<PaymentResponse> verifyPaymentStatus(String gatewayPaymentId) {
        try {
            ApiLogger.info("Verifying Stripe payment status: " + gatewayPaymentId);

            PaymentIntent paymentIntent = PaymentIntent.retrieve(gatewayPaymentId);

            Optional<Payment> paymentOpt = paymentRepository.findByGatewayPaymentId(gatewayPaymentId);
            if (paymentOpt.isEmpty()) {
                return ApplicationResponse.error(new ErrorCode(5105L,
                    "Payment not found in database",
                    HttpStatus.NOT_FOUND));
            }

            Payment payment = paymentOpt.get();
            PaymentStatus newStatus = mapStripeStatusToPaymentStatus(paymentIntent.getStatus());

            if (!payment.getStatus().equals(newStatus)) {
                payment.setStatus(newStatus);
                if ("failed".equals(paymentIntent.getStatus()) && paymentIntent.getLastPaymentError() != null) {
                    payment.setFailureReason(paymentIntent.getLastPaymentError().getMessage());
                }
                payment = paymentRepository.save(payment);
                ApiLogger.info("Payment status updated to: " + newStatus);
            }

            PaymentResponse response = convertToPaymentResponse(payment);
            return ApplicationResponse.success(response, "Payment status verified");

        } catch (StripeException e) {
            ApiLogger.error("Stripe payment status verification failed", e);
            return ApplicationResponse.error(new ErrorCode(5106L,
                "Status verification failed: " + e.getMessage(),
                HttpStatus.BAD_GATEWAY));
        } catch (Exception e) {
            ApiLogger.error("Unexpected error during status verification", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(),
                ApiCode.SYSTEM_ERROR.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }

    @Override
    public ApplicationResponse<PaymentResponse> cancelPayment(Payment payment) {
        try {
            ApiLogger.info("Cancelling Stripe payment: " + payment.getId());

            if (payment.getGatewayPaymentId() == null) {
                return ApplicationResponse.error(new ErrorCode(5107L,
                    "Cannot cancel payment without gateway payment ID",
                    HttpStatus.BAD_REQUEST));
            }

            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getGatewayPaymentId());
            paymentIntent = paymentIntent.cancel();

            payment.setStatus(PaymentStatus.CANCELLED);
            payment = paymentRepository.save(payment);

            ApiLogger.info("Stripe payment cancelled: " + paymentIntent.getId());

            PaymentResponse response = convertToPaymentResponse(payment);
            return ApplicationResponse.success(response, "Payment cancelled successfully");

        } catch (StripeException e) {
            ApiLogger.error("Stripe API error during payment cancellation", e);
            return ApplicationResponse.error(new ErrorCode(5108L,
                "Payment cancellation failed: " + e.getMessage(),
                HttpStatus.BAD_GATEWAY));
        } catch (Exception e) {
            ApiLogger.error("Unexpected error during payment cancellation", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(),
                ApiCode.SYSTEM_ERROR.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }

    @Override
    public ApplicationResponse<String> handleWebhook(String webhookPayload, String signature) {
        try {
            if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
                ApiLogger.info("Stripe webhook secret not configured in application.properties");
                return ApplicationResponse.error(new ErrorCode(5109L,
                    "Webhook secret not configured",
                    HttpStatus.INTERNAL_SERVER_ERROR));
            }

            Event event = Webhook.constructEvent(webhookPayload, signature, stripeWebhookSecret);
            ApiLogger.info("Received Stripe webhook event: " + event.getType());

            // Log webhook event details
            PaymentAuditLogger.logWebhookEventReceived("stripe", event.getType(), event.getId());

            // Handle different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    //handlePaymentIntentSucceeded(event);
                    webhookHelper.handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                default:
                    ApiLogger.info("Unhandled webhook event type: " + event.getType());
                    PaymentAuditLogger.logWebhookEventReceived("stripe", event.getType() + " (unhandled)", event.getId());
            }

            return ApplicationResponse.success("Webhook processed successfully");

        } catch (SignatureVerificationException e) {
            ApiLogger.error("Invalid webhook signature", e);
            return ApplicationResponse.error(new ErrorCode(5110L,
                "Invalid webhook signature",
                HttpStatus.UNAUTHORIZED));
        } catch (Exception e) {
            ApiLogger.error("Error processing webhook", e);
            return ApplicationResponse.error(new ErrorCode(ApiCode.SYSTEM_ERROR.getCode(),
                ApiCode.SYSTEM_ERROR.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()));
        }
    }

    @Override
    public String getGatewayName() {
        return "stripe";
    }

    @Override
    public boolean supportsCurrency(String currency) {
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }

    @Override
    public BigDecimal getMinimumAmount(String currency) {
        return MINIMUM_AMOUNTS.getOrDefault(currency.toUpperCase(), new BigDecimal("0.50"));
    }

    // Helper methods
    private Long convertToSmallestUnit(BigDecimal amount, String currency) {
        // For zero-decimal currencies like JPY, don't multiply by 100
        if (isZeroDecimalCurrency(currency)) {
            return amount.longValue();
        }
        return amount.multiply(new BigDecimal("100")).longValue();
    }

    private boolean isZeroDecimalCurrency(String currency) {
        Set<String> zeroDecimalCurrencies = Set.of("JPY", "KRW", "VND", "CLP", "PYG", "RWF", "UGX");
        return zeroDecimalCurrencies.contains(currency.toUpperCase());
    }

    private PaymentStatus mapStripeStatusToPaymentStatus(String stripeStatus) {
        switch (stripeStatus) {
            case "succeeded":
                return PaymentStatus.SUCCEEDED;
            case "processing":
                return PaymentStatus.PROCESSING;
            case "requires_payment_method":
            case "requires_confirmation":
            case "requires_action":
                return PaymentStatus.PENDING;
            case "canceled":
                return PaymentStatus.CANCELLED;
            default:
                return PaymentStatus.FAILED;
        }
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getQuantity(),
            payment.getCustomerName(),
            payment.getGateway(),
            payment.getMethod(),
            payment.getStatus(),
            payment.getGatewayPaymentId(),
                payment.getClientSecret(),
            payment.getDescription(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }

    private void handlePaymentIntentSucceeded(Event event) {
        try {
            ApiLogger.info("=== Starting handlePaymentIntentSucceeded ===");
            ApiLogger.info("Event ID: " + event.getId());
            ApiLogger.info("Event Type: " + event.getType());
            ApiLogger.info("Event API Version: " + event.getApiVersion());

            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                updatePaymentStatus(paymentIntent.getId(), PaymentStatus.SUCCEEDED, null);
            }
        } catch (Exception e) {
            ApiLogger.error("Error handling payment_intent.succeeded webhook", e);
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                String failureReason = paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : "Payment failed";
                updatePaymentStatus(paymentIntent.getId(), PaymentStatus.FAILED, failureReason);
            }
        } catch (Exception e) {
            ApiLogger.error("Error handling payment_intent.payment_failed webhook", e);
        }
    }

    private void handlePaymentIntentCanceled(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                updatePaymentStatus(paymentIntent.getId(), PaymentStatus.CANCELLED, null);
            }
        } catch (Exception e) {
            ApiLogger.error("Error handling payment_intent.canceled webhook", e);
        }
    }

    private void updatePaymentStatus(String gatewayPaymentId, PaymentStatus status, String failureReason) {
        Optional<Payment> paymentOpt = paymentRepository.findByGatewayPaymentId(gatewayPaymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(status);
            if (failureReason != null) {
                payment.setFailureReason(failureReason);
            }
            paymentRepository.save(payment);
            ApiLogger.info("Updated payment status to " + status + " for gateway payment ID: " + gatewayPaymentId);
        } else {
            ApiLogger.info("Payment not found for gateway payment ID: " + gatewayPaymentId);
        }
    }
}