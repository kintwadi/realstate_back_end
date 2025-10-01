# Payment Module Documentation

## Table of Contents
1. [Overview](#overview)
2. [C4 Model Architecture](#c4-model-architecture)
3. [Sequence Diagrams](#sequence-diagrams)
4. [Class Diagrams](#class-diagrams)
5. [API Endpoints](#api-endpoints)
6. [Configuration](#configuration)
7. [Security](#security)
8. [Monitoring and Auditing](#monitoring-and-auditing)

## Overview

The Payment Module is a comprehensive payment processing system designed for the Imovel API. It provides a flexible, secure, and scalable solution for handling various payment operations including processing payments, refunds, cancellations, and webhook handling. The module follows a clean architecture pattern with clear separation of concerns.

### Key Features
- Multi-gateway support (currently Stripe with extensible architecture)
- Comprehensive audit logging
- Real-time monitoring and metrics
- Secure payment processing with authentication
- Webhook handling for payment status updates
- Refund and cancellation capabilities
- Payment statistics and reporting

## C4 Model Architecture

### Context Diagram

```mermaid
C4Context
    title Payment System Context Diagram
    
    Person(customer, "Customer", "User making payments")
    Person(admin, "Administrator", "System administrator")
    
    System(paymentSystem, "Payment System", "Handles payment processing, refunds, and monitoring")
    
    System_Ext(stripeGateway, "Stripe Gateway", "External payment processor")
    System_Ext(database, "PostgreSQL Database", "Stores payment data")
    System_Ext(monitoring, "Monitoring System", "Collects metrics and logs")
    
    Rel(customer, paymentSystem, "Makes payments", "HTTPS/REST")
    Rel(admin, paymentSystem, "Monitors payments", "HTTPS/REST")
    Rel(paymentSystem, stripeGateway, "Processes payments", "HTTPS/API")
    Rel(paymentSystem, database, "Stores/retrieves data", "JDBC")
    Rel(paymentSystem, monitoring, "Sends metrics", "Micrometer")
    Rel(stripeGateway, paymentSystem, "Sends webhooks", "HTTPS")
```

### Container Diagram

```mermaid
C4Container
    title Payment System Container Diagram
    
    Person(customer, "Customer")
    Person(admin, "Administrator")
    
    Container_Boundary(paymentSystem, "Payment System") {
        Container(webApp, "Web Application", "Spring Boot", "Provides REST API for payment operations")
        Container(paymentService, "Payment Service", "Java/Spring", "Core payment business logic")
        Container(gatewayService, "Gateway Service", "Java/Spring", "Payment gateway integrations")
        Container(auditService, "Audit Service", "Java/Spring", "Payment audit logging")
        Container(monitoringService, "Monitoring Service", "Java/Spring", "Metrics and monitoring")
    }
    
    ContainerDb(database, "Database", "PostgreSQL", "Stores payment data")
    Container_Ext(stripeAPI, "Stripe API", "External Service", "Payment processing")
    Container_Ext(metricsStore, "Metrics Store", "Micrometer", "Application metrics")
    
    Rel(customer, webApp, "Makes API calls", "HTTPS/REST")
    Rel(admin, webApp, "Monitors system", "HTTPS/REST")
    Rel(webApp, paymentService, "Delegates requests", "Java calls")
    Rel(paymentService, gatewayService, "Processes payments", "Java calls")
    Rel(paymentService, auditService, "Logs events", "Java calls")
    Rel(paymentService, monitoringService, "Records metrics", "Java calls")
    Rel(gatewayService, stripeAPI, "API calls", "HTTPS")
    Rel(paymentService, database, "Reads/writes", "JDBC")
    Rel(monitoringService, metricsStore, "Sends metrics", "HTTP")
```

### Component Diagram

```mermaid
C4Component
    title Payment Service Component Diagram
    
    Container_Boundary(paymentService, "Payment Service") {
        Component(paymentController, "Payment Controller", "Spring REST Controller", "Handles HTTP requests")
        Component(webhookController, "Webhook Controller", "Spring REST Controller", "Handles webhook events")
        Component(paymentServiceImpl, "Payment Service Implementation", "Service Layer", "Core business logic")
        Component(paymentGatewayFactory, "Payment Gateway Factory", "Factory Pattern", "Creates gateway instances")
        Component(stripeGateway, "Stripe Gateway", "Gateway Implementation", "Stripe-specific logic")
        Component(paymentRepository, "Payment Repository", "JPA Repository", "Data access layer")
        Component(paymentAuditLogger, "Payment Audit Logger", "Audit Component", "Logs payment events")
        Component(monitoringService, "Monitoring Service", "Metrics Component", "Collects metrics")
        Component(securityAspect, "Security Aspect", "AOP Component", "Authorization logic")
    }
    
    Container_Ext(database, "Database", "PostgreSQL")
    Container_Ext(stripeAPI, "Stripe API", "External Service")
    
    Rel(paymentController, paymentServiceImpl, "Delegates to")
    Rel(webhookController, paymentServiceImpl, "Delegates to")
    Rel(paymentServiceImpl, paymentGatewayFactory, "Gets gateway from")
    Rel(paymentGatewayFactory, stripeGateway, "Creates")
    Rel(paymentServiceImpl, paymentRepository, "Uses")
    Rel(paymentServiceImpl, paymentAuditLogger, "Logs with")
    Rel(paymentServiceImpl, monitoringService, "Records metrics with")
    Rel(securityAspect, paymentServiceImpl, "Intercepts calls to")
    Rel(stripeGateway, stripeAPI, "Calls")
    Rel(paymentRepository, database, "Queries")
```

### Code Diagram

```mermaid
C4Component
    title Payment Processing Code Level Diagram
    
    Component_Boundary(paymentProcessing, "Payment Processing") {
        Component(paymentRequest, "PaymentRequest", "DTO", "Payment request data")
        Component(payment, "Payment", "Entity", "Payment domain model")
        Component(paymentStatus, "PaymentStatus", "Enum", "Payment status values")
        Component(paymentGateway, "PaymentGateway", "Enum", "Gateway types")
        Component(paymentMethod, "PaymentMethod", "Enum", "Payment methods")
        Component(paymentResponse, "PaymentResponse", "DTO", "Payment response data")
        Component(applicationResponse, "ApplicationResponse", "Wrapper", "Standard API response")
    }
    
    Rel(paymentRequest, payment, "Maps to")
    Rel(payment, paymentStatus, "Has")
    Rel(payment, paymentGateway, "Uses")
    Rel(payment, paymentMethod, "Uses")
    Rel(payment, paymentResponse, "Maps to")
    Rel(paymentResponse, applicationResponse, "Wrapped in")
```

## Sequence Diagrams

### Payment Processing Flow

```mermaid
sequenceDiagram
    participant C as Customer
    participant PC as PaymentController
    participant PS as PaymentService
    participant PGF as PaymentGatewayFactory
    participant SG as StripeGateway
    participant PR as PaymentRepository
    participant PAL as PaymentAuditLogger
    participant SA as Stripe API
    
    C->>PC: POST /api/payments/process
    PC->>PS: processPayment(paymentRequest, userId)
    PS->>PAL: logPaymentInitiated()
    PS->>PS: validatePaymentRequest()
    PS->>PGF: getPaymentGateway(STRIPE)
    PGF->>SG: return StripeGateway instance
    PS->>PR: save(payment) [PENDING status]
    PS->>SG: processPayment(payment, userId)
    SG->>SA: create PaymentIntent
    SA-->>SG: PaymentIntent response
    SG->>PR: update payment with gatewayPaymentId
    SG->>PAL: logPaymentProcessing()
    SG-->>PS: ApplicationResponse<PaymentResponse>
    PS->>PAL: logPaymentStatusChange()
    PS-->>PC: ApplicationResponse<PaymentResponse>
    PC-->>C: HTTP 200 + PaymentResponse
```

### Webhook Processing Flow

```mermaid
sequenceDiagram
    participant SA as Stripe API
    participant WC as WebhookController
    participant PS as PaymentService
    participant SG as StripeGateway
    participant WH as WebhookHelper
    participant PR as PaymentRepository
    participant PAL as PaymentAuditLogger
    
    SA->>WC: POST /api/payments/stripe/webhook
    WC->>PS: handleWebhook("stripe", payload, signature)
    PS->>SG: handleWebhook(payload, signature)
    SG->>WH: verifySignature(payload, signature)
    WH-->>SG: signature valid
    SG->>SG: parseEvent(payload)
    
    alt Payment Intent Succeeded
        SG->>PR: findByGatewayPaymentId()
        PR-->>SG: Payment entity
        SG->>PR: updateStatus(SUCCEEDED)
        SG->>PAL: logPaymentStatusChange()
    else Payment Intent Failed
        SG->>PR: findByGatewayPaymentId()
        PR-->>SG: Payment entity
        SG->>PR: updateStatus(FAILED)
        SG->>PAL: logPaymentStatusChange()
    end
    
    SG-->>PS: ApplicationResponse<String>
    PS-->>WC: ApplicationResponse<String>
    WC-->>SA: HTTP 200
```

### Refund Processing Flow

```mermaid
sequenceDiagram
    participant A as Admin
    participant PC as PaymentController
    participant PS as PaymentService
    participant PR as PaymentRepository
    participant SG as StripeGateway
    participant SA as Stripe API
    participant PAL as PaymentAuditLogger
    
    A->>PC: POST /api/payments/{id}/refund
    PC->>PS: processRefund(paymentId, amount, reason, userId)
    PS->>PR: findById(paymentId)
    PR-->>PS: Payment entity
    PS->>PS: validateRefundRequest()
    PS->>SG: processRefund(payment, amount, reason)
    SG->>SA: create Refund
    SA-->>SG: Refund response
    SG->>PR: update payment status
    SG->>PAL: logRefundProcessing()
    SG-->>PS: ApplicationResponse<PaymentResponse>
    PS-->>PC: ApplicationResponse<PaymentResponse>
    PC-->>A: HTTP 200 + PaymentResponse
```

## Class Diagrams

### Core Domain Model

```mermaid
classDiagram
    class Payment {
        -Long id
        -Long userId
        -BigDecimal amount
        -String currency
        -Integer quantity
        -String name
        -PaymentGateway gateway
        -PaymentMethod method
        -PaymentStatus status
        -String gatewayPaymentId
        -String failureReason
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +Payment()
        +Payment(userId, amount, currency, ...)
        +getters()
        +setters()
    }
    
    class PaymentStatus {
        <<enumeration>>
        PENDING
        PROCESSING
        SUCCEEDED
        FAILED
        CANCELLED
        REFUNDED
        PARTIALLY_REFUNDED
        DISPUTED
        EXPIRED
        +getValue() String
        +fromString(String) PaymentStatus
    }
    
    class PaymentGateway {
        <<enumeration>>
        STRIPE
        PAYPAL
        +getValue() String
        +fromString(String) PaymentGateway
    }
    
    class PaymentMethod {
        <<enumeration>>
        CREDIT_CARD
        DEBIT_CARD
        BANK_TRANSFER
        DIGITAL_WALLET
        +getValue() String
        +fromString(String) PaymentMethod
    }
    
    Payment --> PaymentStatus
    Payment --> PaymentGateway
    Payment --> PaymentMethod
```

### Service Layer Architecture

```mermaid
classDiagram
    class PaymentService {
        <<interface>>
        +processPayment(PaymentRequest, Long) ApplicationResponse~PaymentResponse~
        +getPaymentById(Long, Long) ApplicationResponse~PaymentResponse~
        +getUserPayments(Long, Pagination, String, String) ApplicationResponse~PaginationResult~
        +processRefund(Long, BigDecimal, String, Long) ApplicationResponse~PaymentResponse~
        +cancelPayment(Long, Long) ApplicationResponse~PaymentResponse~
        +verifyPaymentStatus(Long, Long) ApplicationResponse~PaymentResponse~
        +getPaymentStatistics(Long, LocalDateTime, LocalDateTime) ApplicationResponse~PaymentStatistics~
        +handleWebhook(String, String, String) ApplicationResponse~String~
    }
    
    class PaymentServiceImpl {
        -PaymentRepository paymentRepository
        -PaymentGatewayFactory paymentGatewayFactory
        -PaymentMonitoringService monitoringService
        -SessionManager sessionManager
        -EntityManager entityManager
        +processPayment(PaymentRequest, Long) ApplicationResponse~PaymentResponse~
        +getPaymentById(Long, Long) ApplicationResponse~PaymentResponse~
        +getUserPayments(Long, Pagination, String, String) ApplicationResponse~PaginationResult~
        +processRefund(Long, BigDecimal, String, Long) ApplicationResponse~PaymentResponse~
        +cancelPayment(Long, Long) ApplicationResponse~PaymentResponse~
        +verifyPaymentStatus(Long, Long) ApplicationResponse~PaymentResponse~
        +getPaymentStatistics(Long, LocalDateTime, LocalDateTime) ApplicationResponse~PaymentStatistics~
        +handleWebhook(String, String, String) ApplicationResponse~String~
        -validatePaymentRequest(PaymentRequest) ApplicationResponse~String~
        -canBeRefunded(PaymentStatus) boolean
        -canBeCancelled(PaymentStatus) boolean
        -convertToPaymentResponse(Payment) PaymentResponse
        -calculateStatistics(List~Payment~) PaymentStatistics
    }
    
    class PaymentRepository {
        <<interface>>
        +findByUserId(Long) List~Payment~
        +findByUserIdAndStatus(Long, PaymentStatus) List~Payment~
        +findByGatewayPaymentId(String) Optional~Payment~
        +findByUserIdOrderByCreatedAtDesc(Long, Pageable) Page~Payment~
        +findByUserIdAndCreatedAtBetween(Long, LocalDateTime, LocalDateTime) List~Payment~
    }
    
    PaymentService <|.. PaymentServiceImpl
    PaymentServiceImpl --> PaymentRepository
```

### Gateway Architecture

```mermaid
classDiagram
    class PaymentGatewayInterface {
        <<interface>>
        +processPayment(Payment, Long) ApplicationResponse~PaymentResponse~
        +processRefund(Payment, BigDecimal, String) ApplicationResponse~PaymentResponse~
        +verifyPaymentStatus(String) ApplicationResponse~PaymentResponse~
        +cancelPayment(Payment) ApplicationResponse~PaymentResponse~
        +handleWebhook(String, String) ApplicationResponse~String~
        +getGatewayName() String
        +supportsCurrency(String) boolean
        +getMinimumAmount(String) BigDecimal
    }
    
    class StripePaymentGateway {
        -PaymentRepository paymentRepository
        -StripeConfig stripeConfig
        -WebhookHelper webhookHelper
        -String stripePublicKey
        -String stripeSecretKey
        -String stripeWebhookSecret
        -Set~String~ SUPPORTED_CURRENCIES
        -Map~String, BigDecimal~ MINIMUM_AMOUNTS
        +processPayment(Payment, Long) ApplicationResponse~PaymentResponse~
        +processRefund(Payment, BigDecimal, String) ApplicationResponse~PaymentResponse~
        +verifyPaymentStatus(String) ApplicationResponse~PaymentResponse~
        +cancelPayment(Payment) ApplicationResponse~PaymentResponse~
        +handleWebhook(String, String) ApplicationResponse~String~
        +getGatewayName() String
        +supportsCurrency(String) boolean
        +getMinimumAmount(String) BigDecimal
        -initializeStripe() void
        -convertToSmallestUnit(BigDecimal, String) Long
        -isZeroDecimalCurrency(String) boolean
        -mapStripeStatusToPaymentStatus(String) PaymentStatus
        -convertToPaymentResponse(Payment) PaymentResponse
        -handlePaymentIntentSucceeded(Event) void
        -handlePaymentIntentFailed(Event) void
        -handlePaymentIntentCanceled(Event) void
        -updatePaymentStatus(String, PaymentStatus, String) void
    }
    
    class PaymentGatewayFactory {
        -Map~PaymentGateway, PaymentGatewayInterface~ gateways
        +PaymentGatewayFactory(List~PaymentGatewayInterface~)
        +getPaymentGateway(PaymentGateway) PaymentGatewayInterface
        +getSupportedGateways() Set~PaymentGateway~
    }
    
    PaymentGatewayInterface <|.. StripePaymentGateway
    PaymentGatewayFactory --> PaymentGatewayInterface
```

### DTO and Request/Response Models

```mermaid
classDiagram
    class PaymentRequest {
        -String name
        -Long userId
        -BigDecimal amount
        -Integer quantity
        -String currency
        -String gateway
        -String method
        -String description
        +getters()
        +setters()
        +getGatewayEnum() PaymentGateway
        +getMethodEnum() PaymentMethod
    }
    
    class PaymentResponse {
        -Long id
        -Long userId
        -BigDecimal amount
        -String currency
        -Integer quantity
        -String name
        -String gateway
        -String method
        -String status
        -String gatewayPaymentId
        -String failureReason
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getters()
        +setters()
    }
    
    class ApplicationResponse~T~ {
        -boolean success
        -T data
        -ErrorCode error
        -String message
        +isSuccess() boolean
        +getData() T
        +getError() ErrorCode
        +getMessage() String
        +success(T) ApplicationResponse~T~
        +error(ErrorCode) ApplicationResponse~T~
    }
    
    class PaginationResult~T~ {
        -List~T~ content
        -long totalElements
        -int totalPages
        -int currentPage
        -int pageSize
        -boolean hasNext
        -boolean hasPrevious
        +getters()
        +setters()
    }
    
    PaymentRequest --> PaymentGateway
    PaymentRequest --> PaymentMethod
    ApplicationResponse --> PaymentResponse
    ApplicationResponse --> PaginationResult
```

## API Endpoints

### Payment Operations

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/payments/process` | Process a new payment | PaymentRequest | ApplicationResponse<PaymentResponse> |
| GET | `/api/payments/{id}` | Get payment by ID | - | ApplicationResponse<PaymentResponse> |
| GET | `/api/payments/user/{userId}` | Get user's payment history | Query params: page, size, sortBy, sortDirection | ApplicationResponse<PaginationResult<PaymentResponse>> |
| POST | `/api/payments/{id}/refund` | Process a refund | RefundRequest | ApplicationResponse<PaymentResponse> |
| POST | `/api/payments/{id}/cancel` | Cancel a payment | - | ApplicationResponse<PaymentResponse> |
| POST | `/api/payments/{id}/verify` | Verify payment status | - | ApplicationResponse<PaymentResponse> |
| GET | `/api/payments/statistics/{userId}` | Get payment statistics | Query params: startDate, endDate | ApplicationResponse<PaymentStatistics> |

### Webhook Endpoints

| Method | Endpoint | Description | Headers | Response |
|--------|----------|-------------|---------|----------|
| POST | `/api/payments/stripe/webhook` | Handle Stripe webhooks | Stripe-Signature | ApplicationResponse<String> |

### Request/Response Examples

#### Process Payment Request
```json
{
  "name": "Premium Subscription",
  "userId": 12345,
  "amount": 99.99,
  "quantity": 1,
  "currency": "USD",
  "gateway": "STRIPE",
  "method": "CREDIT_CARD",
  "description": "Monthly premium subscription"
}
```

#### Payment Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 12345,
    "amount": 99.99,
    "currency": "USD",
    "quantity": 1,
    "name": "Premium Subscription",
    "gateway": "STRIPE",
    "method": "CREDIT_CARD",
    "status": "SUCCEEDED",
    "gatewayPaymentId": "pi_1234567890",
    "failureReason": null,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:05"
  },
  "error": null,
  "message": "Payment processed successfully"
}
```

## Configuration

### Application Properties

```yaml
# Stripe Configuration
stripe:
  public-key: ${STRIPE_PUBLIC_KEY}
  secret-key: ${STRIPE_SECRET_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}

# Payment Configuration
payment:
  default-currency: USD
  max-refund-days: 30
  webhook-timeout: 30000
  
# Monitoring Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Database Configuration

The payment module uses JPA entities with the following database schema:

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    quantity INTEGER,
    name VARCHAR(255),
    gateway VARCHAR(50) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    gateway_payment_id VARCHAR(255),
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_gateway_payment_id ON payments(gateway_payment_id);
CREATE INDEX idx_payments_created_at ON payments(created_at);
```

## Security

### Authentication and Authorization

The payment module implements comprehensive security measures:

1. **Session-based Authentication**: Uses SessionManager for user authentication
2. **AOP Security**: PaymentServiceAspect intercepts all service calls for authorization
3. **User Isolation**: All operations are scoped to the authenticated user
4. **Webhook Security**: Stripe webhook signature verification

### Security Aspect Implementation

```java
@Around("paymentServiceMethods()")
public Object authorizeAccess(ProceedingJoinPoint joinPoint) throws Throwable {
    // Extract user ID from method parameters
    // Verify against authenticated user
    // Proceed with method execution if authorized
}
```

### Data Protection

- Sensitive payment data is not logged in plain text
- Gateway payment IDs are used for external references
- Failure reasons are sanitized before storage

## Monitoring and Auditing

### Audit Logging

The PaymentAuditLogger provides comprehensive audit trails:

- Payment initiation events
- Status change tracking
- Refund processing logs
- Webhook event logging
- Validation failure tracking

### Metrics and Monitoring

PaymentMonitoringService collects:

- Payment processing times
- Success/failure rates
- Gateway response times
- Error rates by type
- Revenue metrics

### Monitoring Endpoints

- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics format

### Log4j2 Configuration

The module uses structured JSON logging with separate audit loggers:

```xml
<AsyncLogger name="PAYMENT_AUDIT" level="INFO" additivity="false">
    <AppenderRef ref="AsyncFile"/>
</AsyncLogger>
```

## Error Handling

The payment module implements comprehensive error handling:

### Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| 5200 | Unsupported payment gateway | 400 |
| 5201 | Invalid payment amount | 400 |
| 5202 | Payment not found | 404 |
| 5203 | Payment cannot be refunded | 400 |
| 5204 | Payment cannot be cancelled | 400 |
| 5205 | Gateway processing error | 500 |

### Exception Handling

- PaymentProcessingException for gateway-specific errors
- Validation exceptions for request validation
- Authentication exceptions for security violations
- Generic ApplicationResponse wrapper for consistent error responses

## Future Enhancements

1. **Additional Payment Gateways**: PayPal, Square, etc.
2. **Recurring Payments**: Subscription management
3. **Multi-currency Support**: Enhanced currency handling
4. **Advanced Analytics**: Detailed reporting and analytics
5. **Fraud Detection**: Integration with fraud detection services
6. **Mobile Payment Methods**: Apple Pay, Google Pay support

---

*This documentation is maintained as part of the Imovel API project. For questions or updates, please contact the development team.*