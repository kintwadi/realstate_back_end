package com.imovel.api.booking.response;

import com.imovel.api.booking.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {

    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private Long guestId;
    private String guestName;
    private String guestEmail;
    private Long hostId;
    private String hostName;
    private String hostEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalNights;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private Integer totalGuests;
    private BigDecimal baseAmount;
    private BigDecimal cleaningFee;
    private BigDecimal serviceFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal nightlyRate;
    private BookingStatus status;
    private String specialRequests;
    private String cancellationReason;
    private LocalDateTime cancellationDate;
    private String hostNotes;
    private String confirmationCode;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookingGuestResponse> guests;
    private List<BookingPaymentResponse> payments;
    private CancellationPolicyResponse cancellationPolicy;
    private PropertyAvailabilityResponse propertyAvailability;

    // Constructors
    public BookingResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
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

    public Integer getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(Integer totalNights) {
        this.totalNights = totalNights;
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

    public Integer getTotalGuests() {
        return totalGuests;
    }

    public void setTotalGuests(Integer totalGuests) {
        this.totalGuests = totalGuests;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getCleaningFee() {
        return cleaningFee;
    }

    public void setCleaningFee(BigDecimal cleaningFee) {
        this.cleaningFee = cleaningFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(BigDecimal nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public String getHostNotes() {
        return hostNotes;
    }

    public void setHostNotes(String hostNotes) {
        this.hostNotes = hostNotes;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<BookingGuestResponse> getGuests() {
        return guests;
    }

    public void setGuests(List<BookingGuestResponse> guests) {
        this.guests = guests;
    }

    public List<BookingPaymentResponse> getPayments() {
        return payments;
    }

    public void setPayments(List<BookingPaymentResponse> payments) {
        this.payments = payments;
    }

    public CancellationPolicyResponse getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(CancellationPolicyResponse cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    public PropertyAvailabilityResponse getPropertyAvailability() {
        return propertyAvailability;
    }

    public void setPropertyAvailability(PropertyAvailabilityResponse propertyAvailability) {
        this.propertyAvailability = propertyAvailability;
    }

    // Helper methods
    public boolean isCancellable() {
        return status != null && (status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED);
    }

    public boolean isActive() {
        return status != null && status.isActive();
    }

    public boolean isFinalized() {
        return status != null && status.isFinalized();
    }

    public long getDaysUntilCheckIn() {
        if (checkInDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
    }

    public boolean isUpcoming() {
        return checkInDate != null && checkInDate.isAfter(LocalDate.now());
    }

    public boolean isCurrentStay() {
        LocalDate today = LocalDate.now();
        return checkInDate != null && checkOutDate != null && 
               !checkInDate.isAfter(today) && checkOutDate.isAfter(today);
    }

    public boolean isPastStay() {
        return checkOutDate != null && checkOutDate.isBefore(LocalDate.now());
    }
}
