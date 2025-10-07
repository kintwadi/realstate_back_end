package com.imovel.api.booking.service;

import com.imovel.api.booking.model.Booking;
import com.imovel.api.booking.model.BookingPayment;
import com.imovel.api.booking.model.enums.PaymentStatus;
import com.imovel.api.booking.model.enums.PaymentType;
import com.imovel.api.booking.repository.BookingPaymentRepository;
import com.imovel.api.booking.repository.BookingRepository;
import com.imovel.api.booking.request.BookingPaymentRequest;
import com.imovel.api.booking.response.BookingPaymentResponse;
import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.User;
import com.imovel.api.pagination.PaginationResult;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.SessionManager;
import com.imovel.api.services.StripePaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for handling booking payment operations.
 * Manages payment processing, refunds, and payment tracking.
 */
@Service
@Transactional
public class BookingPaymentService {

    private final BookingPaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final StripePaymentService stripePaymentService;
    private final SessionManager sessionManager;

    private static final String SERVICE_NAME = "BookingPaymentService";

    @Autowired
    public BookingPaymentService(BookingPaymentRepository paymentRepository,
                               BookingRepository bookingRepository,
                               Optional<StripePaymentService> stripePaymentService,
                               SessionManager sessionManager) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.stripePaymentService = stripePaymentService.orElse(null);
        this.sessionManager = sessionManager;
    }

    /**
     * Processes a payment for a booking.
     */
    public ApplicationResponse<BookingPaymentResponse> processPayment(BookingPaymentRequest request, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Processing payment for booking: " + request.getBookingId());

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate booking exists
            Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

            // Check permissions (only guest can make payments)
            if (!booking.getGuest().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to make payment for this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate payment amount
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Payment amount must be greater than zero", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check if booking is in a payable state
            if (booking.getStatus().name().equals("CANCELLED") || booking.getStatus().name().equals("COMPLETED")) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Cannot process payment for this booking status", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Create payment record
            BookingPayment payment = new BookingPayment();
            payment.setBooking(booking);
            payment.setAmount(request.getAmount());
            payment.setPaymentType(request.getPaymentType());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setNotes(request.getNotes());

            // Process payment based on method
            try {
                switch (request.getPaymentMethod().toLowerCase()) {
                    case "stripe":
                        processStripePayment(payment, request);
                        break;
                    case "paypal":
                        processPayPalPayment(payment, request);
                        break;
                    case "bank_transfer":
                        processBankTransferPayment(payment, request);
                        break;
                    default:
                        return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Unsupported payment method", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }

                payment = paymentRepository.save(payment);

                ApiLogger.info(SERVICE_NAME, "Successfully processed payment: " + payment.getId());

                return ApplicationResponse.success(convertToPaymentResponse(payment));

            } catch (Exception paymentException) {
                // Mark payment as failed
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setFailureReason(paymentException.getMessage());
                payment = paymentRepository.save(payment);

                ApiLogger.error(SERVICE_NAME, "Payment processing failed: " + paymentException.getMessage());
                return ApplicationResponse.error(
                    ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                    "Payment processing failed: " + paymentException.getMessage(),
                    ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
                );
            }

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Booking not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error processing payment: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to process payment",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Processes a refund for a booking payment by payment ID.
     */
    public ApplicationResponse<BookingPaymentResponse> processRefund(Long paymentId, BigDecimal refundAmount, String reason, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Find the payment
            BookingPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

            Booking booking = payment.getBooking();

            // Check permissions
            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to process refund for this payment", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            return processRefund(booking, refundAmount, reason);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error processing refund: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to process refund",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Processes a refund for a booking payment.
     */
    public ApplicationResponse<BookingPaymentResponse> processRefund(Booking booking, BigDecimal refundAmount, String reason) {
        try {
            ApiLogger.info(SERVICE_NAME, "Processing refund for booking: " + booking.getId());

            // Find the original payment
            List<BookingPayment> successfulPayments = paymentRepository
                .findByBookingIdAndStatus(booking.getId(), PaymentStatus.COMPLETED);

            if (successfulPayments.isEmpty()) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "No successful payments found for refund", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Calculate total paid amount
            BigDecimal totalPaid = successfulPayments.stream()
                .map(BookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Validate refund amount
            if (refundAmount.compareTo(totalPaid) > 0) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Refund amount cannot exceed paid amount", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Create refund payment record
            BookingPayment refundPayment = new BookingPayment();
            refundPayment.setBooking(booking);
            refundPayment.setAmount(refundAmount.negate()); // Negative amount for refund
            refundPayment.setPaymentType(PaymentType.REFUND);
            refundPayment.setPaymentStatus(PaymentStatus.PENDING);
            refundPayment.setNotes(reason);
            refundPayment.setRefundDate(LocalDateTime.now());

            // Process refund through payment gateway
            BookingPayment originalPayment = successfulPayments.get(0); // Use first payment for refund
            try {
                switch (originalPayment.getPaymentMethod().toLowerCase()) {
                    case "stripe":
                        processStripeRefund(refundPayment, originalPayment, refundAmount);
                        break;
                    case "paypal":
                        processPayPalRefund(refundPayment, originalPayment, refundAmount);
                        break;
                    default:
                        // For other methods, mark as manual refund required
                        refundPayment.setPaymentStatus(PaymentStatus.PENDING);
                        refundPayment.setNotes(refundPayment.getNotes() + " - Manual refund required");
                }

                refundPayment = paymentRepository.save(refundPayment);

                ApiLogger.info(SERVICE_NAME, "Successfully processed refund: " + refundPayment.getId());

                return ApplicationResponse.success(convertToPaymentResponse(refundPayment));

            } catch (Exception refundException) {
                refundPayment.setPaymentStatus(PaymentStatus.FAILED);
                refundPayment.setFailureReason(refundException.getMessage());
                refundPayment = paymentRepository.save(refundPayment);

                ApiLogger.error(SERVICE_NAME, "Refund processing failed: " + refundException.getMessage());
                return ApplicationResponse.error(
                    ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                    "Refund processing failed",
                    ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
                );
            }

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error processing refund: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to process refund",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Retrieves payment history for a booking.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<BookingPaymentResponse>> getBookingPayments(Long bookingId, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate booking exists and user has access
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view payments for this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<BookingPayment> payments = paymentRepository.findByBookingId(bookingId);

            payments.sort((p1, p2) -> {
                if (p1.getCreatedAt() == null && p2.getCreatedAt() == null) return 0;
                if (p1.getCreatedAt() == null) return 1;
                if (p2.getCreatedAt() == null) return -1;
                return p2.getCreatedAt().compareTo(p1.getCreatedAt());
            });

            List<BookingPaymentResponse> paymentResponses = payments.stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(paymentResponses);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving booking payments: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to retrieve payments",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Gets a specific payment by ID.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<BookingPaymentResponse> getPaymentById(Long paymentId, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            BookingPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

            Booking booking = payment.getBooking();

            // Check permissions
            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view this payment", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            BookingPaymentResponse response = convertToPaymentResponse(payment);
            return ApplicationResponse.success(response);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving payment: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to retrieve payment",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Gets user payment history with pagination.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<PaginationResult<BookingPaymentResponse>> getUserPaymentHistory(
            Pageable pageable, String status, LocalDateTime startDate, LocalDateTime endDate, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Build query criteria
            List<BookingPayment> allPayments = paymentRepository.findAll();
            
            // Filter by user's bookings
            List<BookingPayment> userPayments = allPayments.stream()
                .filter(payment -> {
                    Booking booking = payment.getBooking();
                    return booking.getGuest().getId().equals(currentUser.getId()) || 
                           booking.getHost().getId().equals(currentUser.getId());
                })
                .filter(payment -> {
                    // Filter by status if provided
                    if (status != null && !status.isEmpty()) {
                        return payment.getPaymentStatus().toString().equalsIgnoreCase(status);
                    }
                    return true;
                })
                .filter(payment -> {
                    // Filter by date range if provided
                    if (startDate != null && payment.getPaymentDate() != null) {
                        if (payment.getPaymentDate().isBefore(startDate)) {
                            return false;
                        }
                    }
                    if (endDate != null && payment.getPaymentDate() != null) {
                        if (payment.getPaymentDate().isAfter(endDate)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((p1, p2) -> {
                    if (p1.getPaymentDate() == null && p2.getPaymentDate() == null) return 0;
                    if (p1.getPaymentDate() == null) return 1;
                    if (p2.getPaymentDate() == null) return -1;
                    return p2.getPaymentDate().compareTo(p1.getPaymentDate()); // Descending order
                })
                .collect(Collectors.toList());

            // Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), userPayments.size());
            List<BookingPayment> pagePayments = userPayments.subList(start, end);

            List<BookingPaymentResponse> paymentResponses = pagePayments.stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());

            PaginationResult<BookingPaymentResponse> result = new PaginationResult<>();
            int pageSize = pageable.getPageSize();
            long totalRecords = userPayments.size();
            int lastPageNumber = (int) Math.ceil((double) Math.max(1, totalRecords) / Math.max(1, pageSize));
            int currentPageNumber = Math.min(pageable.getPageNumber() + 1, Math.max(1, lastPageNumber));
            result.setCurrentPageNumber(currentPageNumber);
            result.setLastPageNumber(lastPageNumber == 0 ? 1 : lastPageNumber);
            result.setPageSize(pageSize);
            result.setTotalRecords(totalRecords);
            result.setRecords(paymentResponses);

            return ApplicationResponse.success(result);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving user payment history: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to retrieve payment history",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Gets payment summary for a booking.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<BookingPaymentResponse> getPaymentSummary(Long bookingId, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view payment summary", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<BookingPayment> payments = paymentRepository.findByBookingId(bookingId);

            // Calculate totals
            BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED && p.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(BookingPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRefunded = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED && p.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(BookingPayment::getAmount)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remainingAmount = booking.getTotalAmount().subtract(totalPaid).add(totalRefunded);

            // Create summary response
            BookingPaymentResponse summary = new BookingPaymentResponse();
            summary.setBookingId(bookingId);
            summary.setAmount(totalPaid);
            summary.setRefundAmount(totalRefunded);
            summary.setStatus(remainingAmount.compareTo(BigDecimal.ZERO) <= 0 ? PaymentStatus.COMPLETED : PaymentStatus.PENDING);

            return ApplicationResponse.success(summary);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving payment summary: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to retrieve payment summary",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    // Private helper methods for payment processing

    private void processStripePayment(BookingPayment payment, BookingPaymentRequest request) throws Exception {
        // Integration with Stripe payment service
        if (stripePaymentService != null) {
            // This would integrate with the existing StripePaymentService
            // For now, we'll simulate the process
            payment.setTransactionId("stripe_" + System.currentTimeMillis());
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
        } else {
            throw new Exception("Stripe payment service not available");
        }
    }

    private void processPayPalPayment(BookingPayment payment, BookingPaymentRequest request) throws Exception {
        // PayPal integration would go here
        payment.setTransactionId("paypal_" + System.currentTimeMillis());
        payment.setPaymentStatus(PaymentStatus.PENDING); // PayPal typically requires confirmation
        payment.setPaymentDate(LocalDateTime.now());
    }

    private void processBankTransferPayment(BookingPayment payment, BookingPaymentRequest request) throws Exception {
        // Bank transfer processing
        payment.setTransactionId("bank_" + System.currentTimeMillis());
        payment.setPaymentStatus(PaymentStatus.PENDING); // Requires manual verification
        payment.setPaymentDate(LocalDateTime.now());
    }

    private void processStripeRefund(BookingPayment refundPayment, BookingPayment originalPayment, BigDecimal refundAmount) throws Exception {
        // Stripe refund processing
        refundPayment.setTransactionId("stripe_refund_" + System.currentTimeMillis());
        // No originalTransactionId field in BookingPayment
        refundPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        refundPayment.setPaymentMethod(originalPayment.getPaymentMethod());
    }

    private void processPayPalRefund(BookingPayment refundPayment, BookingPayment originalPayment, BigDecimal refundAmount) throws Exception {
        // PayPal refund processing
        refundPayment.setTransactionId("paypal_refund_" + System.currentTimeMillis());
        refundPayment.setPaymentStatus(PaymentStatus.PENDING);
        refundPayment.setPaymentMethod(originalPayment.getPaymentMethod());
    }

    private BookingPaymentResponse convertToPaymentResponse(BookingPayment payment) {
        BookingPaymentResponse response = new BookingPaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentType(payment.getPaymentType());
        response.setStatus(payment.getPaymentStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentDate(payment.getPaymentDate());
        response.setRefundDate(payment.getRefundDate());
        response.setRefundAmount(payment.getAmount().compareTo(BigDecimal.ZERO) < 0 ? payment.getAmount().abs() : null);
        response.setFailureReason(payment.getFailureReason());
        response.setNotes(payment.getNotes());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }

    /**
     * Get pending payments for the current user
     */
    public ApplicationResponse<List<BookingPaymentResponse>> getPendingPayments(HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Get all pending payments and filter by user
            List<BookingPayment> allPendingPayments = paymentRepository.findPendingPayments();
            List<BookingPayment> userPendingPayments = allPendingPayments.stream()
                .filter(payment -> payment.getBooking().getGuest().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

            List<BookingPaymentResponse> paymentResponses = userPendingPayments.stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(paymentResponses);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving pending payments: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to retrieve pending payments",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Get overdue payments for the current user
     */
    public ApplicationResponse<List<BookingPaymentResponse>> getOverduePayments(HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Get overdue payments (pending for more than 24 hours)
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            List<BookingPayment> allOverduePayments = paymentRepository.findOverduePayments(cutoffTime);
            List<BookingPayment> userOverduePayments = allOverduePayments.stream()
                .filter(payment -> payment.getBooking().getGuest().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

            List<BookingPaymentResponse> paymentResponses = userOverduePayments.stream()
                .map(this::convertToPaymentResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(paymentResponses);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving overdue payments: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to retrieve overdue payments",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Cancel a pending payment
     */
    public ApplicationResponse<BookingPaymentResponse> cancelPayment(Long paymentId, String reason, HttpSession session) {
        try {
            // Get current user
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            if (currentUser == null) {
                return ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                );
            }

            // Find the payment
            Optional<BookingPayment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ApplicationResponse.error(
                    ApiCode.RESOURCE_NOT_FOUND.getCode(),
                    "Payment not found",
                    ApiCode.RESOURCE_NOT_FOUND.getHttpStatus()
                );
            }

            BookingPayment payment = paymentOpt.get();

            // Verify user has permission to cancel this payment
            if (!payment.getBooking().getGuest().getId().equals(currentUser.getId()) &&
                !payment.getBooking().getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(
                    ApiCode.ACCESS_DENIED.getCode(),
                    "You don't have permission to cancel this payment",
                    ApiCode.ACCESS_DENIED.getHttpStatus()
                );
            }

            // Check if payment can be cancelled
            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                return ApplicationResponse.error(
                    ApiCode.VALIDATION_ERROR.getCode(),
                    "Only pending payments can be cancelled",
                    ApiCode.VALIDATION_ERROR.getHttpStatus()
                );
            }

            // Cancel the payment
            payment.setPaymentStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason(reason);
            payment.setUpdatedAt(LocalDateTime.now());

            BookingPayment savedPayment = paymentRepository.save(payment);

            return ApplicationResponse.success(
                BookingPaymentResponse.fromEntity(savedPayment),
                "Payment cancelled successfully"
            );

        } catch (Exception e) {
            ApiLogger.error("BookingPaymentService.cancelPayment", "Error cancelling payment: " + e.getMessage(), e);
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to cancel payment: " + e.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Verifies the status of a payment
     * @param paymentId The payment ID to verify
     * @param session The HTTP session for authentication
     * @return ApplicationResponse containing the payment status
     */
    public ApplicationResponse<BookingPaymentResponse> verifyPaymentStatus(Long paymentId, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Verifying payment status for payment ID: " + paymentId);

            // Get authenticated user
            User user = sessionManager.getCurrentAuthenticatedUser(session);
            if (user == null) {
                return ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                );
            }

            // Find the payment
            BookingPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

            // Check if user has permission to view this payment
            if (!payment.getBooking().getGuest().getId().equals(user.getId())) {
                return ApplicationResponse.error(
                    ApiCode.ACCESS_DENIED.getCode(),
                    "You don't have permission to view this payment",
                    ApiCode.ACCESS_DENIED.getHttpStatus()
                );
            }

            return ApplicationResponse.success(
                BookingPaymentResponse.fromEntity(payment),
                "Payment status retrieved successfully"
            );

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Payment not found: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.RESOURCE_NOT_FOUND.getCode(),
                e.getMessage(),
                ApiCode.RESOURCE_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error verifying payment status: " + e.getMessage(), e);
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to verify payment status: " + e.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }
}
