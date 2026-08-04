#!/bin/bash

# MFA Integration Testing Script
# This script automates testing of MFA TOTP setup and verification with Google Authenticator

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8081}"
TENANT_ID="${TENANT_ID:-550e8400-e29b-41d4-a716-446655440000}"
JWT_TOKEN="${JWT_TOKEN:-}"
USER_EMAIL="${USER_EMAIL:-test@datasheield.com}"
USER_PASSWORD="${USER_PASSWORD:-TestPassword123!}"

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v curl &> /dev/null; then
        log_error "curl is not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        log_warning "jq is not installed - output may be unformatted"
        log_info "Install jq for better output formatting: brew install jq (macOS) or apt-get install jq (Linux)"
    fi
    
    log_success "Prerequisites check passed"
}

# Authenticate user and get JWT token
authenticate() {
    log_info "Authenticating user..."
    
    RESPONSE=$(curl -s -X POST "${API_BASE_URL}/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"${USER_EMAIL}\",
            \"password\": \"${USER_PASSWORD}\",
            \"tenantId\": \"${TENANT_ID}\"
        }")
    
    JWT_TOKEN=$(echo "$RESPONSE" | jq -r '.accessToken // empty' 2>/dev/null)
    
    if [ -z "$JWT_TOKEN" ]; then
        log_error "Failed to authenticate"
        log_info "Response: $RESPONSE"
        exit 1
    fi
    
    log_success "Authentication successful"
    log_info "JWT Token (first 50 chars): ${JWT_TOKEN:0:50}..."
}

# Step 1: Setup TOTP
setup_totp() {
    log_info "Setting up TOTP..."
    
    SETUP_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/v1/auth/mfa/totp/setup" \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{
            \"tenantId\": \"${TENANT_ID}\"
        }")
    
    # Extract setup ID, secret, and QR code
    MFA_SETUP_ID=$(echo "$SETUP_RESPONSE" | jq -r '.mfaSetupId // empty' 2>/dev/null)
    SECRET=$(echo "$SETUP_RESPONSE" | jq -r '.secret // empty' 2>/dev/null)
    QR_CODE=$(echo "$SETUP_RESPONSE" | jq -r '.qrCode // empty' 2>/dev/null)
    BACKUP_CODES=$(echo "$SETUP_RESPONSE" | jq -r '.backupCodes // empty' 2>/dev/null)
    VERIFICATION_URL=$(echo "$SETUP_RESPONSE" | jq -r '.verificationUrl // empty' 2>/dev/null)
    
    if [ -z "$MFA_SETUP_ID" ] || [ -z "$SECRET" ]; then
        log_error "Failed to setup TOTP"
        log_info "Response: $SETUP_RESPONSE"
        exit 1
    fi
    
    log_success "TOTP setup successful"
    log_info "MFA Setup ID: $MFA_SETUP_ID"
    log_info "Secret: $SECRET"
    log_info "Verification URL: $VERIFICATION_URL"
    
    # Save QR code to file for manual scanning
    if [ ! -z "$QR_CODE" ]; then
        echo "$QR_CODE" | sed 's/data:image\/png;base64,//' | base64 -d > mfa_qr_code.png
        log_success "QR code saved to mfa_qr_code.png"
        log_info "🔍 Steps to test with Google Authenticator:"
        log_info "  1. Open Google Authenticator app on your phone"
        log_info "  2. Tap '+' to add account"
        log_info "  3. Choose 'Can't scan it?'"
        log_info "  4. Enter Setup Key: $SECRET"
        log_info "  5. Time-based is already selected"
        log_info "  6. Enter account name: DataShield"
        log_info "  7. Get the 6-digit code and run the verification step"
    fi
    
    # Save backup codes
    if [ ! -z "$BACKUP_CODES" ]; then
        echo "Backup Codes for DataShield MFA - $(date)" > mfa_backup_codes.txt
        echo "$BACKUP_CODES" >> mfa_backup_codes.txt
        log_success "Backup codes saved to mfa_backup_codes.txt"
    fi
}

# Step 2: Verify TOTP code
verify_totp() {
    if [ -z "$1" ]; then
        log_error "TOTP code required for verification"
        log_info "Usage: verify_totp <6-digit-code>"
        return 1
    fi
    
    local TOTP_CODE="$1"
    
    log_info "Verifying TOTP code: $TOTP_CODE"
    
    VERIFY_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/v1/auth/mfa/totp/verify" \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{
            \"tenantId\": \"${TENANT_ID}\",
            \"mfaSetupId\": \"${MFA_SETUP_ID}\",
            \"verificationCode\": \"${TOTP_CODE}\"
        }")
    
    STATUS=$(echo "$VERIFY_RESPONSE" | jq -r '.status // empty' 2>/dev/null)
    MFA_DEVICE_ID=$(echo "$VERIFY_RESPONSE" | jq -r '.mfaDeviceId // empty' 2>/dev/null)
    MESSAGE=$(echo "$VERIFY_RESPONSE" | jq -r '.message // empty' 2>/dev/null)
    
    if [ "$STATUS" = "VERIFIED" ]; then
        log_success "TOTP verification successful!"
        log_info "MFA Device ID: $MFA_DEVICE_ID"
        log_info "Message: $MESSAGE"
        return 0
    else
        log_error "TOTP verification failed"
        log_info "Response: $VERIFY_RESPONSE"
        return 1
    fi
}

