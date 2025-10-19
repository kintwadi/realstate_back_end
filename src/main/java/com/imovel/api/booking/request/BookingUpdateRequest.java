package com.imovel.api.booking.request;

import com.imovel.api.booking.model.enums.BookingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class BookingUpdateRequest {

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private String specialRequests;
    private int totalNights;
    private BookingStatus status;
    private String cancellationReason;
    private String hostNotes;
    private List<BookingGuestRequest> additionalGuests;
    private String internalNotes;

    // Constructors
    public BookingUpdateRequest() {}

    // Getters and Setters
    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(Integer numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getHostNotes() {
        return hostNotes;
    }

    public void setHostNotes(String hostNotes) {
        this.hostNotes = hostNotes;
    }

    public List<BookingGuestRequest> getAdditionalGuests() {
        return additionalGuests;
    }

    public int getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(int totalNights) {
        this.totalNights = totalNights;
    }

    public void setAdditionalGuests(List<BookingGuestRequest> additionalGuests) {
        this.additionalGuests = additionalGuests;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    // Validation methods
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Allow partial updates
        }
        return checkOutDate.isAfter(checkInDate);
    }

    @AssertTrue(message = "Cancellation reason is required when status is CANCELLED")
    public boolean isCancellationReasonValid() {
        if (status == BookingStatus.CANCELLED) {
            return cancellationReason != null && !cancellationReason.trim().isEmpty();
        }
        return true;
    }

    // Helper methods
    public boolean hasDateChanges() {
        return checkInDate != null || checkOutDate != null;
    }

    public boolean hasGuestCountChanges() {
        return numberOfAdults != null || numberOfChildren != null;
    }

    public boolean hasStatusChange() {
        return status != null;
    }

    public int getTotalGuests() {
        return (numberOfAdults != null ? numberOfAdults : 0) + 
               (numberOfChildren != null ? numberOfChildren : 0);
    }
}