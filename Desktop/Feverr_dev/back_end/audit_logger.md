# Property Rental and Management Audit Log Design

## Overview
An audit log for property rental and management should track all significant actions and changes made within the system, providing accountability and a historical record for compliance, dispute resolution, and operational analysis.

## Core Log Structure

### Basic Fields (for every log entry)
- **Timestamp**: Date and time of the action (UTC)
- **User ID**: Identifier of the user who performed the action
- **User Role**: Tenant/Landlord/Property Manager/Admin/etc.
- **IP Address**: Origin of the request
- **Action Type**: Categorization of the action
- **Entity Type**: What was affected (lease, property, payment, etc.)
- **Entity ID**: Specific identifier of the affected item
- **Description**: Human-readable description of the action
- **Status**: Success/Failure/Partial
- **Request ID**: Unique identifier for the transaction

## Action-Specific Fields

### 1. Property Management Actions
- **Property Created/Updated/Deleted**
  - Property ID
  - Address
  - Field changes (old value → new value)
  - Approval status changes

### 2. Lease Agreement Actions
- **Lease Created/Updated/Terminated/Renewed**
  - Lease ID
  - Tenant IDs
  - Property ID
  - Term dates (old → new)
  - Rent amount changes
  - Terms modified
  - Digital signature events

### 3. Tenant Management
- **Tenant Added/Removed/Updated**
  - Tenant ID
  - Personal information changes (masked for privacy)
  - Background check events
  - Emergency contact updates

### 4. Financial Transactions
- **Rent Payment Received**
  - Payment ID
  - Amount
  - Payment method
  - Due date vs payment date
  - Late fee applied/waived
  - Receipt generated
  
- **Expense Recorded**
  - Expense ID
  - Category
  - Amount
  - Property affected
  - Supporting documents

### 5. Maintenance Activities
- **Maintenance Request Created/Updated/Completed**
  - Request ID
  - Property ID
  - Issue type
  - Priority
  - Vendor assigned
  - Cost
  - Before/after photos

### 6. Access Control
- **Login/Logout Events**
  - Authentication method
  - Device information
  - Failed login attempts
  
- **Permission Changes**
  - User role modified
  - Access rights granted/revoked

### 7. System Configuration
- **Settings Changed**
  - Notification preferences
  - Billing configurations
  - System defaults

## Security Considerations
- **Immutable Storage**: Logs should be write-once, read-many
- **Access Control**: Strict permissions for viewing audit logs
- **Data Retention**: Policy for how long logs are kept
- **Masking**: Sensitive data (SSN, bank details) should be masked/hashed

## Implementation Recommendations

### Database Table Structure/ JSON layout 
```sql
CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  timestamp DATETIME NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  ip_address VARCHAR(45),
  action_type VARCHAR(100) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id VARCHAR(36) NOT NULL,
  description TEXT,
  status VARCHAR(20) NOT NULL,
  request_id VARCHAR(36) NOT NULL,
  old_values JSON,
  new_values JSON,
  metadata JSON
);