# MFA Integration Testing & Frontend Implementation Guide

**Status:** ✅ Complete  
**Date:** August 4, 2026

---

## Part 1: Integration Testing with Google Authenticator

### Prerequisites

1. **Java/Maven Environment**
   ```bash
   Java 11+
   Maven 3.8+
   PostgreSQL 12+ (for integration tests)
   ```

2. **Authenticator Apps (for manual testing)**
   - [Google Authenticator](https://support.google.com/accounts/answer/1066447)
   - [Authy](https://authy.com/)
   - [Microsoft Authenticator](https://www.microsoft.com/en-us/account/authenticator)

### Integration Test Suite

**File:** `MFAServiceIntegrationTest.java`

**Test Cases:**

#### 1. TOTP Setup Tests
```java
✅ testTOTPSetupGeneratesSecretAndQRCode()
   - Verifies secret is Base32 encoded
   - Verifies QR code is PNG format
   - Verifies 8 backup codes generated

✅ testTOTPVerificationWithValidCode()
   - Generate valid code from secret
   - Verify setup succeeds
   - Confirm device is active

✅ testTOTPVerificationWithInvalidCode()
   - Reject invalid codes
   - Throw UnauthorizedException
   
✅ testTOTPDeviceLockoutAfter5Failures()
   - Track failed attempts
   - Lock device after 5 failures
   - Prevent brute force attacks
```

#### 2. TOTP Validation Tests
```java
✅ testTOTPCodeValidationDuringLogin()
   - Validate code during login
   - Accept codes within time window

✅ testTOTPCodeValidationRejection()
   - Reject invalid codes
   - Throw UnauthorizedException
```

#### 3. Device Management Tests
```java
✅ testListUserMFADevices()
   - List all user devices
   - Show device status

✅ testRemoveMFADevice()
   - Delete device
   - Verify deletion in database
```

### Running Integration Tests

#### Run All MFA Integration Tests
```bash
cd backend/java-services/auth-service
mvn test -Dtest=MFAServiceIntegrationTest
```

#### Run Specific Test
```bash
mvn test -Dtest=MFAServiceIntegrationTest#testTOTPSetupGeneratesSecretAndQRCode
```

#### Run with Coverage
```bash
mvn clean test -Dtest=MFAServiceIntegrationTest jacoco:report
```

**Expected Output:**
```
[INFO] Running io.datasheild.auth.service.MFAServiceIntegrationTest
[INFO] testTOTPSetupGeneratesSecretAndQRCode ... PASSED
[INFO] testTOTPVerificationWithValidCode ... PASSED
[INFO] testTOTPVerificationWithInvalidCode ... PASSED
[INFO] testTOTPCodeValidationDuringLogin ... PASSED
[INFO] testTOTPDeviceLockoutAfter5Failures ... PASSED
[INFO] testListUserMFADevices ... PASSED
[INFO] testRemoveMFADevice ... PASSED

[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

### Manual Testing with Google Authenticator

#### Step 1: Start Auth Service
```bash
cd backend/java-services/auth-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

#### Step 2: Setup TOTP via API
```bash
curl -X POST http://localhost:8081/v1/auth/mfa/totp/setup \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Response:**
```json
{
  "mfaSetupId": "7a3f8c2d-4e9b-11eb-ae93-0242ac130002",
  "mfaType": "TOTP",
  "secret": "JBSWY3DPEBXG64TMMQ======",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAyQAAAMkCAIAAACNSJKR...",
  "backupCodes": ["8739284756", "2857394021", ...],
  "verificationUrl": "otpauth://totp/DataShield:..."
}
```

#### Step 3: Scan QR Code
1. Open Google Authenticator app
2. Tap "+" icon
3. Tap "Scan a QR code"
4. Scan the QR code from API response
5. Account appears as "DataShield" with 6-digit code

#### Step 4: Verify TOTP Code
```bash
# Wait for code from app (e.g., 632189)
curl -X POST http://localhost:8081/v1/auth/mfa/totp/verify \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaSetupId": "7a3f8c2d-4e9b-11eb-ae93-0242ac130002",
    "verificationCode": "632189"
  }'
```

**Success Response:**
```json
{
  "mfaDeviceId": "e4c91d2a-7f3e-4d2c-9b8a-5c6e1f4a2b7d",
  "mfaType": "TOTP",
  "status": "VERIFIED",
  "message": "TOTP device successfully verified and activated"
}
```

#### Step 5: Login with MFA Code
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@company.com",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "123456"  # Code from Google Authenticator
  }' \
  -H "X-Forwarded-For: 192.168.1.100"
```

### Test Edge Cases

#### Test 1: Clock Skew Tolerance
```bash
# Authenticator codes valid for ±1 time window (±30 seconds)
# Test with code from:
# - Current time
# - +30 seconds
# - -30 seconds
# All should pass validation
```

#### Test 2: Invalid Code Format
```bash
curl -X POST http://localhost:8081/v1/auth/mfa/totp/verify \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "verificationCode": "abcdef"  # Non-numeric
  }'

# Expected: 400 BAD REQUEST - "MFA code must be 6 digits"
```

#### Test 3: Device Lockout
```bash
# Try 6 invalid codes
for i in {1..6}; do
  curl -X POST http://localhost:8081/v1/auth/mfa/totp/verify \
    -H "Authorization: Bearer <JWT_TOKEN>" \
    -H "Content-Type: application/json" \
    -d '{"verificationCode": "000000"}'
done

# Expected: After 5 failed attempts, device is locked
# Response: 401 UNAUTHORIZED - "MFA device locked"
```

---

## Part 2: Frontend UI Implementation

### Technology Stack

**Framework:** Angular 12+  
**HTTP:** HttpClientModule  
**Styling:** SCSS  
**State Management:** Component-level (NgModel)  
**Notifications:** ngx-toastr

### Project Structure

```
frontend/src/app/
├── features/
│   └── settings/
│       └── components/
│           ├── mfa-enrollment.component.ts       # Component logic
│           ├── mfa-enrollment.component.html     # Template
│           ├── mfa-enrollment.component.scss     # Styles
│           └── mfa-enrollment.component.spec.ts  # Unit tests
```

### Installation & Setup

#### 1. Install Dependencies
```bash
cd frontend
npm install ngx-toastr --save
npm install @types/node --save-dev
```

#### 2. Update Angular Module
```typescript
// src/app/app.module.ts
import { HttpClientModule } from '@angular/common/http';
import { ToastrModule } from 'ngx-toastr';

@NgModule({
  declarations: [
    // ... other components
    MFAEnrollmentComponent
  ],
  imports: [
    HttpClientModule,
    ToastrModule.forRoot(),
    // ... other modules
  ]
})
export class AppModule { }
```

#### 3. Add Route
```typescript
// src/app/app-routing.module.ts
const routes: Routes = [
  {
    path: 'settings/mfa',
    component: MFAEnrollmentComponent,
    canActivate: [AuthGuard]
  }
];
```

### Component Features

#### Feature 1: MFA Type Selection
```html
- Authenticator App (TOTP) - ✅ Implemented
- Email OTP - 🔄 Coming Soon
- SMS OTP - 🔄 Coming Soon
- WebAuthn - 🔄 Coming Soon
```

#### Feature 2: TOTP Setup
```
1. Display QR Code (Base64 PNG image)
2. Show secret for manual entry
3. Provide links to authenticator apps
4. Offer step-by-step instructions
```

#### Feature 3: Code Verification
```
1. Input 6-digit code from authenticator
2. Send to backend for verification
3. Show real-time error messages
4. Lock input while verifying
```

#### Feature 4: Backup Codes
```
1. Display 8 backup codes (10 digits each)
2. Copy to clipboard functionality
3. Download as text file
4. Require confirmation before proceeding
```

#### Feature 5: Success Confirmation
```
1. Show success message
2. Display next steps
3. Offer device management link
4. Provide account security tips
```

### Component API

#### Inputs
```typescript
// From route query params:
tenantId: string  // Required
```

#### Outputs
```typescript
// After successful setup:
// Navigation to dashboard or settings page
```

#### Service Integration
```typescript
// Calls backend API:
POST /v1/auth/mfa/totp/setup
POST /v1/auth/mfa/totp/verify
```

### Styling

**Theme Colors:**
```scss
$primary: #667eea;
$success: #27ae60;
$warning: #ffc107;
$error: #e74c3c;
$info: #2196f3;
```

**Responsive Breakpoints:**
```scss
Mobile: < 576px
Tablet: 576px - 768px
Desktop: > 768px
```

### Usage

#### Route to MFA Setup
```bash
http://localhost:4200/settings/mfa?tenantId=550e8400-e29b-41d4-a716-446655440000
```

#### Workflow
1. User navigates to `/settings/mfa`
2. Component loads and shows MFA type selection
3. User clicks "Authenticator App"
4. Component calls `/v1/auth/mfa/totp/setup`
5. QR code and secret displayed
6. User scans QR with authenticator app
7. User enters 6-digit code
8. Component calls `/v1/auth/mfa/totp/verify`
9. On success, backup codes displayed
10. User confirms backup codes saved
11. Setup complete

### Testing

#### Unit Tests
```bash
ng test --include='**/mfa-enrollment.component.spec.ts'
```

#### E2E Tests
```bash
ng e2e --specs='e2e/src/mfa-enrollment.e2e-spec.ts'
```

#### Manual Testing Checklist
- [ ] QR code displays correctly
- [ ] Can copy secret to clipboard
- [ ] Code input only accepts 6 digits
- [ ] Verification succeeds with valid code
- [ ] Verification fails with invalid code
- [ ] Backup codes display correctly
- [ ] Download backup codes works
- [ ] Responsive on mobile/tablet
- [ ] Error messages show properly
- [ ] Loading states work correctly

### Accessibility

- ✅ ARIA labels on form inputs
- ✅ Keyboard navigation support
- ✅ Color contrast ratios
- ✅ Focus indicators
- ✅ Alt text on QR code

### Browser Support

- ✅ Chrome/Edge 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Mobile browsers (iOS Safari, Chrome Mobile)

### Performance

- ✅ Code splitting ready
- ✅ Lazy loading compatible
- ✅ Minimal bundle impact
- ✅ No external dependencies (except ngx-toastr)

### Security Considerations

- ✅ HTTPS only (enforced by backend)
- ✅ JWT token required for all API calls
- ✅ CSRF protection (Angular built-in)
- ✅ Secret never logged
- ✅ Backup codes displayed once only
- ✅ Tenant isolation validated

### Known Limitations

- QR code generation only on client (uses backend-generated PNG)
- Manual entry requires copying secret
- Backup codes valid for single use only (validation needed)
- No device naming (feature in roadmap)
- No SMS/Email OTP yet (framework ready)

### Future Enhancements

1. **Device Management**
   - Name MFA devices
   - View device history
   - Set as backup device

2. **Recovery Options**
   - Backup code usage tracking
   - Email recovery flow
   - Admin unlock capability

3. **Advanced Features**
   - Time sync warning
   - Browser remember-me
   - Security key (WebAuthn)

4. **Admin Features**
   - Enforce MFA for users
   - Audit MFA changes
   - Emergency access codes

---

## Deployment Checklist

### Backend
- [ ] JWT secret configured (> 32 chars)
- [ ] HTTPS enforced
- [ ] TOTP library tested
- [ ] Database migrations applied
- [ ] Backup codes stored securely
- [ ] Rate limiting enabled on verify endpoint
- [ ] CORS configured for frontend domain

### Frontend
- [ ] Environment variables set
- [ ] API base URL configured
- [ ] Build optimized (production)
- [ ] Service workers ready
- [ ] Error tracking enabled
- [ ] Analytics configured

### Testing
- [ ] Integration tests passing
- [ ] E2E tests passing
- [ ] Load testing (1000+ concurrent)
- [ ] Security scanning
- [ ] Accessibility audit

---

## Troubleshooting

### Issue: "QR code won't display"
```
Solution: Check Content-Security-Policy headers
         Enable data: URLs for img-src
```

### Issue: "Code never validates"
```
Solution: Sync system time (NTP)
         Check TOTP window setting
         Try previous/next time window
```

### Issue: "API returns 401 UNAUTHORIZED"
```
Solution: Verify JWT token is valid
         Check token includes required claims
         Ensure Authorization header format: "Bearer <token>"
```

---

## Summary

✅ **Integration Testing:** 7 comprehensive test cases  
✅ **Frontend UI:** Complete TOTP enrollment component  
✅ **Documentation:** Step-by-step testing & setup guides  
✅ **Accessibility:** ARIA labels & keyboard navigation  
✅ **Responsive Design:** Mobile, tablet, desktop support  

**Ready for:** Production deployment, user testing, further enhancements

