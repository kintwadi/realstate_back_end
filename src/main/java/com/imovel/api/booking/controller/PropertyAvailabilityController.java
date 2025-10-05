package com.imovel.api.booking.controller;

import com.imovel.api.booking.request.AvailabilityCheckRequest;
import com.imovel.api.booking.request.PropertyAvailabilityRequest;
import com.imovel.api.booking.response.AvailabilityCheckResponse;
import com.imovel.api.booking.response.PropertyAvailabilityResponse;
import com.imovel.api.booking.service.PropertyAvailabilityService;
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
 * Controller for handling property availability operations.
 * Provides endpoints for checking availability, managing availability settings,
 * and retrieving availability information.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/availability")
public class PropertyAvailabilityController {

    private final PropertyAvailabilityService availabilityService;
    private final SessionManager sessionManager;

    @Autowired
    public PropertyAvailabilityController(PropertyAvailabilityService availabilityService, 
                                        SessionManager sessionManager) {
        this.availabilityService = availabilityService;
        this.sessionManager = sessionManager;
    }

    private String buildLogTag(String method) {
        return "PropertyAvailabilityController#" + method;
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
     * Check availability for a property
     */
    @PostMapping("/check")
    public ResponseEntity<ApplicationResponse<AvailabilityCheckResponse>> checkAvailability(
            @Valid @RequestBody AvailabilityCheckRequest request) {
        final String TAG = "checkAvailability";
        ApiLogger.info(buildLogTag(TAG), "Received availability check request for property: " + request.getPropertyId());

        try {
            ApplicationResponse<AvailabilityCheckResponse> response = 
                availabilityService.checkAvailability(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error checking availability: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to check availability: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get availability for a property within a date range
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApplicationResponse<List<PropertyAvailabilityResponse>>> getPropertyAvailability(
            @PathVariable Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        final String TAG = "getPropertyAvailability";
        ApiLogger.info(buildLogTag(TAG), "Received request to get availability for property: " + propertyId);

        try {
            ApplicationResponse<List<PropertyAvailabilityResponse>> response = 
                availabilityService.getPropertyAvailability(propertyId, startDate, endDate);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving property availability: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve property availability: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Set availability for a property (for hosts)
     */
    @PostMapping("/property/{propertyId}")
    public ResponseEntity<ApplicationResponse<PropertyAvailabilityResponse>> setPropertyAvailability(
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyAvailabilityRequest request,
            HttpSession session) {
        final String TAG = "setPropertyAvailability";
        ApiLogger.info(buildLogTag(TAG), "Received request to set availability for property: " + propertyId);

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
            // Set the property ID from the path parameter
            request.setPropertyId(propertyId);
            
            ApplicationResponse<PropertyAvailabilityResponse> response = 
                availabilityService.setPropertyAvailability(request, session);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error setting property availability: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to set property availability: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Update availability for a specific date
     */
    @PutMapping("/{availabilityId}")
    public ResponseEntity<ApplicationResponse<PropertyAvailabilityResponse>> updateAvailability(
            @PathVariable Long availabilityId,
            @Valid @RequestBody PropertyAvailabilityRequest request,
            HttpSession session) {
        final String TAG = "updateAvailability";
        ApiLogger.info(buildLogTag(TAG), "Received request to update availability: " + availabilityId);

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
            ApplicationResponse<PropertyAvailabilityResponse> response = 
                availabilityService.updateAvailability(availabilityId, request, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error updating availability: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update availability: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Bulk update availability for multiple dates
     */
    @PostMapping("/property/{propertyId}/bulk")
    public ResponseEntity<ApplicationResponse<List<PropertyAvailabilityResponse>>> bulkUpdateAvailability(
            @PathVariable Long propertyId,
            @Valid @RequestBody List<PropertyAvailabilityRequest> requests,
            HttpSession session) {
        final String TAG = "bulkUpdateAvailability";
        ApiLogger.info(buildLogTag(TAG), "Received bulk availability update for property: " + propertyId);

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
            // Set the property ID for all requests
            requests.forEach(request -> request.setPropertyId(propertyId));
            
            ApplicationResponse<List<PropertyAvailabilityResponse>> response = 
                availabilityService.bulkUpdateAvailability(requests, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error bulk updating availability: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to bulk update availability: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Block dates for a property
     */
    @PostMapping("/property/{propertyId}/block")
    public ResponseEntity<ApplicationResponse<List<PropertyAvailabilityResponse>>> blockDates(
            @PathVariable Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        final String TAG = "blockDates";
        ApiLogger.info(buildLogTag(TAG), "Received request to block dates for property: " + propertyId);

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
            ApplicationResponse<List<PropertyAvailabilityResponse>> response = 
                availabilityService.blockDates(propertyId, startDate, endDate, reason, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error blocking dates: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to block dates: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Release blocked dates for a property
     */
    @PostMapping("/property/{propertyId}/release")
    public ResponseEntity<ApplicationResponse<List<PropertyAvailabilityResponse>>> releaseDates(
            @PathVariable Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        final String TAG = "releaseDates";
        ApiLogger.info(buildLogTag(TAG), "Received request to release dates for property: " + propertyId);

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
            ApplicationResponse<List<PropertyAvailabilityResponse>> response = 
                availabilityService.releaseDates(propertyId, startDate, endDate, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error releasing dates: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to release dates: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get availability calendar for a property
     */
    @GetMapping("/property/{propertyId}/calendar")
    public ResponseEntity<ApplicationResponse<PaginationResult<PropertyAvailabilityResponse>>> getAvailabilityCalendar(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "31") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        final String TAG = "getAvailabilityCalendar";
        ApiLogger.info(buildLogTag(TAG), "Received request to get availability calendar for property: " + propertyId);

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
            
            ApplicationResponse<PaginationResult<PropertyAvailabilityResponse>> response = 
                availabilityService.getAvailabilityCalendar(propertyId, startDate, endDate, pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving availability calendar: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve availability calendar: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get blocked dates for a property
     */
    @GetMapping("/property/{propertyId}/blocked")
    public ResponseEntity<ApplicationResponse<List<PropertyAvailabilityResponse>>> getBlockedDates(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        final String TAG = "getBlockedDates";
        ApiLogger.info(buildLogTag(TAG), "Received request to get blocked dates for property: " + propertyId);

        try {
            ApplicationResponse<List<PropertyAvailabilityResponse>> response = 
                availabilityService.getBlockedDates(propertyId, startDate, endDate);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving blocked dates: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve blocked dates: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get available dates for a property
     */
    @GetMapping("/property/{propertyId}/available")
    public ResponseEntity<ApplicationResponse<List<PropertyAvailabilityResponse>>> getAvailableDates(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean instantBookOnly) {
        final String TAG = "getAvailableDates";
        ApiLogger.info(buildLogTag(TAG), "Received request to get available dates for property: " + propertyId);

        try {
            ApplicationResponse<List<PropertyAvailabilityResponse>> response = 
                availabilityService.getAvailableDates(propertyId, startDate, endDate, instantBookOnly);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving available dates: " + e.getMessage(), e);
            ApplicationResponse<List<PropertyAvailabilityResponse>> errorResponse = 
                ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                    "Failed to retrieve available dates: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete availability record
     */
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<ApplicationResponse<Void>> deleteAvailability(
            @PathVariable Long availabilityId,
            HttpSession session) {
        final String TAG = "deleteAvailability";
        ApiLogger.info(buildLogTag(TAG), "Received request to delete availability: " + availabilityId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<Void> errorResponse = 
                ApplicationResponse.error(ApiCode.ACCESS_DENIED.getCode(), 
                    "Authentication failed", 
                    HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<Void> response = 
                availabilityService.deleteAvailability(availabilityId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error deleting availability: " + e.getMessage(), e);
            ApplicationResponse<Void> errorResponse = 
                ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                    "Failed to delete availability: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
