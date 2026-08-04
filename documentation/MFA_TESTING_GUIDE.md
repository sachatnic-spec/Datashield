# MFA Testing Guide - Local Development

## Quick Start: Test TOTP Validation

### Step 1: Download Google Authenticator or Authy
- **Google Authenticator** (iOS/Android/Linux): Free
- **Authy** (iOS/Android/Desktop): Free with multi-device support
- **Microsoft Authenticator**: Free
- **Totp Lite**: Free Linux app

### Step 2: Generate TOTP Secret (Base32 Format)

For testing, you can use this example TOTP secret:
```
Base32 Secret: JBSWY3DPEBXG64TMMQ======
```

Convert to bytes for testing:
```
Decoded (hex): 2406b1c8e7a5c1be5c1adcc99cb77ec1c2c
```

### Step 3: Add to Authenticator App

1. Open Google Authenticator
2. Tap the `+` icon
3. Select "Manual Entry"
4. Enter:
   - Account name: `alice@datasheild.io`
   - Key: `JBSWY3DPEBXG64TMMQ======`
   - Type: `Time-based`
   - Time step: `30`
5. Tap "Add"
6. Get your 6-digit code

### Step 4: Test Login with MFA

```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "123456"  # Use code from Authenticator app
  }' \
  -H "X-Forwarded-For: 192.168.1.100"
```

### Step 5: Verify Response

Success response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "email": "alice@datasheild.io",
    "mfaEnabled": true
  }
}
```

Failure response:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid MFA code"
}
```

---

## Test Scenarios

### Scenario 1: Successful TOTP Validation

1. User: `alice@datasheild.io`, Password: `password123`
2. Open Authenticator app, note the 6-digit code (e.g., `632189`)
3. Login with that code
4. ✅ Expected: Session created with IP tracking

```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "632189"
  }' \
  -H "X-Forwarded-For: 192.168.1.100" \
  -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
```

---

### Scenario 2: Invalid TOTP Code

1. User: `alice@datasheild.io`, Password: `password123`
2. Intentionally use wrong code (e.g., `000000`)
3. ❌ Expected: "Invalid MFA code" error + failed attempts incremented

```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "000000"
  }'
```

Repeat 5 times → Device should be locked

---

### Scenario 3: MFA Code Required

1. User: `alice@datasheild.io`, Password: `password123`
2. Login WITHOUT mfaCode parameter
3. ❌ Expected: "MFA code is required" error

```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### Scenario 4: IP Change Detection

1. **First Login** from `192.168.1.100`:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "632189"
  }' \
  -H "X-Forwarded-For: 192.168.1.100"
```

2. **Second Login** from `203.0.113.50`:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "632189"
  }' \
  -H "X-Forwarded-For: 203.0.113.50"
```

3. ⚠️ Expected: Anomaly detection logs show "IP address change detected", risk level = MEDIUM

---

### Scenario 5: Device Change Detection

1. **First Login** from Chrome on Windows:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "632189"
  }' \
  -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/121.0"
```

2. **Second Login** from Safari on Mac:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "632189"
  }' \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Safari/605.1.15"
```

3. ⚠️ Expected: Anomaly detection logs show "Browser/OS change detected", risk level = MEDIUM

---

### Scenario 6: Token Rotation on Refresh

1. **Login** and get tokens:
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@datasheild.io",
    "password": "password123",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "mfaCode": "632189"
  }' \
  -H "X-Forwarded-For: 192.168.1.100"
```

Save the `refreshToken` from response.

2. **Refresh** using refresh token:
```bash
curl -X POST http://localhost:8081/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000"
  }' \
  -H "X-Forwarded-For: 192.168.1.100"
```

3. ✅ Expected:
   - New accessToken issued
   - New refreshToken issued (different from old)
   - Old token marked as USED

4. **Try to reuse old token**:
```bash
curl -X POST http://localhost:8081/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  # Old token
    "tenantId": "550e8400-e29b-41d4-a716-446655440000"
  }' \
  -H "X-Forwarded-For: 192.168.1.100"
```

5. ❌ Expected: "Invalid or expired refresh token" error

---

## Database Queries for Verification

### Check MFA Device Status
```sql
SELECT * FROM mfa_devices 
WHERE user_id = '550e8400-e29b-41d4-a716-446655440001'
AND tenant_id = '550e8400-e29b-41d4-a716-446655440000';

-- Expected columns:
-- id | user_id | type (TOTP/SMS/EMAIL/WEBAUTHN)
-- secret | verified | active | failed_attempts | last_used_at
```

### Check Session with Device Context
```sql
SELECT * FROM sessions 
WHERE user_id = '550e8400-e29b-41d4-a716-446655440001'
ORDER BY created_at DESC LIMIT 1;

-- Expected columns:
-- id | user_id | access_token_hash | ip_address | user_agent | created_at
```

### Check Refresh Token Rotation
```sql
SELECT * FROM refresh_tokens 
WHERE user_id = '550e8400-e29b-41d4-a716-446655440001'
ORDER BY created_at DESC LIMIT 5;

-- Expected columns:
-- id | user_id | token_hash | status (VALID/USED/REVOKED)
-- ip_address | user_agent | created_at | expires_at
```

