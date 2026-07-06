# Security Audit Checklist - DataShield India

**Version:** 1.0.0  
**Date:** 2026-06-24  
**Status:** Pre-Production Audit  
**Compliance:** DPDP Act 2023, OWASP ASVS L2  

---

## 1. Authentication & Authorization

### JWT Security
- [ ] JWT tokens use strong signing algorithm (RS256/ES256)
- [ ] JWT tokens have reasonable expiration (15-60 minutes)
- [ ] Refresh tokens implemented with secure storage
- [ ] Token revocation mechanism in place
- [ ] No sensitive data in JWT payload
- [ ] JWT secret keys rotated regularly (90 days)

### Password Security
- [ ] Passwords hashed with bcrypt (cost factor ≥ 12)
- [ ] Minimum password length: 8 characters
- [ ] Password complexity requirements enforced
- [ ] Account lockout after failed attempts (5 attempts)
- [ ] Password reset uses secure tokens (time-limited)
- [ ] No password transmitted in URLs or logs

### Session Management
- [ ] Session timeout configured (30 minutes inactivity)
- [ ] Secure session cookies (HttpOnly, Secure, SameSite)
- [ ] Session invalidation on logout
- [ ] Concurrent session control
- [ ] Session fixation protection

### Multi-Factor Authentication (MFA)
- [ ] MFA available for admin accounts
- [ ] OTP generation uses TOTP standard
- [ ] SMS-based OTP for data principals
- [ ] MFA recovery codes provided
- [ ] Rate limiting on OTP attempts

---

## 2. Data Protection

### Encryption at Rest
- [ ] Database encryption enabled (AES-256)
- [ ] S3 bucket encryption enabled (SSE-S3 or SSE-KMS)
- [ ] Redis data encrypted
- [ ] Backup files encrypted
- [ ] Encryption keys stored in Vault/Secrets Manager
- [ ] Per-tenant encryption keys (enterprise tier)

### Encryption in Transit
- [ ] TLS 1.3 enforced (minimum TLS 1.2)
- [ ] Valid SSL/TLS certificates
- [ ] HSTS header configured (max-age=31536000)
- [ ] Certificate pinning for mobile apps
- [ ] No mixed content warnings
- [ ] Database connections use SSL/TLS

### Data Minimization
- [ ] Only necessary PII collected
- [ ] PII inventory documented
- [ ] Data retention policies implemented
- [ ] Automated data deletion after retention period
- [ ] PII access logging enabled
- [ ] Data classification labels applied

### Data Masking
- [ ] PII masked in logs
- [ ] Credit card numbers masked (last 4 digits visible)
- [ ] Aadhaar numbers masked (last 4 digits visible)
- [ ] Email addresses partially masked
- [ ] Phone numbers partially masked

---

## 3. Input Validation

### Server-Side Validation
- [ ] All inputs validated on server-side
- [ ] Input length limits enforced
- [ ] Input type validation (email, phone, etc.)
- [ ] Whitelist validation for enums
- [ ] Null/undefined checks
- [ ] SQL injection protection (parameterized queries)
- [ ] NoSQL injection protection
- [ ] XML/JSON injection protection

### Client-Side Validation
- [ ] Client-side validation for UX only
- [ ] Never trust client-side validation alone
- [ ] JavaScript validation cannot be bypassed

### File Upload Security
- [ ] File type validation (whitelist)
- [ ] File size limits (max 10MB)
- [ ] Malware scanning on upload
- [ ] Files stored outside web root
- [ ] Unique file names generated
- [ ] Content-Type verification

---

## 4. API Security

### Authentication
- [ ] All APIs require authentication (except public endpoints)
- [ ] API keys rotated regularly
- [ ] No hardcoded credentials in code
- [ ] Bearer token in Authorization header

### Rate Limiting
- [ ] Rate limiting per IP (100 req/min)
- [ ] Rate limiting per user (1000 req/hour)
- [ ] Exponential backoff on failures
- [ ] Rate limit headers returned (X-RateLimit-*)

### CORS Policy
- [ ] CORS configured for specific origins
- [ ] No wildcard (*) origins in production
- [ ] Credentials allowed only for trusted origins
- [ ] Preflight requests handled correctly

### API Versioning
- [ ] API versioning in URL (/api/v1/)
- [ ] Deprecated API versions documented
- [ ] Sunset header for deprecated APIs

### Error Handling
- [ ] No stack traces in API responses
- [ ] Generic error messages for users
- [ ] Detailed errors logged server-side only
- [ ] HTTP status codes used correctly

---

## 5. Infrastructure Security

