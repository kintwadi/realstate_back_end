package com.imovel.api.booking.service;

import com.imovel.api.booking.model.Booking;
import com.imovel.api.booking.model.BookingGuest;
import com.imovel.api.booking.repository.BookingGuestRepository;
import com.imovel.api.booking.repository.BookingRepository;
import com.imovel.api.booking.request.BookingGuestRequest;
import com.imovel.api.booking.response.BookingGuestResponse;
import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.User;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing booking guests.
 * Handles guest information for bookings including additional guests.
 */
@Service
@Transactional
public class BookingGuestService {

    private final BookingGuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final SessionManager sessionManager;

    private static final String SERVICE_NAME = "BookingGuestService";

    @Autowired
    public BookingGuestService(BookingGuestRepository guestRepository,
                             BookingRepository bookingRepository,
                             SessionManager sessionManager) {
        this.guestRepository = guestRepository;
        this.bookingRepository = bookingRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Adds a guest to a booking.
     */
    public ApplicationResponse<BookingGuestResponse> addGuestToBooking(Long bookingId, 
                                                                      BookingGuestRequest request, 
                                                                      HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Adding guest to booking: " + bookingId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate booking exists
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            // Check permissions (only booking guest can add guests)
            if (!booking.getGuest().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to add guests to this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate booking is in a state where guests can be added
            if (booking.getStatus().name().equals("CANCELLED") || booking.getStatus().name().equals("COMPLETED")) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Cannot add guests to cancelled or completed booking", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check if this would exceed property capacity
            int currentGuestCount = booking.getNumberOfAdults() + 
                (booking.getNumberOfChildren() != null ? booking.getNumberOfChildren() : 0);
            
            List<BookingGuest> existingGuests = guestRepository.findByBookingId(bookingId);
            int additionalGuestCount = existingGuests.size() + 1;

            Integer maxAdults = booking.getProperty().getMaxAdultsAccommodation();
            Integer maxChildren = booking.getProperty().getMaxChildrenAccommodation();
            Integer totalMaxGuests = (maxAdults != null ? maxAdults : 0) + (maxChildren != null ? maxChildren : 0);
            
            if (totalMaxGuests > 0 && (currentGuestCount + additionalGuestCount) > totalMaxGuests) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Adding this guest would exceed property capacity", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check if there's already a primary guest (only one allowed)
            if (request.getIsPrimaryGuest() != null && request.getIsPrimaryGuest()) {
                boolean hasPrimaryGuest = existingGuests.stream()
                    .anyMatch(guest -> guest.getIsPrimaryGuest() != null && guest.getIsPrimaryGuest());
                
                if (hasPrimaryGuest) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Booking already has a primary guest", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
            }

            // Create guest record
            BookingGuest guest = new BookingGuest();
            guest.setBooking(booking);
            guest.setFullName(request.getFullName());
            guest.setEmail(request.getEmail());
            guest.setPhone(request.getPhone());
            guest.setAge(request.getAge());
            guest.setSpecialRequests(request.getSpecialRequests());
            guest.setDietaryRestrictions(request.getDietaryRestrictions());
            guest.setEmergencyContactName(request.getEmergencyContactName());
            guest.setEmergencyContactPhone(request.getEmergencyContactPhone());
            guest.setIsPrimaryGuest(request.getIsPrimaryGuest());

            guest = guestRepository.save(guest);

            ApiLogger.info(SERVICE_NAME, "Successfully added guest: " + guest.getId() + " to booking: " + bookingId);

            return ApplicationResponse.success(convertToGuestResponse(guest));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Booking not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error adding guest to booking: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to add guest", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Updates guest information.
     */
    public ApplicationResponse<BookingGuestResponse> updateGuest(Long guestId, 
                                                               BookingGuestRequest request, 
                                                               HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Updating guest: " + guestId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            BookingGuest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingGuest", guestId));

            // Check permissions
            if (!guest.getBooking().getGuest().getId().equals(currentUser.getId()) && 
                !guest.getBooking().getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to update this guest", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate booking is in a state where guests can be updated
            if (guest.getBooking().getStatus().name().equals("CANCELLED")) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Cannot update guests for cancelled booking", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Update fields if provided
            if (request.getFullName() != null) {
                guest.setFullName(request.getFullName());
            }
            if (request.getEmail() != null) {
                guest.setEmail(request.getEmail());
            }
            if (request.getPhone() != null) {
                guest.setPhone(request.getPhone());
            }
            if (request.getAge() != null) {
                guest.setAge(request.getAge());
            }
            if (request.getSpecialRequests() != null) {
                guest.setSpecialRequests(request.getSpecialRequests());
            }
            if (request.getDietaryRestrictions() != null) {
                guest.setDietaryRestrictions(request.getDietaryRestrictions());
            }
            if (request.getEmergencyContactName() != null) {
                guest.setEmergencyContactName(request.getEmergencyContactName());
            }
            if (request.getEmergencyContactPhone() != null) {
                guest.setEmergencyContactPhone(request.getEmergencyContactPhone());
            }
            if (request.getIsPrimaryGuest() != null) {
                // Check if trying to set as primary when another primary exists
                if (request.getIsPrimaryGuest()) {
                    List<BookingGuest> otherGuests = guestRepository.findByBookingId(guest.getBooking().getId())
                        .stream()
                        .filter(g -> !g.getId().equals(guestId))
                        .collect(Collectors.toList());

                    boolean hasOtherPrimary = otherGuests.stream()
                        .anyMatch(g -> g.getIsPrimaryGuest() != null && g.getIsPrimaryGuest());

                    if (hasOtherPrimary) {
                        return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                            "Another guest is already set as primary", ApiCode.VALIDATION_ERROR.getHttpStatus());
                    }
                }
                guest.setIsPrimaryGuest(request.getIsPrimaryGuest());
            }

            guest = guestRepository.save(guest);

            ApiLogger.info(SERVICE_NAME, "Successfully updated guest: " + guestId);

            return ApplicationResponse.success(convertToGuestResponse(guest));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Guest not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error updating guest: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to update guest", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Removes a guest from a booking.
     */
    public ApplicationResponse<Void> removeGuestFromBooking(Long guestId, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Removing guest: " + guestId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            BookingGuest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingGuest", guestId));

            // Check permissions
            if (!guest.getBooking().getGuest().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to remove this guest", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate booking is in a state where guests can be removed
            if (guest.getBooking().getStatus().name().equals("CANCELLED") || 
                guest.getBooking().getStatus().name().equals("COMPLETED")) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Cannot remove guests from cancelled or completed booking", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Don't allow removing primary guest if there are other guests
            if (guest.getIsPrimaryGuest() != null && guest.getIsPrimaryGuest()) {
                List<BookingGuest> otherGuests = guestRepository.findByBookingId(guest.getBooking().getId())
                    .stream()
                    .filter(g -> !g.getId().equals(guestId))
                    .collect(Collectors.toList());

                if (!otherGuests.isEmpty()) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Cannot remove primary guest while other guests exist", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
            }

            guestRepository.delete(guest);

            ApiLogger.info(SERVICE_NAME, "Successfully removed guest: " + guestId);

            return ApplicationResponse.success(null);

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Guest not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error removing guest: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to remove guest", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Retrieves all guests for a booking.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<BookingGuestResponse>> getBookingGuests(Long bookingId, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate booking exists and user has access
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            if (!booking.getGuest().getId().equals(currentUser.getId()) && 
                !booking.getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view guests for this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            List<BookingGuest> guests = guestRepository.findByBookingId(bookingId);

            List<BookingGuestResponse> guestResponses = guests.stream()
                .map(this::convertToGuestResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(guestResponses);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving booking guests: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve guests", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Retrieves a specific guest by ID.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<BookingGuestResponse> getGuestById(Long guestId, HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            BookingGuest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("BookingGuest", guestId));

            // Check permissions
            if (!guest.getBooking().getGuest().getId().equals(currentUser.getId()) && 
                !guest.getBooking().getHost().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to view this guest", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            return ApplicationResponse.success(convertToGuestResponse(guest));

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving guest: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve guest", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Bulk adds multiple guests to a booking.
     */
    public ApplicationResponse<List<BookingGuestResponse>> addMultipleGuests(Long bookingId, 
                                                                           List<BookingGuestRequest> requests, 
                                                                           HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Adding multiple guests to booking: " + bookingId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate booking exists
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

            // Check permissions
            if (!booking.getGuest().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to add guests to this booking", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Validate booking state
            if (booking.getStatus().name().equals("CANCELLED") || booking.getStatus().name().equals("COMPLETED")) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Cannot add guests to cancelled or completed booking", 
                    ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check capacity
            int currentGuestCount = booking.getGuestCount() != null ? booking.getGuestCount() : 0;
            
            List<BookingGuest> existingGuests = guestRepository.findByBookingId(bookingId);
            int totalNewGuests = existingGuests.size() + requests.size();

            Integer maxAdults = booking.getProperty().getMaxAdultsAccommodation();
            Integer maxChildren = booking.getProperty().getMaxChildrenAccommodation();
            Integer totalMaxGuests = (maxAdults != null ? maxAdults : 0) + (maxChildren != null ? maxChildren : 0);
            
            if (totalMaxGuests > 0 && (currentGuestCount + totalNewGuests) > totalMaxGuests) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Adding these guests would exceed property capacity", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Check for multiple primary guests
            boolean hasExistingPrimary = existingGuests.stream()
                .anyMatch(guest -> guest.getIsPrimaryGuest() != null && guest.getIsPrimaryGuest());

            long newPrimaryCount = requests.stream()
                .filter(req -> req.getIsPrimaryGuest() != null && req.getIsPrimaryGuest())
                .count();

            if (hasExistingPrimary && newPrimaryCount > 0) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Booking already has a primary guest", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            if (newPrimaryCount > 1) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                    "Cannot add multiple primary guests", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Create all guest records
            List<BookingGuest> newGuests = requests.stream()
                .map(request -> {
                    BookingGuest guest = new BookingGuest();
                    guest.setBooking(booking);
                    guest.setFullName(request.getFullName());
                    guest.setEmail(request.getEmail());
                    guest.setPhone(request.getPhone());
                    guest.setAge(request.getAge());
                    guest.setSpecialRequests(request.getSpecialRequests());
                    guest.setDietaryRestrictions(request.getDietaryRestrictions());
                    guest.setEmergencyContactName(request.getEmergencyContactName());
                    guest.setEmergencyContactPhone(request.getEmergencyContactPhone());
                    guest.setIsPrimaryGuest(request.getIsPrimaryGuest());
                    return guest;
                })
                .collect(Collectors.toList());

            List<BookingGuest> savedGuests = guestRepository.saveAll(newGuests);

            List<BookingGuestResponse> guestResponses = savedGuests.stream()
                .map(this::convertToGuestResponse)
                .collect(Collectors.toList());

            ApiLogger.info(SERVICE_NAME, "Successfully added " + savedGuests.size() + " guests to booking: " + bookingId);

            return ApplicationResponse.success(guestResponses);

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Booking not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error adding multiple guests: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to add guests", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    // Helper methods

    private BookingGuestResponse convertToGuestResponse(BookingGuest guest) {
        BookingGuestResponse response = new BookingGuestResponse();
        response.setId(guest.getId());
        response.setBookingId(guest.getBooking().getId());
        response.setFullName(guest.getFullName());
        response.setEmail(guest.getEmail());
        response.setPhone(guest.getPhone());
        response.setAge(guest.getAge());
        response.setSpecialRequests(guest.getSpecialRequests());
        response.setDietaryRestrictions(guest.getDietaryRestrictions());
        response.setEmergencyContactName(guest.getEmergencyContactName());
        response.setEmergencyContactPhone(guest.getEmergencyContactPhone());
        response.setIsPrimaryGuest(guest.getIsPrimaryGuest());
        response.setCreatedAt(guest.getCreatedAt());
        response.setUpdatedAt(guest.getUpdatedAt());

        return response;
    }
}
