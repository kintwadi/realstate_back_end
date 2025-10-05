# Booking System Endpoints Guide

This guide provides comprehensive documentation for all booking-related endpoints in the iMóvel API.

## Table of Contents

1. [Authentication](#authentication)
2. [Booking Management](#booking-management)
3. [Property Availability](#property-availability)
4. [Booking Payments](#booking-payments)
5. [Cancellation Policies](#cancellation-policies)
6. [Booking Guests](#booking-guests)
7. [Error Handling](#error-handling)
8. [Rate Limiting](#rate-limiting)
9. [Examples](#examples)

## Authentication

All booking endpoints require authentication via JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Booking Management

Base URL: `/api/v1/bookings`

### Create Booking

**POST** `/api/v1/bookings`

Creates a new booking for a property.

**Request Body:**
```json
{
  "propertyId": 123,
  "checkInDate": "2024-03-15",
  "checkOutDate": "2024-03-20",
  "adults": 2,
  "children": 1,
  "infants": 0,
  "totalAmount": 750.00,
  "serviceFee": 22.50,
  "cleaningFee": 50.00,
  "securityDeposit": 150.00,
  "specialRequests": "Late check-in requested",
  "paymentMethod": "STRIPE"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 456,
    "bookingReference": "BK-2024-001234",
    "propertyId": 123,
    "userId": 789,
    "status": "PENDING_CONFIRMATION",
    "checkInDate": "2024-03-15",
    "checkOutDate": "2024-03-20",
    "nights": 5,
    "adults": 2,
    "children": 1,
    "infants": 0,
    "totalAmount": 750.00,
    "serviceFee": 22.50,
    "cleaningFee": 50.00,
    "securityDeposit": 150.00,
    "specialRequests": "Late check-in requested",
    "paymentMethod": "STRIPE",
    "createdAt": "2024-02-15T10:30:00Z",
    "updatedAt": "2024-02-15T10:30:00Z"
  }
}
```

### Get Booking by ID

**GET** `/api/v1/bookings/{id}`

Retrieves a specific booking by its ID.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 456,
    "bookingReference": "BK-2024-001234",
    "propertyId": 123,
    "userId": 789,
    "status": "CONFIRMED",
    "checkInDate": "2024-03-15",
    "checkOutDate": "2024-03-20",
    "nights": 5,
    "adults": 2,
    "children": 1,
    "infants": 0,
    "totalAmount": 750.00,
    "serviceFee": 22.50,
    "cleaningFee": 50.00,
    "securityDeposit": 150.00,
    "specialRequests": "Late check-in requested",
    "paymentMethod": "STRIPE",
    "createdAt": "2024-02-15T10:30:00Z",
    "updatedAt": "2024-02-15T11:15:00Z"
  }
}
```

### Update Booking

**PUT** `/api/v1/bookings/{id}`

Updates an existing booking (only allowed for certain statuses).

**Request Body:**
```json
{
  "checkInDate": "2024-03-16",
  "checkOutDate": "2024-03-21",
  "adults": 3,
  "children": 1,
  "infants": 0,
  "specialRequests": "Updated: Late check-in and early breakfast"
}
```

### Cancel Booking

**POST** `/api/v1/bookings/{id}/cancel`

Cancels a booking and processes refund according to cancellation policy.

**Request Body:**
```json
{
  "reason": "Change of plans due to work commitment"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Booking cancelled successfully",
  "data": {
    "bookingId": 456,
    "status": "CANCELLED",
    "refundAmount": 600.00,
    "refundPercentage": 80.0,
    "cancellationFee": 150.00,
    "refundProcessingTime": "3-5 business days"
  }
}
```

### Confirm Booking

**POST** `/api/v1/bookings/{id}/confirm`

Confirms a pending booking (host only).

### Get User Bookings

**GET** `/api/v1/bookings/user`

Retrieves all bookings for the authenticated user.

**Query Parameters:**
- `status` (optional): Filter by booking status
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `sortBy` (optional): Sort field (default: createdAt)
- `sortDirection` (optional): Sort direction (ASC/DESC, default: DESC)

### Get Property Bookings

**GET** `/api/v1/bookings/property/{propertyId}`

Retrieves all bookings for a specific property (property owner only).

### Get Host Bookings

**GET** `/api/v1/bookings/host`

Retrieves all bookings for properties owned by the authenticated user.

### Get Booking Statistics

**GET** `/api/v1/bookings/statistics`

Retrieves booking statistics for the authenticated user.

**Response:**
```json
{
  "success": true,
  "data": {
    "totalBookings": 25,
    "confirmedBookings": 20,
    "cancelledBookings": 3,
    "completedBookings": 18,
    "totalRevenue": 15750.00,
    "averageBookingValue": 630.00,
    "occupancyRate": 78.5,
    "cancellationRate": 12.0
  }
}
```

### Get Upcoming Check-ins

**GET** `/api/v1/bookings/check-ins/upcoming`

Retrieves upcoming check-ins for the authenticated host.

### Get Upcoming Check-outs

**GET** `/api/v1/bookings/check-outs/upcoming`

Retrieves upcoming check-outs for the authenticated host.

## Property Availability

Base URL: `/api/v1/availability`

### Check Availability

**GET** `/api/v1/availability/{propertyId}/check`

Checks if a property is available for specific dates.

**Query Parameters:**
- `checkInDate`: Check-in date (YYYY-MM-DD)
- `checkOutDate`: Check-out date (YYYY-MM-DD)
- `guests`: Number of guests

**Response:**
```json
{
  "success": true,
  "data": {
    "available": true,
    "propertyId": 123,
    "checkInDate": "2024-03-15",
    "checkOutDate": "2024-03-20",
    "nights": 5,
    "basePrice": 120.00,
    "totalPrice": 600.00,
    "availableRooms": 1,
    "restrictions": []
  }
}
```

### Set Availability

**POST** `/api/v1/availability/{propertyId}`

Sets availability for specific dates (property owner only).

**Request Body:**
```json
{
  "date": "2024-03-15",
  "available": true,
  "price": 120.00,
  "minimumStay": 2,
  "maximumStay": 14
}
```

### Update Availability

**PUT** `/api/v1/availability/{propertyId}/{date}`

Updates availability for a specific date.

### Bulk Update Availability

**POST** `/api/v1/availability/{propertyId}/bulk`

Updates availability for multiple dates.

**Request Body:**
```json
{
  "updates": [
    {
      "date": "2024-03-15",
      "available": true,
      "price": 120.00
    },
    {
      "date": "2024-03-16",
      "available": true,
      "price": 125.00
    }
  ]
}
```

### Block Dates

**POST** `/api/v1/availability/{propertyId}/block`

Blocks specific dates for booking.

**Request Body:**
```json
{
  "startDate": "2024-03-15",
  "endDate": "2024-03-20",
  "reason": "Property maintenance"
}
```

### Release Dates

**POST** `/api/v1/availability/{propertyId}/release`

Releases previously blocked dates.

### Get Availability Calendar

**GET** `/api/v1/availability/{propertyId}/calendar`

Retrieves availability calendar for a property.

**Query Parameters:**
- `startDate`: Start date (YYYY-MM-DD)
- `endDate`: End date (YYYY-MM-DD)

### Get Blocked Dates

**GET** `/api/v1/availability/{propertyId}/blocked`

Retrieves all blocked dates for a property.

### Get Available Dates

**GET** `/api/v1/availability/{propertyId}/available`

Retrieves all available dates for a property.

## Booking Payments

Base URL: `/api/v1/booking-payments`

### Process Payment

**POST** `/api/v1/booking-payments/process`

Processes a payment for a booking.

**Request Body:**
```json
{
  "bookingId": 456,
  "amount": 750.00,
  "paymentMethod": "STRIPE",
  "paymentToken": "tok_1234567890",
  "savePaymentMethod": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "paymentId": 789,
    "bookingId": 456,
    "amount": 750.00,
    "status": "COMPLETED",
    "paymentMethod": "STRIPE",
    "transactionId": "pi_1234567890",
    "processedAt": "2024-02-15T11:15:00Z"
  }
}
```

### Process Refund

**POST** `/api/v1/booking-payments/refund`

Processes a refund for a booking payment.

**Request Body:**
```json
{
  "bookingId": 456,
  "amount": 600.00,
  "reason": "Booking cancellation"
}
```

### Get Payment by ID

**GET** `/api/v1/booking-payments/{id}`

Retrieves a specific payment by its ID.

### Get Payments by Booking

**GET** `/api/v1/booking-payments/booking/{bookingId}`

Retrieves all payments for a specific booking.

### Get User Payment History

**GET** `/api/v1/booking-payments/user/history`

Retrieves payment history for the authenticated user.

**Query Parameters:**
- `status` (optional): Filter by payment status
- `startDate` (optional): Start date filter
- `endDate` (optional): End date filter
- `page` (optional): Page number
- `size` (optional): Page size

### Get Payment Summary

**GET** `/api/v1/booking-payments/booking/{bookingId}/summary`

Retrieves payment summary for a booking.

### Get Pending Payments

**GET** `/api/v1/booking-payments/user/pending`

Retrieves pending payments for the authenticated user.

### Get Overdue Payments

**GET** `/api/v1/booking-payments/user/overdue`

Retrieves overdue payments for the authenticated user.

### Verify Payment

**POST** `/api/v1/booking-payments/{id}/verify`

Verifies a payment status with the payment provider.

### Cancel Payment

**POST** `/api/v1/booking-payments/{id}/cancel`

Cancels a pending payment.

## Cancellation Policies

Base URL: `/api/v1/cancellation-policies`

### Create Policy

**POST** `/api/v1/cancellation-policies`

Creates a new cancellation policy for a property.

**Request Body:**
```json
{
  "propertyId": 123,
  "policyType": "MODERATE",
  "refundPercentage": 75.0,
  "daysBeforeCheckIn": 5,
  "description": "75% refund if cancelled 5+ days before check-in"
}
```

### Get Policy by ID

**GET** `/api/v1/cancellation-policies/{id}`

Retrieves a specific cancellation policy.

### Get Policies by Property

**GET** `/api/v1/cancellation-policies/property/{propertyId}`

Retrieves all cancellation policies for a property.

### Get User Policies

**GET** `/api/v1/cancellation-policies/user`

Retrieves all cancellation policies for properties owned by the authenticated user.

### Update Policy

**PUT** `/api/v1/cancellation-policies/{id}`

Updates an existing cancellation policy.

### Delete Policy

**DELETE** `/api/v1/cancellation-policies/{id}`

Deletes a cancellation policy.

### Change Policy Status

**POST** `/api/v1/cancellation-policies/{id}/status`

Changes the status of a cancellation policy.

**Request Body:**
```json
{
  "status": "ACTIVE"
}
```

### Calculate Refund

**POST** `/api/v1/cancellation-policies/calculate-refund`

Calculates refund amount based on cancellation policy.

**Request Body:**
```json
{
  "bookingId": 456,
  "cancellationDate": "2024-03-10T10:00:00Z"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "originalAmount": 750.00,
    "refundAmount": 562.50,
    "refundPercentage": 75.0,
    "cancellationFee": 187.50,
    "policyType": "MODERATE",
    "daysBeforeCheckIn": 5
  }
}
```

### Get Default Policies

**GET** `/api/v1/cancellation-policies/defaults`

Retrieves default cancellation policies.

### Validate Policy

**POST** `/api/v1/cancellation-policies/validate`

Validates a cancellation policy configuration.

### Get Policy Statistics

**GET** `/api/v1/cancellation-policies/statistics`

Retrieves statistics about cancellation policies.

## Booking Guests

Base URL: `/api/v1/booking-guests`

### Add Guest

**POST** `/api/v1/booking-guests`

Adds a guest to a booking.

**Request Body:**
```json
{
  "bookingId": 456,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "age": 35,
  "isPrimary": false
}
```

### Get Guest by ID

**GET** `/api/v1/booking-guests/{id}`

Retrieves a specific guest by ID.

### Get Guests by Booking

**GET** `/api/v1/booking-guests/booking/{bookingId}`

Retrieves all guests for a specific booking.

### Get Primary Guest

**GET** `/api/v1/booking-guests/booking/{bookingId}/primary`

Retrieves the primary guest for a booking.

### Update Guest

**PUT** `/api/v1/booking-guests/{id}`

Updates guest information.

### Remove Guest

**DELETE** `/api/v1/booking-guests/{id}`

Removes a guest from a booking.

### Add Multiple Guests

**POST** `/api/v1/booking-guests/bulk`

Adds multiple guests to a booking.

**Request Body:**
```json
{
  "bookingId": 456,
  "guests": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "age": 35
    },
    {
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane.doe@example.com",
      "age": 32
    }
  ]
}
```

### Remove Multiple Guests

**DELETE** `/api/v1/booking-guests/bulk`

Removes multiple guests from a booking.

### Set Primary Guest

**POST** `/api/v1/booking-guests/{id}/primary`

Sets a guest as the primary guest for a booking.

### Get Guest Count

**GET** `/api/v1/booking-guests/booking/{bookingId}/count`

Retrieves the guest count for a booking.

### Validate Guest Capacity

**POST** `/api/v1/booking-guests/validate-capacity`

Validates if guest count is within property capacity.

**Request Body:**
```json
{
  "bookingId": 456,
  "guestCount": 4
}
```

## Error Handling

All endpoints return standardized error responses:

```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "BOOKING_NOT_FOUND",
  "timestamp": "2024-02-15T10:30:00Z",
  "path": "/api/v1/bookings/999"
}
```

### Common Error Codes

- `BOOKING_NOT_FOUND`: Booking does not exist
- `UNAUTHORIZED_ACCESS`: User not authorized to access resource
- `INVALID_BOOKING_STATUS`: Operation not allowed for current booking status
- `PROPERTY_NOT_AVAILABLE`: Property not available for selected dates
- `PAYMENT_REQUIRED`: Payment required to proceed
- `GUEST_LIMIT_EXCEEDED`: Too many guests for property
- `INVALID_DATE_RANGE`: Invalid check-in/check-out dates
- `CANCELLATION_NOT_ALLOWED`: Cancellation not permitted
- `INSUFFICIENT_FUNDS`: Payment failed due to insufficient funds

## Rate Limiting

The following rate limits apply:

- **Booking Creation**: 10 requests per hour per user
- **Payment Processing**: 5 requests per hour per user
- **Availability Checks**: 100 requests per minute per user
- **Cancellations**: 3 requests per day per user
- **Guest Modifications**: 20 requests per hour per user

Rate limit headers are included in responses:

```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-RateLimit-Reset: 1708012800
```

## Examples

### Complete Booking Flow

1. **Check Availability**
```bash
GET /api/v1/availability/123/check?checkInDate=2024-03-15&checkOutDate=2024-03-20&guests=3
```

2. **Create Booking**
```bash
POST /api/v1/bookings
{
  "propertyId": 123,
  "checkInDate": "2024-03-15",
  "checkOutDate": "2024-03-20",
  "adults": 2,
  "children": 1,
  "totalAmount": 750.00
}
```

3. **Add Guests**
```bash
POST /api/v1/booking-guests/bulk
{
  "bookingId": 456,
  "guests": [...]
}
```

4. **Process Payment**
```bash
POST /api/v1/booking-payments/process
{
  "bookingId": 456,
  "amount": 750.00,
  "paymentMethod": "STRIPE",
  "paymentToken": "tok_1234567890"
}
```

### Cancellation Flow

1. **Calculate Refund**
```bash
POST /api/v1/cancellation-policies/calculate-refund
{
  "bookingId": 456,
  "cancellationDate": "2024-03-10T10:00:00Z"
}
```

2. **Cancel Booking**
```bash
POST /api/v1/bookings/456/cancel
{
  "reason": "Change of plans"
}
```

### Host Management Flow

1. **Get Host Bookings**
```bash
GET /api/v1/bookings/host?status=PENDING_CONFIRMATION
```

2. **Confirm Booking**
```bash
POST /api/v1/bookings/456/confirm
```

3. **Set Availability**
```bash
POST /api/v1/availability/123/bulk
{
  "updates": [...]
}
```

## Best Practices

1. **Always check availability** before creating a booking
2. **Validate guest information** before adding guests
3. **Handle payment failures** gracefully with retry mechanisms
4. **Implement proper error handling** for all API calls
5. **Use pagination** for list endpoints with large datasets
6. **Cache availability data** to improve performance
7. **Implement idempotency** for payment operations
8. **Monitor rate limits** and implement backoff strategies

## Support

For additional support or questions about the booking API, please contact the development team or refer to the main API documentation.