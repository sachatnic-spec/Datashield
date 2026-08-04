# Defense-in-Depth Authentication - Code Audit Report

**Date:** August 4, 2026  
**Status:** Audit Complete - Fixes Required  
**Scope:** auth-service implementation  

---

## Executive Summary

The codebase has partial implementation of defense-in-depth authentication. Most components are present but some critical features need enhancement or implementation.

### Audit Findings

| Component | Status | Issue | Severity |
|-----------|--------|-------|----------|
| Password validation | ✅ Implemented | BCrypt/PasswordEncoder used | GREEN |
| MFA (TOTP/SMS/Email) | ⚠️ Partial | MFA check exists but needs validation logic | YELLOW |
| JWT tokens (short-lived) | ✅ Implemented | 1 hour expiry configured | GREEN |
| Refresh token rotation | ✅ Implemented | Token marked as USED, new issued | GREEN |
| Device/IP anomaly detection | ❌ Missing | No detection logic implemented | RED |
| MFA verification | ❌ Missing | No MFA code validation endpoint | RED |
| Token hashing | ⚠️ Issue | Using `token.hashCode()` instead of bcrypt | YELLOW |
| Refresh token with device context | ⚠️ Partial | IP/userAgent stored but not validated | YELLOW |

---

## Detailed Findings

### ✅ GOOD: Password + MFA Check

**File:** AuthService.java (lines 60-69)

```java
// Password validation with encoder
if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new UnauthorizedException("Invalid email or password");
}

// MFA check
if (user.getMfaEnabled() && (request.getMfaCode() == null || request.getMfaCode().isEmpty())) {
    throw new UnauthorizedException("MFA code is required");
}
```

**Status:** ✅ Good - Correct structure
**Issue:** MFA code validation missing (just checks if provided, not if correct)

---

### ✅ GOOD: Short-Lived JWT Access Token

**File:** application.yml (line 75)

```yaml
datasheild:
  auth:
    jwt:
      expiration: 3600000  # 1 hour ✅
```

**Status:** ✅ Good - 1 hour is appropriate for access token

---

### ✅ GOOD: Rotating Refresh Tokens

**File:** AuthService.java (lines 129-151)

```java
// Mark old token as used
storedToken.setStatus(RefreshToken.TokenStatus.USED);
refreshTokenRepository.save(storedToken);

// Generate new tokens
String newAccessToken = jwtTokenProvider.generateAccessToken(...);
String newRefreshToken = jwtTokenProvider.generateRefreshToken(...);

// Create new refresh token record
RefreshToken newStoredToken = RefreshToken.builder()
    .tokenHash(hashToken(newRefreshToken))
    .status(RefreshToken.TokenStatus.VALID)
    .expiresAt(LocalDateTime.now().plusSeconds(604800))
    .build();
```

**Status:** ✅ Good - Proper rotation with USED status

---

### ⚠️ ISSUE: Token Hashing Using hashCode()

**File:** AuthService.java (lines 186-189)

```java
private String hashToken(String token) {
    // Simple SHA-256 hash for storage (in production, use bcrypt)
    return Integer.toHexString(token.hashCode());  // ❌ WEAK - Uses hashCode()
}
```

**Status:** ❌ Security Issue
**Issue:** `hashCode()` is not cryptographically secure
**Fix:** Use BCrypt or SHA-256 properly

---

### ❌ MISSING: MFA Code Validation

**What's Missing:**
1. No endpoint to verify MFA code (TOTP/SMS/Email)
2. No TOTP validation logic
3. No SMS/Email OTP sending

**Current State:**
```java
if (user.getMfaEnabled() && (request.getMfaCode() == null || request.getMfaCode().isEmpty())) {
    throw new UnauthorizedException("MFA code is required");  // Only checks if provided
}
// But doesn't validate the MFA code!
```

**Fix Required:**
- Add MFA code validation logic
- Add TOTP verification service
- Add SMS/Email OTP service

---

### ❌ MISSING: Device/IP Anomaly Detection

**What's Missing:**
1. No device fingerprinting from client
2. No IP address tracking on login
3. No anomaly detection logic
4. No step-up authentication trigger

**Current State:**
```java
// RefreshToken has ipAddress and userAgent fields (line 55-58 in RefreshToken.java)
@Column(length = 50)
private String ipAddress;

@Column(length = 500)
private String userAgent;
// But never populated or validated!
```

**Fix Required:**
- Capture IP address on login
- Capture device fingerprint
- Compare with known devices
- Trigger step-up auth if anomaly detected

---

### ⚠️ ISSUE: No Device Context in Login Flow

**What's Missing:**
- IP address not captured during login
- User agent not captured
- Device fingerprint not generated
- No device tracking in Session entity

**File:** AuthService.java (line 82-90)

```java
Session session = Session.builder()
    .userId(user.getId())
    .tenantId(tenantId)
    .accessToken(accessToken)
    .tokenHash(hashToken(accessToken))
    .refreshToken(refreshToken)
    .expiresAt(LocalDateTime.now().plusSeconds(3600))
    .status(Session.SessionStatus.ACTIVE)
    .build();
    // ❌ Missing: ipAddress, userAgent, deviceFingerprint
```

---

## Defense-in-Depth Policy vs Implementation

| Component | Policy | Implementation | Gap |
|-----------|--------|---|---|
| Password + MFA | ✅ Both required | ✅ Check exists | ✅ Code validation missing |
| Short-lived JWT | ✅ < 1 hour | ✅ 1 hour configured | ✅ None |
| Rotating refresh token | ✅ On each refresh | ✅ Implemented | ✅ None |
| Device tracking | ✅ Required | ⚠️ Partial (fields only) | ❌ No detection logic |
| IP anomaly detection | ✅ Required | ❌ Not implemented | ❌ No detection logic |
| Token hashing | ✅ Secure hash | ❌ Using hashCode() | ❌ Weak implementation |

---

## Fixes Required (Priority Order)

### Priority 1: CRITICAL

1. **Fix Token Hashing** (Security Issue)
   - Replace `hashCode()` with BCrypt
   - File: AuthService.java (line 186-189)

2. **Implement MFA Code Validation**
   - Add TOTP verification
   - File: Add new MFAService.java

3. **Implement Device/IP Tracking**
   - Capture IP on login
   - Capture user agent
   - File: AuthService.java

### Priority 2: IMPORTANT

4. **Add Device Anomaly Detection**
   - Compare login location with known devices
   - Flag unusual activity
   - File: Add new DeviceAnomalyService.java

5. **Update Session Entity**
   - Add ipAddress, userAgent, deviceFingerprint
   - File: Session.java

6. **Add Step-Up Authentication**
   - Require additional auth for suspicious logins
   - File: Add new step-up auth endpoint

### Priority 3: ENHANCEMENT

7. **Add MFA Enrollment Endpoints**
   - Setup TOTP
   - Manage MFA devices
   - File: AuthController.java

---

## Testing Strategy

After fixes, test:
- MFA code validation (valid/invalid codes)
- Token refresh with new access token
- Device tracking (IP/user agent capture)
- Anomaly detection (new device login)
- Token hashing (verify not using hashCode)

---

## Code Changes Required

### Files to Modify:
1. AuthService.java (token hashing, device tracking)
2. Session.java (add device fields)
3. AuthController.java (add MFA endpoints)
4. application.yml (add MFA configuration)

### Files to Create:
1. MFAService.java (TOTP/SMS/Email validation)
2. DeviceAnomalyService.java (anomaly detection)
3. MFAController.java (MFA enrollment endpoints)

