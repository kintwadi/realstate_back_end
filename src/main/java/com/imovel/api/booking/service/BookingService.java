package com.imovel.api.booking.service;

import com.imovel.api.booking.model.*;
import com.imovel.api.booking.model.enums.BookingStatus;
import com.imovel.api.booking.repository.*;
import com.imovel.api.booking.request.*;
import com.imovel.api.booking.response.*;
import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Property;
import com.imovel.api.model.User;
import com.imovel.api.repository.PropertyRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.SessionManager;
import com.imovel.api.pagination.PaginationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for handling booking operations.
 * Manages booking creation, updates, cancellation, and retrieval with proper business logic.
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyAvailabilityService availabilityService;
    private final BookingPaymentService paymentService;
    private final CancellationPolicyService cancellationPolicyService;
    private final SessionManager sessionManager;

    private static final String SERVICE_NAME = "BookingService";

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                         PropertyRepository propertyRepository,
                         UserRepository userRepository,
                         PropertyAvailabilityService availabilityService,
                         BookingPaymentService paymentService,
                         CancellationPolicyService cancellationPolicyService,
                         SessionManager sessionManager) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.availabilityService = availabilityService;
        this.paymentService = paymentService;
        this.cancellationPolicyService = cancellationPolicyService;
        this.sessionManager = sessionManager;
    }

    /**
     * Creates a new booking with validation and availability checking.
     */
    public ApplicationResponse<BookingResponse> createBooking(BookingCreateRequest request, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Creating new booking for property: " + request.getPropertyId());

            // Get current user
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate property exists
            Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

            // Validate guest exists
            User guest = userRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getGuestId()));

            // Check if user can book this property (not their own)
            if (property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Cannot book your own property", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check availability
            AvailabilityCheckRequest availabilityRequest = new AvailabilityCheckRequest();
            availabilityRequest.setPropertyId(request.getPropertyId());
            availabilityRequest.setCheckInDate(request.getCheckInDate());
            availabilityRequest.setCheckOutDate(request.getCheckOutDate());
            availabilityRequest.setNumberOfAdults(request.getNumberOfAdults());
            availabilityRequest.setNumberOfChildren(request.getNumberOfChildren());

            ApplicationResponse<AvailabilityCheckResponse> availabilityResponse = 
                availabilityService.checkAvailability(availabilityRequest);

            if (!availabilityResponse.isSuccess() || !availabilityResponse.getData().isBookable()) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Property is not available for the selected dates", 
                    ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check for overlapping bookings
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.getPropertyId(), request.getCheckInDate(), request.getCheckOutDate());

            if (!overlappingBookings.isEmpty()) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Property has conflicting bookings for the selected dates", 
                    ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Create booking entity
            Booking booking = new Booking();
            booking.setProperty(property);
            booking.setGuest(guest);
            booking.setHost(property.getCreatedBy());
            booking.setCheckInDate(request.getCheckInDate());
            booking.setCheckOutDate(request.getCheckOutDate());
            booking.setNumberOfAdults(request.getNumberOfAdults());
            booking.setNumberOfChildren(request.getNumberOfChildren());
            booking.setSpecialRequests(request.getSpecialRequests());
            booking.setNotes(request.getNotes());
            booking.setConfirmationCode(generateConfirmationCode());

            // Calculate pricing
            AvailabilityCheckResponse availability = availabilityResponse.getData();
            booking.setTotalAmount(availability.getTotalPrice());
            booking.setNightlyRate(availability.getAverageNightlyRate());

            // Set initial status based on instant booking
            if (availability.getIsInstantBookable()) {
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setConfirmedAt(LocalDateTime.now());
            } else {
                booking.setStatus(BookingStatus.PENDING);
            }

            // Save booking
            booking = bookingRepository.save(booking);

            // Create guest records
            if (request.getAdditionalGuests() != null && !request.getAdditionalGuests().isEmpty()) {
                createBookingGuests(booking, request.getAdditionalGuests());
            }

            // Block availability dates
            availabilityService.blockDatesForBooking(booking);

            ApiLogger.info(SERVICE_NAME, "Successfully created booking: " + booking.getId());

            return ApplicationResponse.success(convertToBookingResponse(booking));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Resource not found during booking creation: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error creating booking: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to create booking", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Updates an existing booking.
     */
    public ApplicationResponse<BookingResponse> updateBooking(Long bookingId, BookingUpdateRequest request, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Updating booking: " + bookingId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            // Check permissions (guest or host can update)
            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to update this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate booking can be updated
            if (booking.getStatus() == BookingStatus.CANCELLED || 
                booking.getStatus() == BookingStatus.COMPLETED) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Cannot update cancelled or completed booking", 
                    ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Update fields if provided
            if (request.getCheckInDate() != null) {
                booking.setCheckInDate(request.getCheckInDate());
            }
            if (request.getCheckOutDate() != null) {
                booking.setCheckOutDate(request.getCheckOutDate());
            }
            if (request.getNumberOfAdults() != null) {
                booking.setNumberOfAdults(request.getNumberOfAdults());
            }
            if (request.getNumberOfChildren() != null) {
                booking.setNumberOfChildren(request.getNumberOfChildren());
            }
            if (request.getSpecialRequests() != null) {
                booking.setSpecialRequests(request.getSpecialRequests());
            }
            if (request.getStatus() != null && booking.getHost().getId().equals(currentUser.getId())) {
                booking.setStatus(request.getStatus());
                if (request.getStatus() == BookingStatus.CONFIRMED) {
                    booking.setConfirmedAt(LocalDateTime.now());
                }
            }
            if (request.getCancellationReason() != null && 
                request.getStatus() == BookingStatus.CANCELLED) {
                booking.setCancellationReason(request.getCancellationReason());
                booking.setCancelledAt(LocalDateTime.now());
            }
            if (request.getHostNotes() != null && booking.getHost().getId().equals(currentUser.getId())) {
                booking.setHostNotes(request.getHostNotes());
            }

            booking = bookingRepository.save(booking);

            ApiLogger.info(SERVICE_NAME, "Successfully updated booking: " + bookingId);

            return ApplicationResponse.success(convertToBookingResponse(booking));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Booking not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error updating booking: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to update booking", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Cancels a booking with refund calculation.
     */
    public ApplicationResponse<BookingResponse> cancelBooking(Long bookingId, String reason, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Cancelling booking: " + bookingId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            // Check permissions
            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to cancel this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate booking can be cancelled
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Booking is already cancelled", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            if (booking.getStatus() == BookingStatus.COMPLETED) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Cannot cancel completed booking", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Calculate refund if applicable
            ApplicationResponse<BigDecimal> refundResponse = 
                cancellationPolicyService.calculateRefund(booking, LocalDate.now());

            if (refundResponse.isSuccess() && refundResponse.getData().compareTo(BigDecimal.ZERO) > 0) {
                // Process refund
                paymentService.processRefund(booking, refundResponse.getData(), reason);
            }

            // Update booking status
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason(reason);
            booking.setCancelledAt(LocalDateTime.now());

            // Release availability
            availabilityService.releaseDatesForBooking(booking);

            booking = bookingRepository.save(booking);

            ApiLogger.info(SERVICE_NAME, "Successfully cancelled booking: " + bookingId);

            return ApplicationResponse.success(convertToBookingResponse(booking));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Booking not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error cancelling booking: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to cancel booking", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Retrieves a booking by ID.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<BookingResponse> getBookingById(Long bookingId, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            // Check permissions
            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            return ApplicationResponse.success(convertToBookingResponse(booking));

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving booking: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve booking", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Confirm a booking (host action)
     */
    public ApplicationResponse<BookingResponse> confirmBooking(Long bookingId, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Confirming booking: " + bookingId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            // Only host can confirm
            if (!booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to confirm this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate status
            if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Cannot confirm cancelled or completed booking", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                return ApplicationResponse.success(convertToBookingResponse(booking), "Booking already confirmed");
            }

            // Confirm booking
            booking.confirm();
            booking.setConfirmationCode(booking.getConfirmationCode() != null ? booking.getConfirmationCode() : generateConfirmationCode());

            // Block availability (if not already blocked)
            availabilityService.blockDatesForBooking(booking);

            booking = bookingRepository.save(booking);

            ApiLogger.info(SERVICE_NAME, "Successfully confirmed booking: " + bookingId);

            return ApplicationResponse.success(convertToBookingResponse(booking));
        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Booking not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error confirming booking: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to confirm booking", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Retrieves bookings for the current user (as guest or host).
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<Page<BookingResponse>> getUserBookings(String role, BookingStatus status, 
                                                                     Pageable pageable, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            List<Booking> bookingList;

            if ("guest".equalsIgnoreCase(role)) {
                if (status != null) {
                    bookingList = bookingRepository.findByGuestIdAndStatus(currentUser.getId(), status);
                } else {
                    bookingList = bookingRepository.findByGuestId(currentUser.getId());
                }
            } else if ("host".equalsIgnoreCase(role)) {
                if (status != null) {
                    bookingList = bookingRepository.findByHostIdAndStatus(currentUser.getId(), status);
                } else {
                    bookingList = bookingRepository.findByHostId(currentUser.getId());
                }
            } else {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Invalid role. Use 'guest' or 'host'", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Convert list to page manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), bookingList.size());
            List<Booking> pageContent = bookingList.subList(start, end);
            Page<Booking> bookings = new PageImpl<>(pageContent, pageable, bookingList.size());

            Page<BookingResponse> bookingResponses = bookings.map(this::convertToBookingResponse);

            return ApplicationResponse.success(bookingResponses);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving user bookings: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve bookings", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets upcoming check-ins for the current user's properties.
     */
    public ApplicationResponse<List<BookingResponse>> getUpcomingCheckIns(int days, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting upcoming check-ins for next " + days + " days");

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(days);
            
            // Get bookings where check-in date is within the specified range
            // and the current user is the host
            List<Booking> upcomingCheckIns = bookingRepository.findUpcomingBookings(startDate, endDate).stream()
                .filter(b -> b.getHost().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
            
            List<BookingResponse> responses = upcomingCheckIns.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
            
            return ApplicationResponse.success(responses);
            
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting upcoming check-ins: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                "Failed to get upcoming check-ins", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets upcoming check-outs for the current user's properties.
     */
    public ApplicationResponse<List<BookingResponse>> getUpcomingCheckOuts(int days, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting upcoming check-outs for next " + days + " days");

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(days);
            
            // Get bookings where check-out date is within the specified range
            // and the current user is the host
            List<Booking> upcomingCheckOuts = bookingRepository.findByHostId(currentUser.getId()).stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN && !b.getCheckOutDate().isBefore(startDate) && !b.getCheckOutDate().isAfter(endDate))
                .collect(Collectors.toList());
            
            List<BookingResponse> responses = upcomingCheckOuts.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
            
            return ApplicationResponse.success(responses);
            
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting upcoming check-outs: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                "Failed to get upcoming check-outs", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets booking statistics for a property within a date range.
     */
    public ApplicationResponse<BookingStatistics> getPropertyBookingStatistics(Long propertyId, 
                                                                              LocalDate startDate, 
                                                                              LocalDate endDate, 
                                                                              HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting booking statistics for property: " + propertyId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            
            // Validate property exists and user has access
            Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
            
            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), 
                    "Not authorized to view statistics for this property", 
                    ApiCode.PERMISSION_DENIED.getHttpStatus());
            }
            
            // Set default date range if not provided
            LocalDate effectiveStart = (startDate != null) ? startDate : LocalDate.now().minusMonths(12);
            LocalDate effectiveEnd = (endDate != null) ? endDate : LocalDate.now();
            
            // Get booking statistics
            List<Booking> bookings = bookingRepository.findByPropertyId(propertyId).stream()
                .filter(b -> !b.getCheckInDate().isBefore(effectiveStart) && !b.getCheckOutDate().isAfter(effectiveEnd))
                .collect(Collectors.toList());
            
            BookingStatistics stats = new BookingStatistics();
            stats.setPropertyId(propertyId);
            stats.setStartDate(effectiveStart);
            stats.setEndDate(effectiveEnd);
            stats.setTotalBookings(bookings.size());
            
            // Calculate statistics
            long confirmedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();
            long cancelledBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();
            long pendingBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();
            
            BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.setConfirmedBookings((int) confirmedBookings);
            stats.setCancelledBookings((int) cancelledBookings);
            stats.setPendingBookings((int) pendingBookings);
            stats.setTotalRevenue(totalRevenue);
            stats.setAverageBookingValue(
                confirmedBookings > 0 ? totalRevenue.divide(BigDecimal.valueOf(confirmedBookings), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO
            );
            
            return ApplicationResponse.success(stats);
            
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting booking statistics: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                "Failed to get booking statistics", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Inner class for booking statistics
     */
    public static class BookingStatistics {
        private Long propertyId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalBookings;
        private Integer confirmedBookings;
        private Integer cancelledBookings;
        private Integer pendingBookings;
        private BigDecimal totalRevenue;
        private BigDecimal averageBookingValue;

        // Getters and Setters
        public Long getPropertyId() { return propertyId; }
        public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public Integer getTotalBookings() { return totalBookings; }
        public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }

        public Integer getConfirmedBookings() { return confirmedBookings; }
        public void setConfirmedBookings(Integer confirmedBookings) { this.confirmedBookings = confirmedBookings; }

        public Integer getCancelledBookings() { return cancelledBookings; }
        public void setCancelledBookings(Integer cancelledBookings) { this.cancelledBookings = cancelledBookings; }

        public Integer getPendingBookings() { return pendingBookings; }
        public void setPendingBookings(Integer pendingBookings) { this.pendingBookings = pendingBookings; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public BigDecimal getAverageBookingValue() { return averageBookingValue; }
        public void setAverageBookingValue(BigDecimal averageBookingValue) { this.averageBookingValue = averageBookingValue; }
    }

    // Helper methods

    private String generateConfirmationCode() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createBookingGuests(Booking booking, List<BookingGuestRequest> guestRequests) {
        // This would be implemented with BookingGuestService
        // For now, we'll leave this as a placeholder
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setPropertyId(booking.getProperty().getId());
        response.setGuestId(booking.getGuest().getId());
        response.setHostId(booking.getHost().getId());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setNumberOfAdults(booking.getNumberOfAdults());
        response.setNumberOfChildren(booking.getNumberOfChildren());
        response.setTotalAmount(booking.getTotalAmount());
        response.setNightlyRate(booking.getNightlyRate());
        response.setStatus(booking.getStatus());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setCancellationReason(booking.getCancellationReason());
        response.setHostNotes(booking.getHostNotes());
        response.setConfirmationCode(booking.getConfirmationCode());
        response.setConfirmedAt(booking.getConfirmedAt());
        response.setCancellationDate(booking.getCancelledAt());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        return response;
    }

    /**
     * Retrieves current user's bookings (guest role) with optional status filter and pagination
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<PaginationResult<BookingResponse>> getUserBookings(Pageable pageable, String status, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            List<Booking> bookingList;
            if (status != null && !status.isBlank()) {
                BookingStatus st;
                try { st = BookingStatus.valueOf(status.toUpperCase()); } catch (IllegalArgumentException ex) { return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Invalid status", ApiCode.VALIDATION_ERROR.getHttpStatus()); }
                bookingList = bookingRepository.findByGuestIdAndStatus(currentUser.getId(), st);
            } else {
                bookingList = bookingRepository.findByGuestId(currentUser.getId());
            }

            PaginationResult<BookingResponse> result = buildPaginationResultFromList(bookingList, pageable);
            return ApplicationResponse.success(result);
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving user bookings: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve bookings", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Retrieves bookings for a specific property with optional status and date range filters.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<PaginationResult<BookingResponse>> getPropertyBookings(Long propertyId, Pageable pageable, String status, LocalDate startDate, LocalDate endDate, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Ensure current user is the owner/host of the property
            Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view bookings for this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<Booking> bookingList = bookingRepository.findByPropertyId(propertyId);

            // Apply filters if provided
            if (status != null && !status.isBlank()) {
                BookingStatus st;
                try { st = BookingStatus.valueOf(status.toUpperCase()); } catch (IllegalArgumentException ex) { return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Invalid status", ApiCode.VALIDATION_ERROR.getHttpStatus()); }
                bookingList = bookingList.stream().filter(b -> b.getStatus() == st).collect(Collectors.toList());
            }
            if (startDate != null) {
                bookingList = bookingList.stream().filter(b -> !b.getCheckInDate().isBefore(startDate)).collect(Collectors.toList());
            }
            if (endDate != null) {
                bookingList = bookingList.stream().filter(b -> !b.getCheckOutDate().isAfter(endDate)).collect(Collectors.toList());
            }

            PaginationResult<BookingResponse> result = buildPaginationResultFromList(bookingList, pageable);
            return ApplicationResponse.success(result);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving property bookings: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve property bookings", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Retrieves bookings across all properties owned by the current host, with optional status/date filters.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<PaginationResult<BookingResponse>> getHostBookings(Pageable pageable, String status, LocalDate startDate, LocalDate endDate, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            List<Booking> bookingList;
            if (status != null && !status.isBlank()) {
                BookingStatus st;
                try { st = BookingStatus.valueOf(status.toUpperCase()); } catch (IllegalArgumentException ex) { return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Invalid status", ApiCode.VALIDATION_ERROR.getHttpStatus()); }
                bookingList = bookingRepository.findByHostIdAndStatus(currentUser.getId(), st);
            } else {
                bookingList = bookingRepository.findByHostId(currentUser.getId());
            }

            if (startDate != null) {
                bookingList = bookingList.stream().filter(b -> !b.getCheckInDate().isBefore(startDate)).collect(Collectors.toList());
            }
            if (endDate != null) {
                bookingList = bookingList.stream().filter(b -> !b.getCheckOutDate().isAfter(endDate)).collect(Collectors.toList());
            }

            PaginationResult<BookingResponse> result = buildPaginationResultFromList(bookingList, pageable);
            return ApplicationResponse.success(result);
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving host bookings: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve host bookings", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    // Helper to build PaginationResult from list and pageable
    private PaginationResult<BookingResponse> buildPaginationResultFromList(List<Booking> bookingList, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), bookingList.size());
        if (start > end) { start = Math.max(0, bookingList.size() - pageable.getPageSize()); end = bookingList.size(); }
        List<BookingResponse> pageContent = bookingList.subList(start, end).stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());

        int pageSize = pageable.getPageSize();
        long totalRecords = bookingList.size();
        int lastPageNumber = (int) Math.ceil((double) Math.max(1, totalRecords) / Math.max(1, pageSize));
        int currentPageNumber = Math.min(pageable.getPageNumber() + 1, Math.max(1, lastPageNumber));

        PaginationResult<BookingResponse> result = new PaginationResult<>();
        result.setCurrentPageNumber(currentPageNumber);
        result.setLastPageNumber(lastPageNumber == 0 ? 1 : lastPageNumber);
        result.setPageSize(pageSize);
        result.setTotalRecords(totalRecords);
        result.setRecords(pageContent);
        return result;
    }
}
