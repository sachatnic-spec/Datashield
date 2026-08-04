# MFA Integration Testing Script (PowerShell)
# This script automates testing of MFA TOTP setup and verification with Google Authenticator

param(
    [string]$Command,
    [string]$TOTPCode,
    [string]$DeviceId,
    [string]$ApiBaseUrl = "http://localhost:8081",
    [string]$TenantId = "550e8400-e29b-41d4-a716-446655440000",
    [string]$UserEmail = "test@datasheield.com",
    [string]$UserPassword = "TestPassword123!",
    [string]$JWTToken
)

# Color codes for output
$Colors = @{
    Info    = "Cyan"
    Success = "Green"
    Error   = "Red"
    Warning = "Yellow"
}

# Logging functions
function Write-Log {
    param([string]$Message, [string]$Type = "Info")
    $Color = $Colors[$Type]
    Write-Host "[$Type] $Message" -ForegroundColor $Color
}

function Write-Header {
    param([string]$Message)
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

# Check prerequisites
function Test-Prerequisites {
    Write-Log "Checking prerequisites..." "Info"
    
    $prereqMet = $true
    
    # Check if running PowerShell 5.0+
    if ($PSVersionTable.PSVersion.Major -lt 5) {
        Write-Log "PowerShell 5.0+ required" "Error"
        $prereqMet = $false
    }
    
    # Check for curl (built-in for PowerShell 7+, need to check for 5.1)
    if ($PSVersionTable.PSVersion.Major -lt 7) {
        if (-not (Get-Command curl -ErrorAction SilentlyContinue)) {
            Write-Log "curl not found - using Invoke-WebRequest instead" "Warning"
        }
    }
    
    if ($prereqMet) {
        Write-Log "Prerequisites check passed" "Success"
    } else {
        exit 1
    }
}

# Invoke REST API
function Invoke-MFAApi {
    param(
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Body,
        [string]$Token = $script:JWTToken
    )
    
    $Url = "$ApiBaseUrl$Endpoint"
    $Headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($Token) {
        $Headers["Authorization"] = "Bearer $Token"
    }
    
    try {
        if ($Body) {
            $JsonBody = $Body | ConvertTo-Json
            $Response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $Headers -Body $JsonBody -UseBasicParsing
        } else {
            $Response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $Headers -UseBasicParsing
        }
        
        return $Response.Content | ConvertFrom-Json
    }
    catch {
        Write-Log "API Request failed: $_" "Error"
        Write-Log "Response: $($_.Exception.Response)" "Error"
        return $null
    }
}

# Authenticate user and get JWT token
function Authenticate {
    Write-Log "Authenticating user..." "Info"
    
    $Body = @{
        email    = $UserEmail
        password = $UserPassword
        tenantId = $TenantId
    }
    
    $Response = Invoke-MFAApi -Method "POST" -Endpoint "/v1/auth/login" -Body $Body -Token ""
    
    if ($Response -and $Response.accessToken) {
        $script:JWTToken = $Response.accessToken
        Write-Log "Authentication successful" "Success"
        Write-Log "JWT Token (first 50 chars): $($JWTToken.Substring(0, [Math]::Min(50, $JWTToken.Length)))..." "Info"
    } else {
        Write-Log "Failed to authenticate" "Error"
        Write-Log "Response: $Response" "Error"
        exit 1
    }
}

# Setup TOTP
function Setup-TOTP {
    Write-Log "Setting up TOTP..." "Info"
    
    $Body = @{
        tenantId = $TenantId
    }
    
    $Response = Invoke-MFAApi -Method "POST" -Endpoint "/v1/auth/mfa/totp/setup" -Body $Body
    
    if ($Response -and $Response.mfaSetupId -and $Response.secret) {
        $script:MFASetupId = $Response.mfaSetupId
        $script:Secret = $Response.secret
        $script:QRCode = $Response.qrCode
        $script:BackupCodes = $Response.backupCodes
        $script:VerificationUrl = $Response.verificationUrl
        
        Write-Log "TOTP setup successful" "Success"
        Write-Log "MFA Setup ID: $($script:MFASetupId)" "Info"
        Write-Log "Secret: $($script:Secret)" "Info"
        Write-Log "Verification URL: $($script:VerificationUrl)" "Info"
        
        # Save QR code to file
        if ($script:QRCode) {
            try {
                $Base64String = $script:QRCode -replace '^data:image/png;base64,', ''
                $ImageBytes = [Convert]::FromBase64String($Base64String)
                [System.IO.File]::WriteAllBytes("$PSScriptRoot\mfa_qr_code.png", $ImageBytes)
                Write-Log "QR code saved to mfa_qr_code.png" "Success"
                
                Write-Log "Steps to test with Google Authenticator:" "Warning"
                Write-Log "  1. Open Google Authenticator app on your phone" "Info"
                Write-Log "  2. Tap '+' to add account" "Info"
                Write-Log "  3. Choose 'Can't scan it?'" "Info"
                Write-Log "  4. Enter Setup Key: $($script:Secret)" "Info"
                Write-Log "  5. Time-based is already selected" "Info"
                Write-Log "  6. Enter account name: DataShield" "Info"
                Write-Log "  7. Get the 6-digit code and run the verification step" "Info"
            }
            catch {
                Write-Log "Failed to save QR code: $_" "Error"
            }
        }
        
        # Save backup codes
        if ($script:BackupCodes) {
            $BackupCodesText = @"
Backup Codes for DataShield MFA - $(Get-Date)
$($script:BackupCodes -join "`n")
"@
            [System.IO.File]::WriteAllText("$PSScriptRoot\mfa_backup_codes.txt", $BackupCodesText)
            Write-Log "Backup codes saved to mfa_backup_codes.txt" "Success"
        }
    } else {
        Write-Log "Failed to setup TOTP" "Error"
        Write-Log "Response: $Response" "Error"
        exit 1
    }
}

# Verify TOTP code
function Verify-TOTP {
    param([string]$Code)
    
    if (-not $Code) {
        Write-Log "TOTP code required for verification" "Error"
        return $false
    }
    
    Write-Log "Verifying TOTP code: $Code" "Info"
    
    $Body = @{
        tenantId         = $TenantId
        mfaSetupId       = $script:MFASetupId
        verificationCode = $Code
    }
    
    $Response = Invoke-MFAApi -Method "POST" -Endpoint "/v1/auth/mfa/totp/verify" -Body $Body
    
    if ($Response -and $Response.status -eq "VERIFIED") {
        Write-Log "TOTP verification successful!" "Success"
        Write-Log "MFA Device ID: $($Response.mfaDeviceId)" "Info"
        Write-Log "Message: $($Response.message)" "Info"
        return $true
    } else {
        Write-Log "TOTP verification failed" "Error"
        Write-Log "Response: $Response" "Error"
        return $false
    }
}

# Test login with MFA code
function Test-LoginWithMFA {
    param([string]$Code)
    
    if (-not $Code) {
        Write-Log "TOTP code required for login" "Error"
        return $false
    }
    
    Write-Log "Testing login with MFA code: $Code" "Info"
    
    $Body = @{
        email    = $UserEmail
        password = $UserPassword
        tenantId = $TenantId
        mfaCode  = $Code
    }
    
    $Response = Invoke-MFAApi -Method "POST" -Endpoint "/v1/auth/login" -Body $Body -Token ""
    
    if ($Response -and $Response.accessToken) {
        Write-Log "Login with MFA successful!" "Success"
        Write-Log "Access Token (first 50 chars): $($Response.accessToken.Substring(0, 50))..." "Info"
        return $true
    } else {
        Write-Log "Login with MFA failed" "Error"
        Write-Log "Response: $Response" "Error"
        return $false
    }
}

# List MFA devices
function Get-MFADevices {
    Write-Log "Listing MFA devices..." "Info"
    
    $Body = @{
        tenantId = $TenantId
    }
    
    $Response = Invoke-MFAApi -Method "GET" -Endpoint "/v1/auth/mfa/devices" -Body $Body
    
    if ($Response) {
        Write-Log "MFA Devices:" "Info"
        $Response | ConvertTo-Json | Write-Host
    } else {
        Write-Log "Failed to list devices" "Error"
    }
}

# Remove MFA device
function Remove-MFADevice {
    param([string]$Id)
    
    if (-not $Id) {
        Write-Log "Device ID required for removal" "Error"
        return $false
    }
    
    Write-Log "Removing MFA device: $Id" "Info"
    
    $Body = @{
        tenantId = $TenantId
    }
    
    $Response = Invoke-MFAApi -Method "DELETE" -Endpoint "/v1/auth/mfa/devices/$Id" -Body $Body
    
    if ($Response -and $Response.success) {
        Write-Log "Device removed successfully" "Success"
        return $true
    } else {
        Write-Log "Failed to remove device" "Error"
        Write-Log "Response: $Response" "Error"
        return $false
    }
}

# Show interactive menu
function Show-Menu {
    Write-Host ""
    Write-Host "=== DataShield MFA Testing Tool ===" -ForegroundColor Cyan
    Write-Host "1) Setup TOTP (generate QR code)" -ForegroundColor White
    Write-Host "2) Verify TOTP code" -ForegroundColor White
    Write-Host "3) Test login with MFA" -ForegroundColor White
    Write-Host "4) List MFA devices" -ForegroundColor White
    Write-Host "5) Remove MFA device" -ForegroundColor White
    Write-Host "6) Run full test sequence" -ForegroundColor White
    Write-Host "7) Exit" -ForegroundColor White
    Write-Host ""
}

# Run full test sequence
function Start-FullTestSequence {
    Write-Log "Starting full MFA test sequence..." "Info"
    
    Write-Log "Step 1: Setting up TOTP" "Info"
    Setup-TOTP
    
    Write-Log "Step 2: Waiting for user input" "Info"
    Write-Log "Please perform these steps:" "Warning"
    Write-Log "  1. Open the MFA QR code (saved as mfa_qr_code.png)" "Warning"
    Write-Log "  2. Scan with Google Authenticator app" "Warning"
    Write-Log "  3. Get the 6-digit code from the app" "Warning"
    Write-Log "  4. Enter it when prompted" "Warning"
    
    $TOTPCodeInput = Read-Host "Enter 6-digit TOTP code from authenticator"
    
    if (-not $TOTPCodeInput) {
        Write-Log "No code provided" "Error"
        return $false
    }
    
    Write-Log "Step 3: Verifying TOTP code" "Info"
    if (Verify-TOTP -Code $TOTPCodeInput) {
        Write-Log "TOTP verification passed!" "Success"
    } else {
        Write-Log "TOTP verification failed" "Error"
        return $false
    }
    
    $TOTPCodeLogin = Read-Host "Enter 6-digit TOTP code again for login test"
    
    if (-not $TOTPCodeLogin) {
        Write-Log "No code provided" "Error"
        return $false
    }
    
    Write-Log "Step 4: Testing login with MFA" "Info"
    if (Test-LoginWithMFA -Code $TOTPCodeLogin) {
        Write-Log "Login with MFA passed!" "Success"
    } else {
        Write-Log "Login with MFA failed" "Error"
        return $false
    }
    
    Write-Log "Full test sequence completed successfully!" "Success"
    return $true
}

# Main execution
function Main {
    Write-Header "DataShield MFA Integration Testing"
    
    Test-Prerequisites
    
    if (-not $Command) {
        # Interactive mode
        Authenticate
        
        while ($true) {
            Show-Menu
            $Choice = Read-Host "Choose option (1-7)"
            
            switch ($Choice) {
                "1" { Setup-TOTP }
                "2" {
                    $Code = Read-Host "Enter 6-digit TOTP code"
                    Verify-TOTP -Code $Code
                }
                "3" {
                    $Code = Read-Host "Enter 6-digit TOTP code"
                    Test-LoginWithMFA -Code $Code
                }
                "4" { Get-MFADevices }
                "5" {
                    $Id = Read-Host "Enter device ID to remove"
                    Remove-MFADevice -Id $Id
                }
                "6" { Start-FullTestSequence }
                "7" {
                    Write-Log "Exiting..." "Info"
                    exit 0
                }
                default {
                    Write-Log "Invalid option" "Error"
                }
            }
        }
    } else {
        # Command mode
        switch ($Command.ToLower()) {
            "setup" {
                Authenticate
                Setup-TOTP
            }
            "verify" {
                Authenticate
                Verify-TOTP -Code $TOTPCode
            }
            "login" {
                Test-LoginWithMFA -Code $TOTPCode
            }
            "list" {
                Authenticate
                Get-MFADevices
            }
            "remove" {
                Authenticate
                Remove-MFADevice -Id $DeviceId
            }
            "test-full" {
                Authenticate
                Start-FullTestSequence
            }
            default {
                Write-Log "Unknown command: $Command" "Error"
                Write-Log "Available commands: setup, verify, login, list, remove, test-full" "Info"
                exit 1
            }
        }
    }
}

# Run main
Main
