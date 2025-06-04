### API Response and Exception Handling

#### Fields Explanation

| Field       | Type       | Description                                      |
|-------------|------------|--------------------------------------------------|
| success     | boolean    | Indicates if the request was successful          |
| data        | generic T  | The response payload (null for errors)           |
| error       | object     | Error details (null for successes) containing:   |
| ∟ code      | string     | Machine-readable error code                      |
| ∟ message   | string     | Human-readable error message                     |
| ∟ timestamp | string     | When the error occurred (ISO-8601)               |
| timestamp   | string     | When the response was generated (ISO-8601)       |

#### Custom Exceptions

````properties
ApplicationException (abstract)
├── ResourceNotFoundException
├── AuthenticationException
├── AuthorizationException
├── ValidationException
├── ConflictException
├── TokenRefreshException
└── BusinessRuleException
````

##### Rules: 
        -- Services --
            Every public methods must return standardResponse
````java

@Service
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("EMAIL_EXISTS", "Email already registered");
        }
        
        if (!isValidPassword(userDto.getPassword())) {
            throw new ValidationException(
                "INVALID_PASSWORD",
                "Password doesn't meet requirements",
                List.of(
                    "Must be at least 8 characters",
                    "Must contain at least one number"
                )
            );
        }
        
        User user = mapToEntity(userDto);
        return userRepository.save(user);
    }
}
````
##### Rules: 
        -- Controllers --
            Every public methods must return standardResponse via ResponseEntity
````java

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;


    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(StandardResponse.success(user));
    }

    @PostMapping
    public ResponseEntity<StandardResponse<User>> createUser(@Valid @RequestBody UserDto userDto) {
        User createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(createdUser));
    }
}
````

#### Error Codes Reference

