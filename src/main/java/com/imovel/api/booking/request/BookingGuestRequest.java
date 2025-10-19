package com.imovel.api.booking.request;

import jakarta.validation.constraints.*;

public class BookingGuestRequest {

    private Long bookingId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private Integer age;
    private String specialRequests;
    private String dietaryRestrictions;
    private String emergencyContactName;
    private String emergencyContactPhone;

    private Boolean isPrimaryGuest = false;

    // Constructors
    public BookingGuestRequest() {}

    public BookingGuestRequest(String firstName, String lastName, String email, String phone, Integer age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
        this.phone = phone;
        this.age = age;
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    // Validation methods
    @AssertTrue(message = "Emergency contact phone is required when emergency contact name is provided")
    public boolean isEmergencyContactValid() {
        if (emergencyContactName != null && !emergencyContactName.trim().isEmpty()) {
            return emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
        }
        return true;
    }
}
