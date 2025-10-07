package com.imovel.api.booking.controller;

import com.imovel.api.booking.request.BookingCreateRequest;
import com.imovel.api.booking.request.BookingUpdateRequest;
import com.imovel.api.booking.response.BookingResponse;
import com.imovel.api.booking.service.BookingService;
import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.pagination.PaginationResult;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.CurrentUser;
import com.imovel.api.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for handling booking-related operations.
 * Provides endpoints for creating, updating, retrieving, and managing bookings.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final SessionManager sessionManager;

    @Autowired
    public BookingController(BookingService bookingService, SessionManager sessionManager) {
        this.bookingService = bookingService;
        this.sessionManager = sessionManager;
    }

    private String buildLogTag(String method) {
        return "BookingController#" + method;
    }

    private ResponseEntity<ApplicationResponse<?>> verifyAuthentication(HttpSession session) {
        try {
            if (session == null) {
                ApiLogger.warn(buildLogTag("verifyAuthentication"), "No HTTP session found");
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication required: No active session",
                        HttpStatus.UNAUTHORIZED);
            }

            CurrentUser currentUser = sessionManager.getCurrentUser(session);
            if (currentUser == null) {
                ApiLogger.warn(buildLogTag("verifyAuthentication"), "No authenticated user found in session");
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication required: Please login again",
                        HttpStatus.UNAUTHORIZED);
            }

            return null; // Authentication successful
        } catch (Exception e) {
            ApiLogger.error(buildLogTag("verifyAuthentication"), "Authentication error: " + e.getMessage(), e);
            return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                    "Authentication error: " + e.getMessage(),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<ApplicationResponse<?>> createErrorResponse(int code, String message, HttpStatus status) {
        ApplicationResponse<?> response = ApplicationResponse.error(code, message, status);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Create a new booking
     */
    @PostMapping
    public ResponseEntity<ApplicationResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingCreateRequest bookingRequest,
            HttpSession session) {
        final String TAG = "createBooking";
        ApiLogger.info(buildLogTag(TAG), "Received request to create booking for property: " + bookingRequest.getPropertyId());

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingResponse> response = bookingService.createBooking(bookingRequest, session);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error creating booking: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to create booking: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApplicationResponse<BookingResponse>> getBookingById(
            @PathVariable Long bookingId,
            HttpSession session) {
        final String TAG = "getBookingById";
        ApiLogger.info(buildLogTag(TAG), "Received request to get booking: " + bookingId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingResponse> response = bookingService.getBookingById(bookingId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving booking: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve booking: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Update an existing booking
     */
    @PutMapping("/{bookingId}")
    public ResponseEntity<ApplicationResponse<BookingResponse>> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingUpdateRequest updateRequest,
            HttpSession session) {
        final String TAG = "updateBooking";
        ApiLogger.info(buildLogTag(TAG), "Received request to update booking: " + bookingId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingResponse> response = bookingService.updateBooking(bookingId, updateRequest, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error updating booking: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update booking: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Cancel a booking
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApplicationResponse<BookingResponse>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String cancellationReason,
            HttpSession session) {
        final String TAG = "cancelBooking";
        ApiLogger.info(buildLogTag(TAG), "Received request to cancel booking: " + bookingId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingResponse> response = bookingService.cancelBooking(bookingId, cancellationReason, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error canceling booking: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to cancel booking: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Confirm a booking (for hosts)
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApplicationResponse<BookingResponse>> confirmBooking(
            @PathVariable Long bookingId,
            HttpSession session) {
        final String TAG = "confirmBooking";
        ApiLogger.info(buildLogTag(TAG), "Received request to confirm booking: " + bookingId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingResponse> response = bookingService.confirmBooking(bookingId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error confirming booking: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to confirm booking: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get user's bookings (as guest)
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<ApplicationResponse<PaginationResult<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String status,
            HttpSession session) {
        final String TAG = "getMyBookings";
        ApiLogger.info(buildLogTag(TAG), "Received request to get user's bookings");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
            
            ApplicationResponse<PaginationResult<BookingResponse>> response = 
                bookingService.getUserBookings(pageable, status, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving user bookings: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve bookings: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get property bookings (for hosts)
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApplicationResponse<PaginationResult<BookingResponse>>> getPropertyBookings(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        final String TAG = "getPropertyBookings";
        ApiLogger.info(buildLogTag(TAG), "Received request to get bookings for property: " + propertyId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
            
            ApplicationResponse<PaginationResult<BookingResponse>> response = 
                bookingService.getPropertyBookings(propertyId, pageable, status, startDate, endDate, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving property bookings: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve property bookings: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get host's bookings across all properties
     */
    @GetMapping("/host-bookings")
    public ResponseEntity<ApplicationResponse<PaginationResult<BookingResponse>>> getHostBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        final String TAG = "getHostBookings";
        ApiLogger.info(buildLogTag(TAG), "Received request to get host's bookings");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
            
            ApplicationResponse<PaginationResult<BookingResponse>> response = 
                bookingService.getHostBookings(pageable, status, startDate, endDate, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving host bookings: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve host bookings: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get booking statistics for a property
     */
    @GetMapping("/property/{propertyId}/statistics")
    public ResponseEntity<ApplicationResponse<BookingService.BookingStatistics>> getPropertyBookingStatistics(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        final String TAG = "getPropertyBookingStatistics";
        ApiLogger.info(buildLogTag(TAG), "Received request to get booking statistics for property: " + propertyId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingService.BookingStatistics> response = 
                bookingService.getPropertyBookingStatistics(propertyId, startDate, endDate, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving booking statistics: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve booking statistics: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get upcoming check-ins for host
     */
    @GetMapping("/upcoming-checkins")
    public ResponseEntity<ApplicationResponse<List<BookingResponse>>> getUpcomingCheckIns(
            @RequestParam(defaultValue = "7") int days,
            HttpSession session) {
        final String TAG = "getUpcomingCheckIns";
        ApiLogger.info(buildLogTag(TAG), "Received request to get upcoming check-ins");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<List<BookingResponse>> response = 
                bookingService.getUpcomingCheckIns(days, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving upcoming check-ins: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve upcoming check-ins: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get upcoming check-outs for host
     */
    @GetMapping("/upcoming-checkouts")
    public ResponseEntity<ApplicationResponse<List<BookingResponse>>> getUpcomingCheckOuts(
            @RequestParam(defaultValue = "7") int days,
            HttpSession session) {
        final String TAG = "getUpcomingCheckOuts";
        ApiLogger.info(buildLogTag(TAG), "Received request to get upcoming check-outs");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<List<BookingResponse>> response = 
                bookingService.getUpcomingCheckOuts(days, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving upcoming check-outs: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve upcoming check-outs: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