````java
public enum ApiErrorCode {
    // General/System Errors (1000-1999)
    SYSTEM_ERROR(1000, "Internal system error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_CONNECTION_ERROR(1001, "Database connection error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(1002, "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_REQUEST(1003, "Invalid request", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(1004, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),

    // Authentication Module Errors (2000-2099)
    AUTHENTICATION_FAILED(2000, "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(2001, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(2002, "Account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(2003, "Account is disabled", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(2004, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(2005, "Invalid token", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(2006, "Access denied", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(2007, "Invalid refresh token", HttpStatus.UNAUTHORIZED),

    // User Module Errors (2100-2199)
    USER_NOT_FOUND(2100, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2101, "User already exists", HttpStatus.CONFLICT),
    INVALID_USER_DATA(2102, "Invalid user data", HttpStatus.BAD_REQUEST),
    PASSWORD_POLICY_VIOLATION(2103, "Password does not meet policy requirements", HttpStatus.BAD_REQUEST),
    USER_PROFILE_INCOMPLETE(2104, "User profile is incomplete", HttpStatus.BAD_REQUEST),

    // Role Module Errors (2200-2299)
    ROLE_NOT_FOUND(2200, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS(2201, "Role already exists", HttpStatus.CONFLICT),
    INVALID_ROLE_DATA(2202, "Invalid role data", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE(2203, "Role is assigned to users and cannot be deleted", HttpStatus.CONFLICT),

    // Permission/Authorization Module Errors (2300-2399)
    PERMISSION_DENIED(2300, "Permission denied", HttpStatus.FORBIDDEN),
    PERMISSION_NOT_FOUND(2301, "Permission not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_PRIVILEGES(2302, "Insufficient privileges", HttpStatus.FORBIDDEN),

    // Property Module Errors (3000-3099)
    PROPERTY_NOT_FOUND(3000, "Property not found", HttpStatus.NOT_FOUND),
    PROPERTY_ALREADY_EXISTS(3001, "Property already exists", HttpStatus.CONFLICT),
    INVALID_PROPERTY_DATA(3002, "Invalid property data", HttpStatus.BAD_REQUEST),
    PROPERTY_IMAGE_UPLOAD_FAILED(3003, "Property image upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PROPERTY_NOT_AVAILABLE(3004, "Property is not available", HttpStatus.CONFLICT),
    INVALID_PROPERTY_STATUS(3005, "Invalid property status", HttpStatus.BAD_REQUEST),
    PROPERTY_OPERATION_NOT_ALLOWED(3006, "Operation not allowed on this property", HttpStatus.FORBIDDEN),

    // Booking Module Errors (3100-3199)
    BOOKING_NOT_FOUND(3100, "Booking not found", HttpStatus.NOT_FOUND),
    INVALID_BOOKING_DATA(3101, "Invalid booking data", HttpStatus.BAD_REQUEST),
    BOOKING_CONFLICT(3102, "Booking dates conflict with existing booking", HttpStatus.CONFLICT),
    BOOKING_CANCELLATION_NOT_ALLOWED(3103, "Booking cancellation not allowed", HttpStatus.FORBIDDEN),
    BOOKING_ALREADY_CONFIRMED(3104, "Booking is already confirmed", HttpStatus.CONFLICT),
    BOOKING_PAYMENT_REQUIRED(3105, "Payment required to confirm booking", HttpStatus.PAYMENT_REQUIRED),
    INVALID_BOOKING_STATUS(3106, "Invalid booking status", HttpStatus.BAD_REQUEST),

    // Subscription Module Errors (3200-3299)
    SUBSCRIPTION_NOT_FOUND(3200, "Subscription not found", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_ALREADY_EXISTS(3201, "Subscription already exists", HttpStatus.CONFLICT),
    INVALID_SUBSCRIPTION_DATA(3202, "Invalid subscription data", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_EXPIRED(3203, "Subscription has expired", HttpStatus.FORBIDDEN),
    SUBSCRIPTION_LIMIT_REACHED(3204, "Subscription limit reached", HttpStatus.FORBIDDEN),
    SUBSCRIPTION_PAYMENT_FAILED(3205, "Subscription payment failed", HttpStatus.PAYMENT_REQUIRED),
    INVALID_SUBSCRIPTION_PLAN(3206, "Invalid subscription plan", HttpStatus.BAD_REQUEST),

    // Validation Errors (4000-4099)
    VALIDATION_ERROR(4000, "Validation error", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING(4001, "Required field is missing", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(4002, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(4003, "Invalid phone number format", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(4004, "Invalid date format", HttpStatus.BAD_REQUEST),
    INVALID_NUMERIC_VALUE(4005, "Invalid numeric value", HttpStatus.BAD_REQUEST),
    VALUE_OUT_OF_RANGE(4006, "Value out of allowed range", HttpStatus.BAD_REQUEST),

    // External Service Errors (5000-5099)
    PAYMENT_GATEWAY_ERROR(5000, "Payment gateway error", HttpStatus.BAD_GATEWAY),
    MAP_SERVICE_ERROR(5001, "Map service error", HttpStatus.BAD_GATEWAY),
    EMAIL_SERVICE_ERROR(5002, "Email service error", HttpStatus.INTERNAL_SERVER_ERROR),
    SMS_SERVICE_ERROR(5003, "SMS service error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
````


#### Responses structure

````json
{
  "success": boolean,
  "data": T,
  "error": {
    "code": string,
    "message": string,
    "timestamp": string
  },
  "timestamp": string
}
````
#### Resource Not Found
````java
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "User not found with id: 123",
    "timestamp": "2023-07-20T12:34:56.789Z"
  },
  "timestamp": "2023-07-20T12:34:56.789Z"
}

````
#### Validation Error

````java
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Request validation failed",
    "details": [
      "Email is required",
      "Password must be 8+ characters"
    ],
    "timestamp": "2023-07-20T12:34:56.789Z"
  },
  "timestamp": "2023-07-20T12:34:56.789Z"
}

````

#### Success Response 

````java
{
  "success": true,
  "data": {
    "id": 123,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "CLIENT",
    "createdAt": "2023-07-20T10:30:00Z",
    "updatedAt": "2023-07-20T10:30:00Z"
  },
  "error": null,
  "timestamp": "2023-07-20T12:34:56.789Z"
}

````

#### Best Practices

- **Services should throw exceptions** - Let the global handler manage the response  
- **Controllers should be lean** - Focus on routing and response formatting  
- **Use specific exceptions** - Choose the most appropriate exception type  
- **Include helpful messages** - Error messages should help clients debug issues  
- **Log exceptions** - Ensure exceptions are properly logged before being handled  