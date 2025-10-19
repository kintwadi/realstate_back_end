package com.imovel.api.booking.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingCreateRequest {


    private Long propertyId;
    private Long guestId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private String specialRequests;

    private List<BookingGuestRequest> additionalGuests;

    private BigDecimal expectedTotalAmount;

    private String promoCode;

    private Boolean agreeToTerms;

    private Boolean agreeToPrivacyPolicy;

    private String notes;

    // Constructors
    public BookingCreateRequest() {}

    // Getters and Setters
    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

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

    public List<BookingGuestRequest> getAdditionalGuests() {
        return additionalGuests;
    }

    public void setAdditionalGuests(List<BookingGuestRequest> additionalGuests) {
        this.additionalGuests = additionalGuests;
    }

    public BigDecimal getExpectedTotalAmount() {
        return expectedTotalAmount;
    }

    public void setExpectedTotalAmount(BigDecimal expectedTotalAmount) {
        this.expectedTotalAmount = expectedTotalAmount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public Boolean getAgreeToTerms() {
        return agreeToTerms;
    }

    public void setAgreeToTerms(Boolean agreeToTerms) {
        this.agreeToTerms = agreeToTerms;
    }

    public Boolean getAgreeToPrivacyPolicy() {
        return agreeToPrivacyPolicy;
    }

    public void setAgreeToPrivacyPolicy(Boolean agreeToPrivacyPolicy) {
        this.agreeToPrivacyPolicy = agreeToPrivacyPolicy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Validation methods
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkOutDate.isAfter(checkInDate);
    }

    @AssertTrue(message = "You must agree to terms and conditions")
    public boolean isTermsAgreed() {
        return agreeToTerms != null && agreeToTerms;
    }

    @AssertTrue(message = "You must agree to privacy policy")
    public boolean isPrivacyPolicyAgreed() {
        return agreeToPrivacyPolicy != null && agreeToPrivacyPolicy;
    }
}