### Network Security
- [ ] VPC configured with private subnets
- [ ] Security groups restrict inbound traffic
- [ ] Only required ports open (80, 443, 8001-8027)
- [ ] Bastion host for SSH access
- [ ] No direct internet access to databases
- [ ] WAF rules configured

### Container Security
- [ ] Base images from trusted sources
- [ ] Images scanned for vulnerabilities (Trivy, Clair)
- [ ] No root user in containers
- [ ] Resource limits configured
- [ ] Secrets not in container images
- [ ] Images signed and verified

### Kubernetes Security
- [ ] RBAC enabled
- [ ] Network policies configured
- [ ] Pod security policies enforced
- [ ] Secrets stored in Kubernetes Secrets
- [ ] Service accounts used for pods
- [ ] Admission controllers configured

---

## 6. Logging & Monitoring

### Audit Logging
- [ ] All authentication attempts logged
- [ ] All PII access logged
- [ ] All consent changes logged
- [ ] All DSAR requests logged
- [ ] All admin actions logged
- [ ] Logs immutable (hash-chained)

### Log Security
- [ ] Logs do not contain PII
- [ ] Logs do not contain credentials
- [ ] Logs encrypted at rest
- [ ] Log retention: 7 years (compliance)
- [ ] Log tampering detection

### Monitoring
- [ ] Failed login attempts monitored
- [ ] Unusual API activity monitored
- [ ] Resource exhaustion monitored
- [ ] Error rate spikes alerted
- [ ] Security events trigger alerts
- [ ] SOC integration (SIEM)

---

## 7. Compliance (DPDP Act 2023)

### Data Subject Rights
- [ ] Right to access implemented
- [ ] Right to correction implemented
- [ ] Right to erasure implemented
- [ ] Right to portability implemented
- [ ] Consent withdrawal mechanism
- [ ] Grievance redressal (30-day SLA)

### Breach Notification
- [ ] Breach detection mechanisms
- [ ] 72-hour notification to DPBI
- [ ] Breach notification to data principals
- [ ] Breach incident tracking
- [ ] Post-incident analysis

### Consent Management
- [ ] Granular consent (purpose-specific)
- [ ] Consent records timestamped
- [ ] Consent withdrawal one-click
- [ ] Parental consent for minors
- [ ] Consent audit trail

---

## 8. Third-Party Dependencies

### Dependency Management
- [ ] Dependency vulnerability scanning (OWASP Dependency-Check)
- [ ] No known critical vulnerabilities (CVE)
- [ ] Dependencies updated regularly
- [ ] License compliance checked
- [ ] Minimal dependencies used

### Third-Party Services
- [ ] DPA (Data Processing Agreement) signed
- [ ] Third-party security assessment completed
- [ ] Data transfer agreements in place
- [ ] Third-party access logged
- [ ] Third-party risk scored

---

## 9. Secure Development

### Code Security
- [ ] No hardcoded secrets
- [ ] No commented-out sensitive code
- [ ] Environment variables for config
- [ ] Code reviews mandatory
- [ ] Security linting enabled (SonarQube)
- [ ] Static analysis (SAST) in CI/CD

### Git Security
- [ ] No secrets in git history
- [ ] Git commit signing enabled
- [ ] Branch protection rules
- [ ] No force pushes to main
- [ ] Pull request reviews required

---

## 10. Incident Response

### Preparation
- [ ] Incident response plan documented
- [ ] Security team identified
- [ ] Escalation procedures defined
- [ ] Communication templates prepared
- [ ] Runbooks for common scenarios

### Detection & Analysis
- [ ] Security monitoring 24/7
- [ ] Alert response SLA (< 15 min)
- [ ] Incident classification criteria
- [ ] Forensic tools available

### Containment & Recovery
- [ ] Isolation procedures documented
- [ ] Rollback procedures tested
- [ ] Backup restoration tested
- [ ] Post-incident review process

---

## Audit Results

### Critical Issues (Blocker)
- [ ] No critical issues found

### High Issues (Must Fix)
- [ ] List high severity issues here

### Medium Issues (Should Fix)
- [ ] List medium severity issues here

### Low Issues (Nice to Fix)
- [ ] List low severity issues here

---

## Sign-Off

**Security Auditor:** ___________________________  
**Date:** ___________________________  
**Status:** [ ] PASS  [ ] PASS WITH CONDITIONS  [ ] FAIL  

**Engineering Lead:** ___________________________  
**Date:** ___________________________  

**DPO Approval:** ___________________________  
**Date:** ___________________________  

---

**Next Audit Date:** 2026-12-24  
**Audit Frequency:** Quarterly
