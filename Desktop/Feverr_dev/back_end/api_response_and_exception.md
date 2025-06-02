## API Response and Exception Handling Documentation
### Table of Contents
1. Standard Response Structure

2. Custom Exceptions

- Exception Hierarchy

- Available Exceptions

3. Usage Examples

  - Controller Implementation

  - Service Implementation

4. Error Codes Reference

#### All API responses follow the StandardResponse<T> format:
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

````java
// Successful response
StandardResponse.success(T data)

// Error response with code and message
StandardResponse.error(String code, String message)

// Error response with ErrorResponse object
StandardResponse.error(ErrorResponse error)

 new ResourceNotFoundException("User", userId);

 new AuthenticationException("INVALID_CREDENTIALS", "Email or password is incorrect");

 new AuthorizationException("ACCESS_DENIED", "You don't have permission to access this resource");

 new ValidationException(
    "INVALID_REQUEST", 
    "Request validation failed", 
    List.of("Email is required", "Password must be 8+ characters")
);

new ConflictException("EMAIL_EXISTS", "User with this email already exists");

new TokenRefreshException("EXPIRED_TOKEN", "Refresh token was expired");

new BusinessRuleException("INSUFFICIENT_BALANCE", "Account has insufficient balance");


// Service Implementation
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

//Controller Implementation

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

| Code                     | HTTP Status | Description                          |
|--------------------------|-------------|--------------------------------------|
| RESOURCE_NOT_FOUND       | 404         | Requested resource doesn't exist    |
| INVALID_CREDENTIALS      | 401         | Authentication failed               |
| ACCESS_DENIED            | 403         | Insufficient permissions            |
| INVALID_REQUEST          | 400         | Request validation failed           |
| EMAIL_EXISTS             | 409         | Email already registered            |
| EXPIRED_TOKEN            | 401         | Refresh token expired               |
| INSUFFICIENT_BALANCE     | 422         | Business rule violation             |
| INTERNAL_SERVER_ERROR    | 500         | Unexpected server error             |


#### Example Error Responses
Resource Not Found
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
Validation Error

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

Success Response 

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

### Best Practices

- **Services should throw exceptions** - Let the global handler manage the response  
- **Controllers should be lean** - Focus on routing and response formatting  
- **Use specific exceptions** - Choose the most appropriate exception type  
- **Include helpful messages** - Error messages should help clients debug issues  
- **Log exceptions** - Ensure exceptions are properly logged before being handled  