package com.imovel.api.booking.service;

import com.imovel.api.booking.model.Booking;
import com.imovel.api.booking.model.PropertyAvailability;
import com.imovel.api.booking.repository.BookingRepository;
import com.imovel.api.booking.repository.PropertyAvailabilityRepository;
import com.imovel.api.booking.request.AvailabilityCheckRequest;
import com.imovel.api.booking.request.PropertyAvailabilityRequest;
import com.imovel.api.booking.response.AvailabilityCheckResponse;
import com.imovel.api.booking.response.PropertyAvailabilityResponse;
import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Property;
import com.imovel.api.model.User;
import com.imovel.api.repository.PropertyRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.pagination.PaginationResult;
import com.imovel.api.session.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing property availability.
 * Handles availability checking, blocking, and pricing calculations.
 */
@Service
@Transactional
public class PropertyAvailabilityService {

    private final PropertyAvailabilityRepository availabilityRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final SessionManager sessionManager;

    private static final String SERVICE_NAME = "PropertyAvailabilityService";

    @Autowired
    public PropertyAvailabilityService(PropertyAvailabilityRepository availabilityRepository,
                                     PropertyRepository propertyRepository,
                                     BookingRepository bookingRepository,
                                     SessionManager sessionManager) {
        this.availabilityRepository = availabilityRepository;
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Checks availability for a property within specified date range.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<AvailabilityCheckResponse> checkAvailability(AvailabilityCheckRequest request) {
        try {
            ApiLogger.info(SERVICE_NAME, "Checking availability for property: " + request.getPropertyId());

            // Validate property exists
            Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

            // Get availability records for the date range
            List<PropertyAvailability> availabilityRecords = availabilityRepository
                .findByPropertyIdAndDateRange(request.getPropertyId(), 
                    request.getCheckInDate(), request.getCheckOutDate().minusDays(1));

            // Check for existing bookings
            List<Booking> existingBookings = bookingRepository.findOverlappingBookings(
                request.getPropertyId(), request.getCheckInDate(), request.getCheckOutDate());

            // Build response
            AvailabilityCheckResponse response = new AvailabilityCheckResponse();
            response.setPropertyId(request.getPropertyId());
            response.setCheckInDate(request.getCheckInDate());
            response.setCheckOutDate(request.getCheckOutDate());
            response.setNumberOfAdults(request.getNumberOfAdults());
            response.setNumberOfChildren(request.getNumberOfChildren());

            // Calculate total nights
            long totalNights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
            response.setTotalNights((int) totalNights);

            // Check if dates are available
            List<LocalDate> unavailableDates = new ArrayList<>();
            List<String> restrictions = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;
            boolean isInstantBookable = true;
            Integer minStay = null;
            Integer maxStay = null;

            // Check each date in the range
            LocalDate currentDate = request.getCheckInDate();
            while (currentDate.isBefore(request.getCheckOutDate())) {
                final LocalDate dateToCheck = currentDate;

                // Check if date is blocked by existing booking
                boolean hasBooking = existingBookings.stream()
                    .anyMatch(booking -> !dateToCheck.isBefore(booking.getCheckInDate()) && 
                                       dateToCheck.isBefore(booking.getCheckOutDate()));

                if (hasBooking) {
                    unavailableDates.add(currentDate);
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                // Check availability record for this date
                PropertyAvailability availability = availabilityRecords.stream()
                    .filter(a -> a.getDate().equals(dateToCheck))
                    .findFirst()
                    .orElse(null);

                if (availability != null) {
                    if (!availability.getIsAvailable()) {
                        unavailableDates.add(currentDate);
                        if (availability.getBlockedReason() != null) {
                            restrictions.add("Date " + currentDate + ": " + availability.getBlockedReason());
                        }
                    } else {
                        // Add to total price
                        if (availability.getPrice() != null) {
                            totalPrice = totalPrice.add(availability.getPrice());
                        } else {
                            // Use property base price
                            totalPrice = totalPrice.add(property.getPrice());
                        }

                        // Check instant booking
                        if (availability.getIsInstantBook() != null && !availability.getIsInstantBook()) {
                            isInstantBookable = false;
                        }

                        // Track min/max stay requirements
                        if (availability.getMinStay() != null) {
                            minStay = minStay == null ? availability.getMinStay() : 
                                     Math.max(minStay, availability.getMinStay());
                        }
                        if (availability.getMaxStay() != null) {
                            maxStay = maxStay == null ? availability.getMaxStay() : 
                                     Math.min(maxStay, availability.getMaxStay());
                        }
                    }
                } else {
                    // No specific availability record, use property defaults
                    totalPrice = totalPrice.add(property.getPrice());
                }

                currentDate = currentDate.plusDays(1);
            }

            // Set response data
            response.setIsAvailable(unavailableDates.isEmpty());
            response.setIsInstantBookable(isInstantBookable && unavailableDates.isEmpty());
            response.setTotalPrice(totalPrice);
            response.setAverageNightlyRate(totalNights > 0 ? 
                totalPrice.divide(BigDecimal.valueOf(totalNights), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            response.setUnavailableDates(
                unavailableDates.stream()
                    .map(LocalDate::toString)
                    .collect(Collectors.toList())
            );
            response.setRestrictions(restrictions);
            response.setMinStayRequired(minStay);
            response.setMaxStayAllowed(maxStay);

            // Check stay duration requirements
            if (minStay != null && totalNights < minStay) {
                response.setIsAvailable(false);
                restrictions.add("Minimum stay requirement: " + minStay + " nights");
            }
            if (maxStay != null && totalNights > maxStay) {
                response.setIsAvailable(false);
                restrictions.add("Maximum stay requirement: " + maxStay + " nights");
            }

            // Check guest capacity
            int totalGuests = request.getNumberOfAdults() + (request.getNumberOfChildren() != null ? request.getNumberOfChildren() : 0);
            Integer maxAdults = property.getMaxAdultsAccommodation();
            Integer maxChildren = property.getMaxChildrenAccommodation();
            Integer totalMaxGuests = (maxAdults != null ? maxAdults : 0) + (maxChildren != null ? maxChildren : 0);
            
            if (totalMaxGuests > 0 && totalGuests > totalMaxGuests) {
                response.setIsAvailable(false);
                restrictions.add("Property capacity exceeded. Maximum guests: " + totalMaxGuests);
            }

            String finalMessage;
            boolean bookable = response.isBookable();
            if (bookable) {
                if (response.getIsInstantBookable()) {
                    finalMessage = "Property is available for instant booking";
                } else {
                    finalMessage = "Property is available but requires host approval";
                }
            } else {
                finalMessage = "Property is not available for the selected dates";
            }
            response.setMessage(finalMessage);

            ApiLogger.info(SERVICE_NAME, "Availability check completed for property: " + request.getPropertyId());

            return ApplicationResponse.success(response);

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Property not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error checking availability: " + e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to check availability",
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Sets availability for a property on specific dates.
     */
    public ApplicationResponse<PropertyAvailabilityResponse> setPropertyAvailability(
            PropertyAvailabilityRequest request, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Setting availability for property: " + request.getPropertyId());

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate property exists and user owns it
            Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to modify this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Check if availability record already exists
            PropertyAvailability availability = availabilityRepository
                .findByPropertyIdAndDate(request.getPropertyId(), request.getDate())
                .orElse(new PropertyAvailability());

            // Set property if new record
            if (availability.getId() == null) {
                availability.setProperty(property);
                availability.setDate(request.getDate());
            }

            // Update fields
            availability.setIsAvailable(request.getIsAvailable());
            availability.setPrice(request.getPrice());
            availability.setMinStay(request.getMinStay());
            availability.setMaxStay(request.getMaxStay());
            availability.setBlockedReason(request.getBlockedReason());
            availability.setIsInstantBook(request.getIsInstantBook());
            availability.setCheckInAllowed(request.getCheckInAllowed());
            availability.setCheckOutAllowed(request.getCheckOutAllowed());
            availability.setNotes(request.getNotes());

            availability = availabilityRepository.save(availability);

            ApiLogger.info(SERVICE_NAME, "Successfully set availability for property: " + request.getPropertyId());

            return ApplicationResponse.success(convertToAvailabilityResponse(availability));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Property not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error setting availability: " + e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to set availability",
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Gets availability records for a property within a date range.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<PropertyAvailabilityResponse>> getPropertyAvailability(
            Long propertyId, LocalDate startDate, LocalDate endDate, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate property exists and user owns it
            Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<PropertyAvailability> availabilityRecords = availabilityRepository
                .findByPropertyIdAndDateRange(propertyId, startDate, endDate);

            List<PropertyAvailabilityResponse> responses = availabilityRecords.stream()
                .map(this::convertToAvailabilityResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(responses);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(
                    ApiCode.RESOURCE_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.RESOURCE_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error updating availability: " + e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update availability",
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }



    /**
     * Blocks dates for a confirmed booking.
     */
    public void blockDatesForBooking(Booking booking) {
        try {
            ApiLogger.info(SERVICE_NAME, "Blocking dates for booking: " + booking.getId());

            LocalDate currentDate = booking.getCheckInDate();
            while (currentDate.isBefore(booking.getCheckOutDate())) {
                PropertyAvailability availability = availabilityRepository
                    .findByPropertyIdAndDate(booking.getProperty().getId(), currentDate)
                    .orElse(new PropertyAvailability());

                if (availability.getId() == null) {
                    availability.setProperty(booking.getProperty());
                    availability.setDate(currentDate);
                }

                availability.setIsAvailable(false);
                availability.setBlockedReason("Booked (Booking #" + booking.getId() + ")");

                availabilityRepository.save(availability);
                currentDate = currentDate.plusDays(1);
            }

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error blocking dates for booking: " + e.getMessage());
        }
    }

    /**
     * Releases dates when a booking is cancelled.
     */
    public void releaseDatesForBooking(Booking booking) {
        try {
            ApiLogger.info(SERVICE_NAME, "Releasing dates for cancelled booking: " + booking.getId());

            LocalDate currentDate = booking.getCheckInDate();
            while (currentDate.isBefore(booking.getCheckOutDate())) {
                availabilityRepository.findByPropertyIdAndDate(booking.getProperty().getId(), currentDate)
                    .ifPresent(availability -> {
                        if (availability.getBlockedReason() != null && 
                            availability.getBlockedReason().contains("Booking #" + booking.getId())) {
                            availability.setIsAvailable(true);
                            availability.setBlockedReason(null);
                            availabilityRepository.save(availability);
                        }
                    });

                currentDate = currentDate.plusDays(1);
            }

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error releasing dates for booking: " + e.getMessage());
        }
    }

    /**
     * Deletes an availability record.
     */
    public ApplicationResponse<Void> deleteAvailability(Long availabilityId, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Deleting availability record: " + availabilityId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Find the availability record
            PropertyAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability record", availabilityId));

            // Check if user owns the property
            if (!availability.getProperty().getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(
                    ApiCode.PERMISSION_DENIED.getCode(), 
                    "Not authorized to delete this availability record", 
                    ApiCode.PERMISSION_DENIED.getHttpStatus()
                );
            }

            availabilityRepository.delete(availability);

            ApiLogger.info(SERVICE_NAME, "Successfully deleted availability record: " + availabilityId);
            return ApplicationResponse.success("Availability record deleted successfully");

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Availability record not found: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.RESOURCE_NOT_FOUND.getCode(), 
                e.getMessage(), 
                ApiCode.RESOURCE_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error deleting availability record: " + e.getMessage());
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to delete availability record",
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Update an existing availability record by ID.
     */
    public ApplicationResponse<PropertyAvailabilityResponse> updateAvailability(
            Long availabilityId, PropertyAvailabilityRequest request, HttpSession session) {
        try {
            if (availabilityId == null || request == null) {
                return ApplicationResponse.error(
                        ApiCode.VALIDATION_ERROR.getCode(),
                        "availabilityId and request are required",
                        ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            PropertyAvailability availability = availabilityRepository.findById(availabilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Availability record", availabilityId));

            if (!availability.getProperty().getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(
                        ApiCode.PERMISSION_DENIED.getCode(),
                        "Not authorized to update this availability record",
                        ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            if (request.getPropertyId() != null && !request.getPropertyId().equals(availability.getProperty().getId())) {
                return ApplicationResponse.error(
                        ApiCode.VALIDATION_ERROR.getCode(),
                        "Property ID mismatch for the availability record",
                        ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // If date is being changed, ensure uniqueness for property/date
            LocalDate targetDate = request.getDate() != null ? request.getDate() : availability.getDate();
            if (!targetDate.equals(availability.getDate())) {
                Optional<PropertyAvailability> existingForDate = availabilityRepository
                        .findByPropertyIdAndDate(availability.getProperty().getId(), targetDate);
                if (existingForDate.isPresent() && !existingForDate.get().getId().equals(availability.getId())) {
                    return ApplicationResponse.error(
                            ApiCode.VALIDATION_ERROR.getCode(),
                            "An availability record already exists for the selected date",
                            ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                availability.setDate(targetDate);
            }

            // Update fields from request when provided
            if (request.getIsAvailable() != null) {
                availability.setIsAvailable(request.getIsAvailable());
            }
            if (request.getPrice() != null) {
                availability.setPrice(request.getPrice());
            }
            if (request.getMinStay() != null) {
                availability.setMinStay(request.getMinStay());
            }
            if (request.getMaxStay() != null) {
                availability.setMaxStay(request.getMaxStay());
            }
            if (request.getBlockedReason() != null) {
                availability.setBlockedReason(request.getBlockedReason());
            }
            if (request.getIsInstantBook() != null) {
                availability.setIsInstantBook(request.getIsInstantBook());
            }
            if (request.getCheckInAllowed() != null) {
                availability.setCheckInAllowed(request.getCheckInAllowed());
            }
            if (request.getCheckOutAllowed() != null) {
                availability.setCheckOutAllowed(request.getCheckOutAllowed());
            }
            if (request.getNotes() != null) {
                availability.setNotes(request.getNotes());
            }

            availability = availabilityRepository.save(availability);

            ApiLogger.info(SERVICE_NAME, "Successfully updated availability record: " + availabilityId);
            return ApplicationResponse.success(convertToAvailabilityResponse(availability));
        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Availability record not found: " + e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.RESOURCE_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.RESOURCE_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error updating availability record: " + e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update availability",
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Overload: Public access to get availability without authentication
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<PropertyAvailabilityResponse>> getPropertyAvailability(
            Long propertyId, LocalDate startDate, LocalDate endDate) {
        try {
            // Validate property exists
            propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            LocalDate effectiveStart = (startDate != null) ? startDate : LocalDate.now();
            LocalDate effectiveEnd = (endDate != null) ? endDate : effectiveStart.plusDays(31);

            List<PropertyAvailabilityResponse> responses = availabilityRepository
                .findByPropertyIdAndDateRange(propertyId, effectiveStart, effectiveEnd)
                .stream()
                .map(this::convertToAvailabilityResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(responses);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(
                    ApiCode.RESOURCE_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.RESOURCE_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving availability: " + e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve availability",
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Bulk update availability for a list of requests.
     */
    public ApplicationResponse<List<PropertyAvailabilityResponse>> bulkUpdateAvailability(
            List<PropertyAvailabilityRequest> requests, HttpSession session) {
        try {
            if (requests == null || requests.isEmpty()) {
                return ApplicationResponse.error(
                        ApiCode.VALIDATION_ERROR.getCode(),
                        "No availability requests provided",
                        ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            Long propertyId = requests.get(0).getPropertyId();
            if (propertyId == null || requests.stream().anyMatch(r -> r.getPropertyId() == null || !r.getPropertyId().equals(propertyId))) {
                return ApplicationResponse.error(
                        ApiCode.VALIDATION_ERROR.getCode(),
                        "All requests must target the same property",
                        ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to modify this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<PropertyAvailabilityResponse> responses = new ArrayList<>();
            for (PropertyAvailabilityRequest req : requests) {
                PropertyAvailability availability = availabilityRepository
                        .findByPropertyIdAndDate(propertyId, req.getDate())
                        .orElse(new PropertyAvailability());

                if (availability.getId() == null) {
                    availability.setProperty(property);
                    availability.setDate(req.getDate());
                }

                availability.setIsAvailable(req.getIsAvailable());
                availability.setPrice(req.getPrice());
                availability.setMinStay(req.getMinStay());
                availability.setMaxStay(req.getMaxStay());
                availability.setBlockedReason(req.getBlockedReason());
                availability.setIsInstantBook(req.getIsInstantBook());
                availability.setCheckInAllowed(req.getCheckInAllowed());
                availability.setCheckOutAllowed(req.getCheckOutAllowed());
                availability.setNotes(req.getNotes());

                availability = availabilityRepository.save(availability);
                responses.add(convertToAvailabilityResponse(availability));
            }

            ApiLogger.info(SERVICE_NAME, "Bulk availability update completed for property: " + propertyId);
            return ApplicationResponse.success(responses);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(
                    ApiCode.RESOURCE_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error bulk updating availability: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to bulk update availability", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Block dates for a property.
     */
    public ApplicationResponse<List<PropertyAvailabilityResponse>> blockDates(
            Long propertyId, LocalDate startDate, LocalDate endDate, String reason, HttpSession session) {
        try {
            if (propertyId == null || startDate == null || endDate == null) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "PropertyId, startDate and endDate are required", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to modify this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<PropertyAvailabilityResponse> responses = new ArrayList<>();
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                PropertyAvailability availability = availabilityRepository
                        .findByPropertyIdAndDate(propertyId, date)
                        .orElse(new PropertyAvailability());
                if (availability.getId() == null) {
                    availability.setProperty(property);
                    availability.setDate(date);
                }
                availability.setIsAvailable(false);
                availability.setBlockedReason((reason != null && !reason.isBlank()) ? reason : "Blocked by host");
                availability = availabilityRepository.save(availability);
                responses.add(convertToAvailabilityResponse(availability));
                date = date.plusDays(1);
            }

            ApiLogger.info(SERVICE_NAME, "Blocked dates for property: " + propertyId + " from " + startDate + " to " + endDate);
            return ApplicationResponse.success(responses);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error blocking dates: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to block dates", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Release previously blocked dates for a property.
     */
    public ApplicationResponse<List<PropertyAvailabilityResponse>> releaseDates(
            Long propertyId, LocalDate startDate, LocalDate endDate, HttpSession session) {
        try {
            if (propertyId == null || startDate == null || endDate == null) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "PropertyId, startDate and endDate are required", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to modify this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<PropertyAvailabilityResponse> responses = new ArrayList<>();
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                PropertyAvailability availability = availabilityRepository
                        .findByPropertyIdAndDate(propertyId, date)
                        .orElse(new PropertyAvailability());
                if (availability.getId() == null) {
                    availability.setProperty(property);
                    availability.setDate(date);
                }
                availability.setIsAvailable(true);
                availability.setBlockedReason(null);
                availability = availabilityRepository.save(availability);
                responses.add(convertToAvailabilityResponse(availability));
                date = date.plusDays(1);
            }

            ApiLogger.info(SERVICE_NAME, "Released dates for property: " + propertyId + " from " + startDate + " to " + endDate);
            return ApplicationResponse.success(responses);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error releasing dates: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to release dates", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Get availability calendar with pagination.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<PaginationResult<PropertyAvailabilityResponse>> getAvailabilityCalendar(
            Long propertyId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            LocalDate effectiveStart = (startDate != null) ? startDate : LocalDate.now();
            LocalDate effectiveEnd = (endDate != null) ? endDate : effectiveStart.plusDays(31);

            List<PropertyAvailabilityResponse> all = availabilityRepository
                    .findByPropertyIdAndDateRange(propertyId, effectiveStart, effectiveEnd)
                    .stream()
                    .map(this::convertToAvailabilityResponse)
                    .collect(Collectors.toList());

            PaginationResult<PropertyAvailabilityResponse> result = buildPaginationResultFromList(all, pageable);
            return ApplicationResponse.success(result);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving availability calendar: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve availability calendar", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Get blocked dates for a property.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<PropertyAvailabilityResponse>> getBlockedDates(
            Long propertyId, LocalDate startDate, LocalDate endDate) {
        try {
            propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            LocalDate effectiveStart = (startDate != null) ? startDate : LocalDate.now();
            LocalDate effectiveEnd = (endDate != null) ? endDate : effectiveStart.plusDays(31);

            List<PropertyAvailabilityResponse> responses = availabilityRepository
                    .findByPropertyIdAndDateRange(propertyId, effectiveStart, effectiveEnd)
                    .stream()
                    .filter(a -> Boolean.FALSE.equals(a.getIsAvailable()))
                    .map(this::convertToAvailabilityResponse)
                    .collect(Collectors.toList());
            return ApplicationResponse.success(responses);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving blocked dates: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve blocked dates", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Get available dates for a property.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<PropertyAvailabilityResponse>> getAvailableDates(
            Long propertyId, LocalDate startDate, LocalDate endDate, boolean instantBookOnly) {
        try {
            propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            LocalDate effectiveStart = (startDate != null) ? startDate : LocalDate.now();
            LocalDate effectiveEnd = (endDate != null) ? endDate : effectiveStart.plusDays(31);

            List<PropertyAvailabilityResponse> responses = availabilityRepository
                    .findByPropertyIdAndDateRange(propertyId, effectiveStart, effectiveEnd)
                    .stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsAvailable()))
                    .filter(a -> !instantBookOnly || Boolean.TRUE.equals(a.getIsInstantBook()))
                    .map(this::convertToAvailabilityResponse)
                    .collect(Collectors.toList());
            return ApplicationResponse.success(responses);
        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving available dates: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve available dates", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    // Helper methods

    private PropertyAvailabilityResponse convertToAvailabilityResponse(PropertyAvailability availability) {
        PropertyAvailabilityResponse response = new PropertyAvailabilityResponse();
        response.setId(availability.getId());
        response.setPropertyId(availability.getProperty().getId());
        response.setDate(availability.getDate());
        response.setIsAvailable(availability.getIsAvailable());
        response.setPrice(availability.getPrice());
        response.setMinStay(availability.getMinStay());
        response.setMaxStay(availability.getMaxStay());
        response.setBlockedReason(availability.getBlockedReason());
        response.setIsInstantBook(availability.getIsInstantBook());
        response.setCheckInAllowed(availability.getCheckInAllowed());
        response.setCheckOutAllowed(availability.getCheckOutAllowed());
        response.setNotes(availability.getNotes());
        response.setCreatedAt(availability.getCreatedAt());
        response.setUpdatedAt(availability.getUpdatedAt());

        return response;
    }
    private PaginationResult<PropertyAvailabilityResponse> buildPaginationResultFromList(List<PropertyAvailabilityResponse> responses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        if (start > end) { start = Math.max(0, responses.size() - pageable.getPageSize()); end = responses.size(); }
        List<PropertyAvailabilityResponse> pageContent = responses.subList(start, end);

        int pageSize = pageable.getPageSize();
        long totalRecords = responses.size();
        int lastPageNumber = (int) Math.ceil((double) Math.max(1, totalRecords) / Math.max(1, pageSize));
        int currentPageNumber = Math.min(pageable.getPageNumber() + 1, Math.max(1, lastPageNumber));

        PaginationResult<PropertyAvailabilityResponse> result = new PaginationResult<>();
        result.setCurrentPageNumber(currentPageNumber);
        result.setLastPageNumber(lastPageNumber == 0 ? 1 : lastPageNumber);
        result.setPageSize(pageSize);
        result.setTotalRecords(totalRecords);
        result.setRecords(pageContent);
        return result;
    }
}
