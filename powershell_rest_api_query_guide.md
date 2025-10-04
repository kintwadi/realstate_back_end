# PowerShell REST API Query Guide

A comprehensive guide for testing REST APIs using PowerShell with dynamic variables, authentication tokens, and advanced techniques.

## Table of Contents
1. [Basic Setup](#basic-setup)
2. [Global Variables and Configuration](#global-variables-and-configuration)
3. [Authentication and Token Management](#authentication-and-token-management)
4. [Dynamic Variables](#dynamic-variables)
5. [Helper Functions](#helper-functions)
6. [Common API Operations](#common-api-operations)
7. [Error Handling](#error-handling)
8. [Advanced Techniques](#advanced-techniques)
9. [Real-World Examples](#real-world-examples)

## Basic Setup

### Setting Base Configuration
```powershell
# Set global base URL
$global:baseUrl = "http://localhost:8080/imovel"

# Set default headers
$global:defaultHeaders = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}
```

### Basic HTTP Request
```powershell
# Simple GET request
$response = Invoke-WebRequest -Uri "$global:baseUrl/api/subscription-plans" -Method GET
$data = $response.Content | ConvertFrom-Json
Write-Output $data
```

## Global Variables and Configuration

### Environment Configuration
```powershell
# Global configuration object
$global:apiConfig = @{
    baseUrl = "http://localhost:8080/imovel"
    timeout = 30
    retryCount = 3
    debug = $true
}

# Authentication state
$global:authState = @{
    token = $null
    refreshToken = $null
    userId = $null
    isAuthenticated = $false
    tokenExpiry = $null
}

# API endpoints
$global:endpoints = @{
    auth = @{
        login = "/api/auth/login"
        register = "/api/auth/register"
        refresh = "/api/auth/refresh"
        logout = "/api/auth/logout"
    }
    subscriptions = @{
        plans = "/api/subscription-plans"
        userSubscriptions = "/api/subscriptions/user"
        subscribe = "/api/subscriptions/subscribe"
        cancel = "/api/subscriptions/cancel"
    }
    users = @{
        profile = "/api/users/profile"
        update = "/api/users/update"
    }
}
```

## Authentication and Token Management

### Login Function with Token Storage
```powershell
function Login-User {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Email,
        
        [Parameter(Mandatory=$true)]
        [string]$Password
    )
    
    $loginBody = @{
        email = $Email
        password = $Password
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri "$($global:apiConfig.baseUrl)$($global:endpoints.auth.login)" `
                                     -Method POST `
                                     -Body $loginBody `
                                     -ContentType "application/json"
        
        $loginData = $response.Content | ConvertFrom-Json
        
        if ($loginData.success) {
            # Store authentication state globally
            $global:authState.token = $loginData.data.accessToken
            $global:authState.refreshToken = $loginData.data.refreshToken
            $global:authState.isAuthenticated = $true
            $global:authState.tokenExpiry = (Get-Date).AddMinutes(15) # Assuming 15-min expiry
            
            # Update default headers with token
            $global:defaultHeaders["Authorization"] = "Bearer $($global:authState.token)"
            
            Write-Host "✅ Login successful! Token stored globally." -ForegroundColor Green
            return $loginData.data
        } else {
            Write-Error "❌ Login failed: $($loginData.message)"
            return $null
        }
    }
    catch {
        Write-Error "❌ Login request failed: $($_.Exception.Message)"
        return $null
    }
}
```

### Token Refresh Function
```powershell
function Refresh-Token {
    if (-not $global:authState.refreshToken) {
        Write-Error "❌ No refresh token available"
        return $false
    }
    
    $refreshBody = @{
        refreshToken = $global:authState.refreshToken
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri "$($global:apiConfig.baseUrl)$($global:endpoints.auth.refresh)" `
                                     -Method POST `
                                     -Body $refreshBody `
                                     -ContentType "application/json"
        
        $refreshData = $response.Content | ConvertFrom-Json
        
        if ($refreshData.success) {
            $global:authState.token = $refreshData.data.accessToken
            $global:authState.tokenExpiry = (Get-Date).AddMinutes(15)
            $global:defaultHeaders["Authorization"] = "Bearer $($global:authState.token)"
            
            Write-Host "✅ Token refreshed successfully!" -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Error "❌ Token refresh failed: $($_.Exception.Message)"
        $global:authState.isAuthenticated = $false
        return $false
    }
}
```

### Auto Token Validation
```powershell
function Test-TokenValidity {
    if (-not $global:authState.isAuthenticated) {
        return $false
    }
    
    if ($global:authState.tokenExpiry -and (Get-Date) -gt $global:authState.tokenExpiry) {
        Write-Host "⚠️ Token expired, attempting refresh..." -ForegroundColor Yellow
        return Refresh-Token
    }
    
    return $true
}
```

## Dynamic Variables

### User Input Variables
```powershell
# Prompt for dynamic values
function Get-UserInput {
    param([string]$Prompt, [string]$Default = "")
    
    if ($Default) {
        $input = Read-Host "$Prompt [$Default]"
        return if ($input) { $input } else { $Default }
    } else {
        return Read-Host $Prompt
    }
}

# Example usage
$userId = Get-UserInput -Prompt "Enter User ID" -Default "123"
$planId = Get-UserInput -Prompt "Enter Plan ID" -Default "1"
```

### Environment-Based Variables
```powershell
# Load from environment variables with fallbacks
$global:dynamicConfig = @{
    userId = $env:API_USER_ID ?? "123"
    planId = $env:API_PLAN_ID ?? "1"
    testEmail = $env:TEST_EMAIL ?? "test@example.com"
    testPassword = $env:TEST_PASSWORD ?? "password123"
}
```

### Configuration File Support
```powershell
function Load-ConfigFromFile {
    param([string]$ConfigPath = "api-config.json")
    
    if (Test-Path $ConfigPath) {
        try {
            $config = Get-Content $ConfigPath | ConvertFrom-Json
            $global:dynamicConfig = $config
            Write-Host "✅ Configuration loaded from $ConfigPath" -ForegroundColor Green
        }
        catch {
            Write-Warning "⚠️ Failed to load config file: $($_.Exception.Message)"
        }
    } else {
        Write-Host "ℹ️ Config file not found, using defaults" -ForegroundColor Blue
    }
}
```

## Helper Functions

### Generic API Request Function
```powershell
function Invoke-ApiRequest {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Endpoint,
        
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [hashtable]$Headers = $null,
        [switch]$RequireAuth = $false,
        [switch]$ReturnRaw = $false
    )
    
    # Check authentication if required
    if ($RequireAuth -and -not (Test-TokenValidity)) {
        Write-Error "❌ Authentication required but not valid"
        return $null
    }
    
    # Prepare headers
    $requestHeaders = $global:defaultHeaders.Clone()
    if ($Headers) {
        $Headers.GetEnumerator() | ForEach-Object {
            $requestHeaders[$_.Key] = $_.Value
        }
    }
    
    # Prepare request parameters
    $requestParams = @{
        Uri = "$($global:apiConfig.baseUrl)$Endpoint"
        Method = $Method
        Headers = $requestHeaders
    }
    
    if ($Body) {
        $requestParams.Body = ($Body | ConvertTo-Json -Depth 10)
    }
    
    try {
        if ($global:apiConfig.debug) {
            Write-Host "🔍 $Method $($requestParams.Uri)" -ForegroundColor Cyan
            if ($Body) {
                Write-Host "📤 Body: $($requestParams.Body)" -ForegroundColor Gray
            }
        }
        
        $response = Invoke-WebRequest @requestParams
        
        if ($ReturnRaw) {
            return $response
        }
        
        $data = $response.Content | ConvertFrom-Json
        
        if ($global:apiConfig.debug) {
            Write-Host "📥 Response: $($response.StatusCode)" -ForegroundColor Green
        }
        
        return $data
    }
    catch {
        Write-Error "❌ API Request failed: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $errorResponse = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorResponse)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error details: $errorBody" -ForegroundColor Red
        }
        return $null
    }
}
```

### Response Formatting
```powershell
function Format-ApiResponse {
    param(
        [Parameter(ValueFromPipeline=$true)]
        $Response,
        [switch]$Pretty
    )
    
    if ($Pretty) {
        return $Response | ConvertTo-Json -Depth 10 | Out-String
    } else {
        return $Response | Format-Table -AutoSize
    }
}
```

## Common API Operations

### User Registration
```powershell
function Register-User {
    param(
        [string]$Name,
        [string]$Email,
        [string]$Password
    )
    
    $registerBody = @{
        name = $Name
        email = $Email
        password = $Password
    }
    
    return Invoke-ApiRequest -Endpoint $global:endpoints.auth.register -Method POST -Body $registerBody
}
```

### Subscription Management
```powershell
function Get-SubscriptionPlans {
    return Invoke-ApiRequest -Endpoint $global:endpoints.subscriptions.plans
}

function Get-UserSubscriptions {
    param([string]$UserId = $global:dynamicConfig.userId)
    
    return Invoke-ApiRequest -Endpoint "$($global:endpoints.subscriptions.userSubscriptions)/$UserId" -RequireAuth
}

function New-Subscription {
    param(
        [string]$UserId = $global:dynamicConfig.userId,
        [string]$PlanId = $global:dynamicConfig.planId
    )
    
    $subscribeBody = @{
        userId = $UserId
        planId = $PlanId
    }
    
    return Invoke-ApiRequest -Endpoint $global:endpoints.subscriptions.subscribe -Method POST -Body $subscribeBody -RequireAuth
}
```

## Error Handling

### Retry Logic
```powershell
function Invoke-ApiRequestWithRetry {
    param(
        [string]$Endpoint,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [int]$MaxRetries = $global:apiConfig.retryCount
    )
    
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            $result = Invoke-ApiRequest -Endpoint $Endpoint -Method $Method -Body $Body
            if ($result) {
                return $result
            }
        }
        catch {
            Write-Warning "⚠️ Attempt $i failed: $($_.Exception.Message)"
            if ($i -eq $MaxRetries) {
                throw
            }
            Start-Sleep -Seconds (2 * $i) # Exponential backoff
        }
    }
}
```

### Status Code Handling
```powershell
function Test-ApiHealth {
    try {
        $response = Invoke-WebRequest -Uri "$($global:apiConfig.baseUrl)/api/subscription-plans" -Method GET
        
        switch ($response.StatusCode) {
            200 { Write-Host "✅ API is healthy" -ForegroundColor Green }
            401 { Write-Host "🔐 Authentication required" -ForegroundColor Yellow }
            403 { Write-Host "🚫 Access forbidden" -ForegroundColor Red }
            404 { Write-Host "❓ Endpoint not found" -ForegroundColor Red }
            500 { Write-Host "💥 Server error" -ForegroundColor Red }
            default { Write-Host "⚠️ Unexpected status: $($response.StatusCode)" -ForegroundColor Yellow }
        }
        
        return $response.StatusCode
    }
    catch {
        Write-Host "❌ API is not accessible: $($_.Exception.Message)" -ForegroundColor Red
        return 0
    }
}
```

## Advanced Techniques

### Batch Operations
```powershell
function Invoke-BatchRequests {
    param(
        [array]$Requests,
        [int]$BatchSize = 5,
        [int]$DelayMs = 100
    )
    
    $results = @()
    
    for ($i = 0; $i -lt $Requests.Count; $i += $BatchSize) {
        $batch = $Requests[$i..([Math]::Min($i + $BatchSize - 1, $Requests.Count - 1))]
        
        Write-Host "Processing batch $([Math]::Floor($i / $BatchSize) + 1)..." -ForegroundColor Blue
        
        $batchResults = $batch | ForEach-Object -Parallel {
            Invoke-ApiRequest @_
        } -ThrottleLimit $BatchSize
        
        $results += $batchResults
        
        if ($DelayMs -gt 0) {
            Start-Sleep -Milliseconds $DelayMs
        }
    }
    
    return $results
}
```

### Data Export
```powershell
function Export-ApiData {
    param(
        [string]$Endpoint,
        [string]$OutputPath,
        [string]$Format = "json" # json, csv, xml
    )
    
    $data = Invoke-ApiRequest -Endpoint $Endpoint
    
    switch ($Format.ToLower()) {
        "json" {
            $data | ConvertTo-Json -Depth 10 | Out-File -FilePath $OutputPath
        }
        "csv" {
            $data | ConvertTo-Csv -NoTypeInformation | Out-File -FilePath $OutputPath
        }
        "xml" {
            $data | ConvertTo-Xml -NoTypeInformation | Out-File -FilePath $OutputPath
        }
    }
    
    Write-Host "✅ Data exported to $OutputPath" -ForegroundColor Green
}
```

## Real-World Examples

### Complete Testing Workflow
```powershell
# Initialize configuration
Load-ConfigFromFile

# Test API health
$healthStatus = Test-ApiHealth
if ($healthStatus -ne 200) {
    Write-Error "API not available, exiting"
    exit 1
}

# Login
$loginResult = Login-User -Email $global:dynamicConfig.testEmail -Password $global:dynamicConfig.testPassword
if (-not $loginResult) {
    Write-Error "Login failed, exiting"
    exit 1
}

# Get subscription plans
Write-Host "`n📋 Getting subscription plans..." -ForegroundColor Blue
$plans = Get-SubscriptionPlans
$plans | Format-ApiResponse -Pretty

# Get user subscriptions
Write-Host "`n👤 Getting user subscriptions..." -ForegroundColor Blue
$userSubs = Get-UserSubscriptions -UserId $global:dynamicConfig.userId
$userSubs | Format-ApiResponse

# Create new subscription
Write-Host "`n➕ Creating new subscription..." -ForegroundColor Blue
$newSub = New-Subscription -UserId $global:dynamicConfig.userId -PlanId $global:dynamicConfig.planId
$newSub | Format-ApiResponse -Pretty

# Export results
Export-ApiData -Endpoint "/api/subscription-plans" -OutputPath "subscription-plans.json" -Format "json"
```

### Interactive Testing Session
```powershell
function Start-InteractiveSession {
    Write-Host "🚀 Starting Interactive API Testing Session" -ForegroundColor Cyan
    
    while ($true) {
        Write-Host "`nAvailable commands:" -ForegroundColor Yellow
        Write-Host "1. Login"
        Write-Host "2. Get Plans"
        Write-Host "3. Get User Subscriptions"
        Write-Host "4. Create Subscription"
        Write-Host "5. Test API Health"
        Write-Host "6. Set User ID"
        Write-Host "7. Export Data"
        Write-Host "0. Exit"
        
        $choice = Read-Host "`nEnter your choice"
        
        switch ($choice) {
            "1" {
                $email = Get-UserInput -Prompt "Email" -Default $global:dynamicConfig.testEmail
                $password = Get-UserInput -Prompt "Password" -Default $global:dynamicConfig.testPassword
                Login-User -Email $email -Password $password
            }
            "2" {
                Get-SubscriptionPlans | Format-ApiResponse
            }
            "3" {
                $userId = Get-UserInput -Prompt "User ID" -Default $global:dynamicConfig.userId
                Get-UserSubscriptions -UserId $userId | Format-ApiResponse
            }
            "4" {
                $userId = Get-UserInput -Prompt "User ID" -Default $global:dynamicConfig.userId
                $planId = Get-UserInput -Prompt "Plan ID" -Default $global:dynamicConfig.planId
                New-Subscription -UserId $userId -PlanId $planId | Format-ApiResponse -Pretty
            }
            "5" {
                Test-ApiHealth
            }
            "6" {
                $global:dynamicConfig.userId = Get-UserInput -Prompt "New User ID" -Default $global:dynamicConfig.userId
                Write-Host "✅ User ID updated to: $($global:dynamicConfig.userId)" -ForegroundColor Green
            }
            "7" {
                $endpoint = Get-UserInput -Prompt "Endpoint" -Default "/api/subscription-plans"
                $outputPath = Get-UserInput -Prompt "Output Path" -Default "export.json"
                Export-ApiData -Endpoint $endpoint -OutputPath $outputPath
            }
            "0" {
                Write-Host "👋 Goodbye!" -ForegroundColor Green
                break
            }
            default {
                Write-Host "❌ Invalid choice" -ForegroundColor Red
            }
        }
    }
}
```

## Quick Start Commands

```powershell
# Source this file to load all functions
. .\powershell_rest_api_query_guide.ps1

# Quick setup
Load-ConfigFromFile
Test-ApiHealth

# Quick login and test
Login-User -Email "test@example.com" -Password "password123"
Get-SubscriptionPlans | Format-ApiResponse

# Start interactive session
Start-InteractiveSession
```

## Best Practices

1. **Always validate tokens** before making authenticated requests
2. **Use retry logic** for network-dependent operations
3. **Log requests and responses** for debugging
4. **Handle errors gracefully** with meaningful messages
5. **Use environment variables** for sensitive data
6. **Implement rate limiting** to avoid overwhelming the API
7. **Cache responses** when appropriate to reduce API calls
8. **Use batch operations** for multiple similar requests

## Troubleshooting

### Common Issues
- **401 Unauthorized**: Check if token is valid and not expired
- **403 Forbidden**: Verify user permissions for the endpoint
- **404 Not Found**: Confirm endpoint URL and API availability
- **500 Internal Server Error**: Check server logs and request payload

### Debug Mode
```powershell
# Enable debug mode
$global:apiConfig.debug = $true

# Disable debug mode
$global:apiConfig.debug = $false
```

This guide provides a comprehensive foundation for testing REST APIs with PowerShell, including authentication, dynamic variables, error handling, and advanced techniques.