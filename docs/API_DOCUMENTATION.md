# Imovel API Documentation

This document provides comprehensive documentation for all API endpoints in the Imovel application. This documentation is intended for frontend developers to understand the available endpoints, request formats, and expected responses.

## Base URL
```
http://localhost:8080/imovel
```

## Authentication
Most endpoints require JWT authentication. Include the access token in the Authorization header:
```
Authorization: Bearer <access_token>
```

---

## 🔐 Authentication Endpoints

### 1. Register User
**POST** `/api/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "name": "API Doc User",
  "email": "apidoc@example.com",
  "password": "password123"
}
```

**Successful Response:**
```json
{
  "success": true,
  "data": {
    "id": 12,
    "name": "API Doc User",
    "email": "apidoc@example.com",
    "phone": "",
    "avatar": "",
    "socialLinks": []
  },
  "message": "User registered successfully",
  "error": null,
  "timestamp": "2025-10-03T16:29:27.994835200Z"
}
```

### 2. Login User
**POST** `/api/auth/login`

Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "apidoc@example.com",
  "password": "password123"
}
```

**Successful Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:29:40.185378500Z"
}
```

### 3. Refresh Token
**POST** `/api/auth/refresh-token`

Get new access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Successful Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:30:11.686754100Z"
}
```

### 4. Logout User
**POST** `/api/auth/logout`

Logout user and invalidate tokens.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Successful Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Logged out successfully",
  "error": null,
  "timestamp": "2025-10-03T16:30:00.151734900Z"
}
```

### 5. Debug User (Development Only)
**GET** `/api/auth/debug/{email}`

Get user debug information.

### 6. Logout All Sessions
**POST** `/api/auth/logout-all`

Logout user from all sessions.

### 7. Test Login (Development Only)
**POST** `/api/auth/test-login`

Test login endpoint for development.

### 8. Forgot Password
**POST** `/api/auth/forgot-password`

Initiate password reset process.

### 9. Reset Password
**POST** `/api/auth/reset-password`

Reset user password with token.

### 10. Change Password
**POST** `/api/auth/change-password`

Change user password (requires authentication).

---

## 👤 User Management Endpoints

### 1. Get Current User
**GET** `/api/users/me`

Get current authenticated user information.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Successful Response:**
```json
{
  "success": true,
  "data": {
    "id": 9,
    "name": "Test User",
    "email": "newuser@example.com",
    "phone": "",
    "avatar": "",
    "socialLinks": []
  },
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:30:23.127219500Z"
}
```

### 2. Get All Users
**GET** `/api/users`

Get list of all users (requires authentication).

**Headers:**
```
Authorization: Bearer <access_token>
```

**Successful Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Test User",
      "email": "testuser@example.com",
      "phone": "1234567890",
      "avatar": "",
      "socialLinks": []
    },
    {
      "id": 2,
      "name": "Test User 2",
      "email": "testuser2@example.com",
      "phone": "",
      "avatar": "",
      "socialLinks": []
    }
  ],
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:30:37.421402700Z"
}
```

### 3. Update Current User
**PUT** `/api/users/me`

Update current user information.

### 4. Delete Current User
**DELETE** `/api/users/me`

Delete current user account.

---

## 🏠 Property Management Endpoints

### 1. Create Property
**POST** `/api/properties`

Create a new property listing.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "API Test Property",
  "description": "A test property for API documentation",
  "price": 250000,
  "address": "123 Test Street",
  "city": "Test City",
  "state": "Test State",
  "zipCode": "12345",
  "bedrooms": 3,
  "bathrooms": 2,
  "area": 1500,
  "propertyType": "HOUSE",
  "listingType": "SALE"
}
```

**Note:** Property creation may require additional setup or permissions.

### 2. Get All Properties
**GET** `/api/properties`

Get paginated list of all properties.

**Successful Response:**
```json
{
  "success": true,
  "data": {
    "content": [],
    "pageable": {},
    "last": true,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "size": 10,
    "number": 0,
    "sort": {},
    "numberOfElements": 0,
    "empty": true
  },
  "message": "Properties retrieved successfully.",
  "error": null,
  "timestamp": "2025-10-03T16:31:10.524921Z"
}
```

### 3. Get Property by ID
**GET** `/api/properties/{id}`

Get specific property details.

### 4. Update Property
**PUT** `/api/properties/{id}`

Update existing property.

### 5. Delete Property
**DELETE** `/api/properties/{id}`

Delete property listing.

---

## 🔍 Search Endpoints

### 1. Search Properties
**GET** `/api/search?query={query}`

Search for properties.

**Query Parameters:**
- `query`: Search term