# Step 3: Test login with MFA code
login_with_mfa() {
    if [ -z "$1" ]; then
        log_error "TOTP code required for login"
        log_info "Usage: login_with_mfa <6-digit-code>"
        return 1
    fi
    
    local TOTP_CODE="$1"
    
    log_info "Testing login with MFA code: $TOTP_CODE"
    
    LOGIN_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"${USER_EMAIL}\",
            \"password\": \"${USER_PASSWORD}\",
            \"tenantId\": \"${TENANT_ID}\",
            \"mfaCode\": \"${TOTP_CODE}\"
        }")
    
    ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // empty' 2>/dev/null)
    
    if [ ! -z "$ACCESS_TOKEN" ]; then
        log_success "Login with MFA successful!"
        log_info "Access Token (first 50 chars): ${ACCESS_TOKEN:0:50}..."
        return 0
    else
        log_error "Login with MFA failed"
        log_info "Response: $LOGIN_RESPONSE"
        return 1
    fi
}

# List MFA devices
list_devices() {
    log_info "Listing MFA devices..."
    
    DEVICES_RESPONSE=$(curl -s -X GET "${API_BASE_URL}/v1/auth/mfa/devices" \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{
            \"tenantId\": \"${TENANT_ID}\"
        }")
    
    log_info "Response:"
    if command -v jq &> /dev/null; then
        echo "$DEVICES_RESPONSE" | jq '.'
    else
        echo "$DEVICES_RESPONSE"
    fi
}

# Remove MFA device
remove_device() {
    if [ -z "$1" ]; then
        log_error "Device ID required for removal"
        log_info "Usage: remove_device <device-id>"
        return 1
    fi
    
    local DEVICE_ID="$1"
    
    log_info "Removing MFA device: $DEVICE_ID"
    
    REMOVE_RESPONSE=$(curl -s -X DELETE "${API_BASE_URL}/v1/auth/mfa/devices/${DEVICE_ID}" \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{
            \"tenantId\": \"${TENANT_ID}\"
        }")
    
    if echo "$REMOVE_RESPONSE" | jq -e '.success' &> /dev/null; then
        log_success "Device removed successfully"
        return 0
    else
        log_error "Failed to remove device"
        log_info "Response: $REMOVE_RESPONSE"
        return 1
    fi
}

# Interactive menu
show_menu() {
    echo ""
    echo "=== DataShield MFA Testing Tool ==="
    echo "1) Setup TOTP (generate QR code)"
    echo "2) Verify TOTP code"
    echo "3) Test login with MFA"
    echo "4) List MFA devices"
    echo "5) Remove MFA device"
    echo "6) Run full test sequence"
    echo "7) Exit"
    echo ""
}

# Full test sequence
run_full_sequence() {
    log_info "Starting full MFA test sequence..."
    
    log_info "Step 1: Setting up TOTP"
    setup_totp
    
    echo ""
    log_info "Step 2: Waiting for user input"
    log_warning "Please perform these steps:"
    log_warning "  1. Open the MFA QR code (saved as mfa_qr_code.png)"
    log_warning "  2. Scan with Google Authenticator app"
    log_warning "  3. Get the 6-digit code from the app"
    log_warning "  4. Enter it when prompted"
    echo ""
    read -p "Enter 6-digit TOTP code from authenticator: " TOTP_CODE
    
    if [ -z "$TOTP_CODE" ]; then
        log_error "No code provided"
        return 1
    fi
    
    log_info "Step 3: Verifying TOTP code"
    if verify_totp "$TOTP_CODE"; then
        log_success "TOTP verification passed!"
    else
        log_error "TOTP verification failed"
        return 1
    fi
    
    echo ""
    read -p "Enter 6-digit TOTP code again for login test: " TOTP_CODE_LOGIN
    
    if [ -z "$TOTP_CODE_LOGIN" ]; then
        log_error "No code provided"
        return 1
    fi
    
    log_info "Step 4: Testing login with MFA"
    if login_with_mfa "$TOTP_CODE_LOGIN"; then
        log_success "Login with MFA passed!"
    else
        log_error "Login with MFA failed"
        return 1
    fi
    
    log_success "Full test sequence completed successfully!"
}

# Main execution
main() {
    if [ $# -eq 0 ]; then
        # Interactive mode
        check_prerequisites
        authenticate
        
        while true; do
            show_menu
            read -p "Choose option (1-7): " CHOICE
            
            case $CHOICE in
                1) setup_totp ;;
                2) 
                    read -p "Enter 6-digit TOTP code: " CODE
                    verify_totp "$CODE"
                    ;;
                3) 
                    read -p "Enter 6-digit TOTP code: " CODE
                    login_with_mfa "$CODE"
                    ;;
                4) list_devices ;;
                5) 
                    read -p "Enter device ID to remove: " DEVICE_ID
                    remove_device "$DEVICE_ID"
                    ;;
                6) run_full_sequence ;;
                7) 
                    log_info "Exiting..."
                    exit 0
                    ;;
                *) log_error "Invalid option" ;;
            esac
        done
    else
        # Command mode
        check_prerequisites
        
        case "$1" in
            setup)
                authenticate
                setup_totp
                ;;
            verify)
                authenticate
                verify_totp "$2"
                ;;
            login)
                login_with_mfa "$2"
                ;;
            list)
                authenticate
                list_devices
                ;;
            remove)
                authenticate
                remove_device "$2"
                ;;
            test-full)
                authenticate
                run_full_sequence
                ;;
            *)
                log_error "Unknown command: $1"
                log_info "Available commands: setup, verify, login, list, remove, test-full"
                exit 1
                ;;
        esac
    fi
}

# Run main
main "$@"
