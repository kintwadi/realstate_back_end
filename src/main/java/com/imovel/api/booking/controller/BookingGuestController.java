package com.imovel.api.booking.controller;

import com.imovel.api.booking.request.BookingGuestRequest;
import com.imovel.api.booking.response.BookingGuestResponse;
import com.imovel.api.booking.service.BookingGuestService;
import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.CurrentUser;
import com.imovel.api.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling booking guest operations.
 * Provides endpoints for managing guests associated with bookings.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking-guests")
public class BookingGuestController {

    private final BookingGuestService guestService;
    private final SessionManager sessionManager;

    @Autowired
    public BookingGuestController(BookingGuestService guestService, SessionManager sessionManager) {
        this.guestService = guestService;
        this.sessionManager = sessionManager;
    }

    private String buildLogTag(String method) {
        return "BookingGuestController#" + method;
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
     * Add a guest to a booking
     */
    @PostMapping("/{bookingId}")
    public ResponseEntity<ApplicationResponse<BookingGuestResponse>> addGuest(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingGuestRequest guestRequest,
            HttpSession session) {
        final String TAG = "addGuest";
        ApiLogger.info(buildLogTag(TAG), "Received request to add guest to booking: " + bookingId);

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
            ApplicationResponse<BookingGuestResponse> response = guestService.addGuestToBooking(bookingId, guestRequest, session);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error adding guest: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to add guest: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get guest by ID
     */
    @GetMapping("/{guestId}")
    public ResponseEntity<ApplicationResponse<BookingGuestResponse>> getGuestById(
            @PathVariable Long guestId,
            HttpSession session) {
        final String TAG = "getGuestById";
        ApiLogger.info(buildLogTag(TAG), "Received request to get guest: " + guestId);

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
            ApplicationResponse<BookingGuestResponse> response = guestService.getGuestById(guestId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving guest: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve guest: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get all guests for a booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApplicationResponse<List<BookingGuestResponse>>> getBookingGuests(
            @PathVariable Long bookingId,
            HttpSession session) {
        final String TAG = "getBookingGuests";
        ApiLogger.info(buildLogTag(TAG), "Received request to get guests for booking: " + bookingId);

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
            ApplicationResponse<List<BookingGuestResponse>> response = guestService.getBookingGuests(bookingId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving booking guests: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve booking guests: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Update guest information
     */
    @PutMapping("/{guestId}")
    public ResponseEntity<ApplicationResponse<BookingGuestResponse>> updateGuest(
            @PathVariable Long guestId,
            @Valid @RequestBody BookingGuestRequest guestRequest,
            HttpSession session) {
        final String TAG = "updateGuest";
        ApiLogger.info(buildLogTag(TAG), "Received request to update guest: " + guestId);

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
            ApplicationResponse<BookingGuestResponse> response = guestService.updateGuest(guestId, guestRequest, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error updating guest: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update guest: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Remove a guest from a booking
     */
    @DeleteMapping("/{guestId}")
    public ResponseEntity<ApplicationResponse<Void>> removeGuest(
            @PathVariable Long guestId,
            HttpSession session) {
        final String TAG = "removeGuest";
        ApiLogger.info(buildLogTag(TAG), "Received request to remove guest: " + guestId);

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
            ApplicationResponse<Void> response = guestService.removeGuestFromBooking(guestId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error removing guest: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to remove guest: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Add multiple guests to a booking
     */
    @PostMapping("/booking/{bookingId}/bulk")
    public ResponseEntity<ApplicationResponse<List<BookingGuestResponse>>> addMultipleGuests(
            @PathVariable Long bookingId,
            @Valid @RequestBody List<BookingGuestRequest> guestRequests,
            HttpSession session) {
        final String TAG = "addMultipleGuests";
        ApiLogger.info(buildLogTag(TAG), "Received request to add " + guestRequests.size() + 
                " guests to booking: " + bookingId);

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
            ApplicationResponse<List<BookingGuestResponse>> response = guestService.addMultipleGuests(bookingId, guestRequests, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error adding multiple guests: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to add multiple guests: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }






}