**Successful Response:**
```json
{
  "success": true,
  "data": null,
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:31:22.882971500Z"
}
```

### 2. Get Search Suggestions
**GET** `/api/search/suggestions?query={query}`

Get search suggestions.

**Query Parameters:**
- `query`: Search term

**Note:** This endpoint may return 400 Bad Request for certain queries.

---

## ❤️ Wishlist Endpoints

### 1. Get User Wishlist
**GET** `/api/wishlist`

Get current user's wishlist.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Successful Response:**
```json
{
  "success": true,
  "data": null,
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:31:46.488386300Z"
}
```

### 2. Add Property to Wishlist
**POST** `/api/wishlist/{propertyId}`

Add property to user's wishlist.

### 3. Remove Property from Wishlist
**DELETE** `/api/wishlist/{propertyId}`

Remove property from user's wishlist.

### 4. Clear Wishlist
**DELETE** `/api/wishlist`

Clear all items from user's wishlist.

---

## ⭐ Review Endpoints

### 1. Get Property Reviews
**GET** `/api/properties/{propertyId}/reviews`

Get reviews for a specific property.

### 2. Create Property Review
**POST** `/api/properties/{propertyId}/reviews`

Create a review for a property.

### 3. Delete Review
**DELETE** `/api/reviews/{id}`

Delete a specific review.

---

## 💳 Subscription Plan Endpoints

### 1. Get All Subscription Plans
**GET** `/api/subscription-plans`

Get all available subscription plans.

**Successful Response:**
```json
{
  "success": true,
  "data": {},
  "message": "",
  "error": null,
  "timestamp": "2025-10-03T16:31:56.255904800Z"
}
```

### 2. Create Subscription Plan
**POST** `/api/subscription-plans`

Create new subscription plan.

### 3. Get Subscription Plan by ID
**GET** `/api/subscription-plans/{id}`

Get specific subscription plan.

### 4. Update Subscription Plan
**PUT** `/api/subscription-plans/{id}`

Update existing subscription plan.

### 5. Delete Subscription Plan
**DELETE** `/api/subscription-plans/{id}`

Delete subscription plan.

---

## 📱 Subscription Management Endpoints

### 1. Subscribe to Plan
**POST** `/api/subscriptions/subscribe`

Subscribe user to a plan.

### 2. Get User Subscriptions
**GET** `/api/subscriptions/user/{userId}`

Get subscriptions for a user.

### 3. Cancel Subscription
**POST** `/api/subscriptions/{subscriptionId}/cancel`

Cancel a subscription.

### 4. Change Subscription Plan
**POST** `/api/subscriptions/{subscriptionId}/change-plan`

Change subscription plan.

---

## 💰 Payment Endpoints

### 1. Process Payment
**POST** `/api/payments/process`

Process a payment.

### 2. Get Payment by ID
**GET** `/api/payments/{paymentId}`

Get payment details.

### 3. Get User Payments
**GET** `/api/payments/user/{userId}`

Get payments for a user.

### 4. Refund Payment
**POST** `/api/payments/refund`

Process payment refund.

### 5. Cancel Payment
**POST** `/api/payments/{paymentId}/cancel`

Cancel a payment.

### 6. Verify Payment
**POST** `/api/payments/{paymentId}/verify`

Verify payment status.

### 7. Get Payment Statistics
**GET** `/api/payments/statistics/{userId}`

Get payment statistics for user.

### 8. Debug Payment Session
**GET** `/api/payments/debug-session`

Debug payment session (development only).

---

## 🔧 Role Management Endpoints

### 1. Get All Roles
**GET** `/api/roles`

Get all available roles.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Successful Response:**
```json
{
  "success": true,
  "data": [
    {
      "roleId": 1,
      "roleName": "TENANT",
      "description": "",
      "permissions": [],
      "userCount": 11
    },
    {
      "roleId": 2,
      "roleName": "ADMIN",
      "description": "System default admin role",
      "permissions": [],
      "userCount": 1
    },
    {
      "roleId": 3,
      "roleName": "AGENT",
      "description": "System default agent role",
      "permissions": [],
      "userCount": 0
    }
  ],
  "message": "list of roles found successfully",
  "error": null,
  "timestamp": "2025-10-03T16:32:19.019990700Z"
}
```

### 2. Create Role
**POST** `/api/roles`

Create new role.

### 3. Get Role by ID
**GET** `/api/roles/{id}`

Get specific role details.

### 4. Get Role by Name
**GET** `/api/roles/name/{roleName}`

Get role by name.

### 5. Update Role
**PUT** `/api/roles/{id}`

Update existing role.

### 6. Delete Role
**DELETE** `/api/roles/{id}`

