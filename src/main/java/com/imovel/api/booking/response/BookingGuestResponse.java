package com.imovel.api.booking.response;

import java.time.LocalDateTime;

public class BookingGuestResponse {

    private Long id;
    private Long bookingId;
    private String fullName;
    private String email;
    private String phone;
    private Integer age;
    private String specialRequests;
    private String dietaryRestrictions;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean isPrimaryGuest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BookingGuestResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public Boolean getIsPrimaryGuest() {
        return isPrimaryGuest;
    }

    public void setIsPrimaryGuest(Boolean isPrimaryGuest) {
        this.isPrimaryGuest = isPrimaryGuest;
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

    // Helper methods
    public boolean hasSpecialRequests() {
        return specialRequests != null && !specialRequests.trim().isEmpty();
    }

    public boolean hasDietaryRestrictions() {
        return dietaryRestrictions != null && !dietaryRestrictions.trim().isEmpty();
    }

    public boolean hasEmergencyContact() {
        return emergencyContactName != null && !emergencyContactName.trim().isEmpty() &&
               emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
    }

    public boolean isAdult() {
        return age != null && age >= 18;
    }

    public boolean isChild() {
        return age != null && age < 18;
    }

    public String getAgeGroup() {
        if (age == null) {
            return "Unknown";
        }
        if (age < 2) {
            return "Infant";
        } else if (age < 12) {
            return "Child";
        } else if (age < 18) {
            return "Teen";
        } else if (age < 65) {
            return "Adult";
        } else {
            return "Senior";
        }
    }
}