### Check Token Hash (SHA-256)
```sql
-- Token hashes should be Base64-encoded SHA-256, not simple hashCode()
-- Example hash: V1e3M5d9K2pQ7sX9nB4wL6tY8hJ1zR4vM7cF9eO2qW5x

SELECT token_hash FROM sessions LIMIT 1;
-- Should look like: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
-- Not like: 1234567890
```

---

## Logs to Monitor

### Successful TOTP Validation
```
INFO  - TOTP validation successful for user 550e8400-e29b-41d4-a716-446655440001 in tenant 550e8400-e29b-41d4-a716-446655440000
INFO  - MFA validation successful for user: alice@datasheild.io
INFO  - User successfully logged in: alice@datasheild.io in tenant: 550e8400-e29b-41d4-a716-446655440000 from IP: 192.168.1.100
```

### Failed MFA Attempts
```
WARN  - TOTP device locked after 5 failed attempts for user 550e8400-e29b-41d4-a716-446655440001
WARN  - Failed MFA validation for user: alice@datasheild.io in tenant: 550e8400-e29b-41d4-a716-446655440000
ERROR - UnauthorizedException: Invalid MFA code
```

### Anomaly Detection
```
WARN  - IP address change detected for user alice@datasheild.io - Old: 192.168.1.100, New: 203.0.113.50
INFO  - Medium-risk login detected for user: alice@datasheild.io - IP or device change detected
WARN  - High-risk login detected for user: alice@datasheild.io in tenant: 550e8400-e29b-41d4-a716-446655440000 from IP: 203.0.113.50
```

### Device Change
```
INFO  - Device fingerprinting: Extracted browser=Chrome, os=Windows
INFO  - Device fingerprinting: Extracted browser=Safari, os=Macintosh
INFO  - Medium-risk login detected for user: alice@datasheild.io - IP or device change detected
```

---

## Environment Variables

For development/testing, set these in `.env` or application-dev.yml:

```properties
# JWT Configuration
JWT_SECRET=dev-secret-key-32-chars-minimum-here
datasheild.auth.jwt.expiration=3600000
datasheild.auth.jwt.refresh-expiration=604800000

# MFA Configuration
datasheild.auth.mfa.enabled=true
datasheild.auth.mfa.totp-window=1  # ±1 window (30 seconds)
datasheild.auth.mfa.max-failed-attempts=5
datasheild.auth.mfa.lock-duration-minutes=15

# Anomaly Detection Configuration
datasheild.auth.device.anomaly-threshold-hours=24
datasheild.auth.device.min-travel-time-minutes=30

# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/datasheild_auth_dev
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Troubleshooting

### Issue: "MFA device not found for user"
**Cause:** User doesn't have an active MFA device configured  
**Solution:** 
- Check MFA device is inserted in database
- Verify `active = true` and `verified = true`
- Check `mfa_device.type = 'TOTP'`

### Issue: "TOTP device not verified"
**Cause:** Device exists but not verified  
**Solution:**
- In database: `UPDATE mfa_devices SET verified = true WHERE id = '...'`

### Issue: "Invalid MFA code" on correct code
**Cause:** Clock skew or wrong secret  
**Solution:**
- Verify TOTP window setting: `datasheild.auth.mfa.totp-window=1` allows ±1 window
- Check system time is synchronized (NTP)
- Verify secret in database matches Authenticator app

### Issue: "MFA code must be 6 digits"
**Cause:** Input validation failed  
**Solution:**
- Ensure code is exactly 6 digits (e.g., `000001` not `1`)
- No spaces or dashes

### Issue: "Device anomaly detection failed"
**Cause:** DeviceAnomalyService threw exception  
**Solution:**
- Check logs for stack trace
- Verify SessionRepository has required query method
- Check user has sessions in database

---

## Performance Testing

### Load Test: 1000 Concurrent Logins with MFA

```bash
# Using Apache Bench
ab -n 1000 -c 100 \
  -p login-payload.json \
  -T "application/json" \
  http://localhost:8081/v1/auth/login

# Expected:
# - Response time: < 500ms per request
# - Success rate: > 99%
# - CPU usage: < 80%
# - Memory: < 2GB
```

### Token Generation Latency

Measure JWT generation time:
```
Baseline: < 5ms per token
With device tracking: < 10ms per token
With anomaly detection: < 15ms per token
```

---

## What's Next

After confirming MFA works locally:

1. **Implement MFA Enrollment**
   - POST `/v1/auth/mfa/totp/setup` - Generate QR code
   - POST `/v1/auth/mfa/totp/verify` - Verify user setup

2. **Add SMS/Email OTP**
   - Integrate Twilio SDK
   - Implement Redis cache for OTP codes

3. **Implement Step-Up Authentication**
   - Trigger on HIGH risk anomalies
   - Require email/SMS verification

4. **Production Deployment**
   - Test with load balancer (X-Forwarded-For header)
   - Monitor anomaly detection for false positives
   - Tune risk thresholds based on user behavior

