package com.imovel.api.booking.model.enums;

public enum BookingStatus {
    PENDING("Pending confirmation"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked in"),
    CHECKED_OUT("Checked out"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No show"),
    REFUNDED("Refunded");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == CONFIRMED || this == CHECKED_IN;
    }

    public boolean isFinalized() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW || this == REFUNDED;
    }

    public boolean canTransitionTo(BookingStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED:
                return newStatus == CHECKED_IN || newStatus == CANCELLED || newStatus == NO_SHOW;
            case CHECKED_IN:
                return newStatus == CHECKED_OUT || newStatus == CANCELLED;
            case CHECKED_OUT:
                return newStatus == COMPLETED;
            case COMPLETED:
                return newStatus == REFUNDED;
            case CANCELLED:
            case NO_SHOW:
            case REFUNDED:
                return false; // Final states
            default:
                return false;
        }
    }
}