Delete role.

### 7. Initialize Default Roles
**POST** `/api/roles/initialize`

Initialize default system roles.

### 8. Assign Role to User
**POST** `/api/roles/assign-to-user`

Assign role to user.

### 9. Remove Role from User
**DELETE** `/api/roles/remove-from-user`

Remove role from user.

---

## 🔑 Permission Management Endpoints

### 1. Create Permission
**POST** `/api/permissions`

Create new permission.

### 2. Get Permission by Name
**GET** `/api/permissions/{permissionName}`

Get permission by name.

### 3. Get All Permissions
**GET** `/api/permissions`

Get all permissions.

### 4. Initialize Default Permissions
**POST** `/api/permissions/initialize`

Initialize default system permissions.

---

## 🔗 Role-Permission Management Endpoints

### 1. Assign Permission to Role
**POST** `/api/role-permissions/assign`

Assign permission to role.

### 2. Get All Role-Permission Mappings
**GET** `/api/role-permissions`

Get all role-permission mappings.

### 3. Assign Permission by Name
**POST** `/api/role-permissions/assign-by-name`

Assign permission to role by name.

### 4. Remove Permission from Role
**DELETE** `/api/role-permissions/remove`

Remove permission from role.

### 5. Get Permissions by Role
**GET** `/api/role-permissions/role/{roleId}`

Get permissions for specific role.

### 6. Get Roles by Permission
**GET** `/api/role-permissions/permission/{permissionId}`

Get roles with specific permission.

### 7. Check Role Permission
**GET** `/api/role-permissions/check`

Check if role has permission.

### 8. Update Role Permissions
**PUT** `/api/role-permissions/update`

Update role permissions.

### 9. Remove All Permissions from Role
**DELETE** `/api/role-permissions/remove/{roleId}`

Remove all permissions from role.

### 10. Count Roles with Permission
**GET** `/api/role-permissions/count/{permissionId}`

Count roles with specific permission.

### 11. Initialize Default Role-Permissions
**POST** `/api/role-permissions/initialize-defaults`

Initialize default role-permission mappings.

### 12. Get User Permissions
**GET** `/api/role-permissions/user-permissions`

Get permissions for current user.

---

## 📁 Media Management Endpoints

### 1. Upload Media
**POST** `/api/media/upload`

Upload media files.

### 2. Get Property Media
**GET** `/api/media/property/{propertyId}`

Get media for specific property.

### 3. Get Specific Media
**GET** `/api/media/{propertyId}/{name}`

Get specific media file.

### 4. Delete Media
**DELETE** `/api/media/{propertyId}/{name}`

Delete specific media file.

---

## 🔔 Webhook Endpoints

### 1. Stripe Webhook Events
**POST** `/api/webhooks/events`

Handle Stripe webhook events.

---

## 🏥 Health Check Endpoints

### 1. Payment Health Check
**GET** `/api/health/payment`

Check payment system health.

**Note:** This endpoint may return 503 Service Unavailable if payment services are not properly configured.

---

## 📝 Response Format

All API responses follow this standard format:

```json
{
  "success": boolean,
  "data": object | array | null,
  "message": string,
  "error": object | null,
  "timestamp": string (ISO 8601)
}
```

### Error Response Example:
```json
{
  "success": false,
  "data": null,
  "message": "",
  "error": {
    "code": 1000,
    "message": "Error description",
    "timestamp": "2025-10-03T16:30:48.996762200Z",
    "status": "INTERNAL_SERVER_ERROR"
  },
  "timestamp": "2025-10-03T16:30:48.996762200Z"
}
```

---

## 🔒 Authentication Notes

1. **Access Token**: Short-lived token (15 minutes) for API access
2. **Refresh Token**: Long-lived token (7 days) for getting new access tokens
3. **Token Storage**: Store tokens securely in your frontend application
4. **Token Refresh**: Implement automatic token refresh using the refresh token endpoint

---

## 📋 Development Notes

1. **Base URL**: Update the base URL for production deployment
2. **CORS**: Ensure CORS is properly configured for your frontend domain
3. **Error Handling**: Implement proper error handling for all API calls
4. **Loading States**: Show loading indicators during API calls
5. **Pagination**: Handle pagination for endpoints that return lists
6. **File Uploads**: Use proper form data for media upload endpoints

---

## 🚀 Getting Started

1. Start the application with PostgreSQL profile
2. Register a new user account
3. Login to get JWT tokens
4. Use the access token for authenticated endpoints
5. Implement token refresh logic for seamless user experience

This documentation covers all tested endpoints in the Imovel API. Some endpoints may require additional configuration or permissions to function properly in a production environment.