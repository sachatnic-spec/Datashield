# DataShield Authentication Policy

**Effective Date:** August 2026  
**Version:** 1.0  
**Status:** Mandatory for all services  
**Distribution:** All engineering teams

---

## Executive Summary

**Principle: Authentication at Gateway Level Only**

All authentication validation occurs at the API Gateway. Individual microservices do NOT implement authentication — they trust pre-authenticated requests from the gateway. This policy applies to ALL 24 DataShield services without exception.

**Result:**
- Single point of authentication policy (easier to update)
- Simplified service code (no security boilerplate)
- Better security observability (centralized logging)
- Improved performance (no redundant token validation)

---

## Authentication Model

### The Flow

```
┌──────────────────────────────────────────────────────┐
│ Client sends request with JWT token                  │
│ POST /api/service/resource                           │
│ Header: Authorization: Bearer eyJhbGc...             │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
       ┌────────────────────────────────────┐
       │   API Gateway (Port 8080)          │
       │   Authentication Filter            │
       │   ┌──────────────────────────────┐ │
       │   │ 1. Parse JWT from header     │ │
       │   │ 2. Validate signature        │ │
       │   │ 3. Check expiration          │ │
       │   │ 4. Extract user claims       │ │
       │   │ 5. Add X-User-ID header      │ │
       │   │ 6. Add X-Tenant-ID header    │ │
       │   │ 7. Add X-User-Roles header   │ │
       │   │ 8. Route to service via LB   │ │
       │   └──────────────────────────────┘ │
       └────────────────────┬────────────────┘
                            │
             Authenticated request + Headers
                            │
         ┌──────────────────┴──────────────────┐
         ↓                                      ↓
    ┌─────────────────┐                   ┌──────────────┐
    │ Service A       │                   │ Service B    │
    │ (8001)          │                   │ (8002)       │
    │                 │                   │              │
    │ Trust Headers:  │                   │ Trust Headers│
    │ X-User-ID       │                   │ X-User-ID    │
    │ X-Tenant-ID     │                   │ X-Tenant-ID  │
    │ X-User-Roles    │                   │ X-User-Roles │
    │                 │                   │              │
    │ NO:             │                   │ NO:          │
    │ ✗ JWT parsing   │                   │ ✗ JWT parse  │
    │ ✗ Token validate│                   │ ✗ Validation │
    │ ✗ @PreAuthorize│                   │ ✗ @Secured   │
    └─────────────────┘                   └──────────────┘
```

### What Gets Guaranteed

| Guarantee | Who | When |
|-----------|-----|------|
| JWT signature is valid | Gateway | On every request |
| Token has not expired | Gateway | Before routing |
| User exists in system | Auth Service | During login |
| User identity verified | Gateway | Before adding X-User-ID header |
| X-User-ID header is authentic | Gateway | Added AFTER validation |

**Consequence:** Services receive requests ONLY if gateway already validated authentication.

---

## Mandatory Rules

### Rule 1: Services do NOT implement authentication

❌ **Forbidden:**
```java
@Configuration
@EnableWebSecurity  // ❌ NOT ALLOWED
public class SecurityConfig {
    // ❌ Remove this entirely
}
```

❌ **Forbidden:**
```java
String token = request.getHeader("Authorization").replace("Bearer ", "");
UUID userId = jwtTokenProvider.extractUserId(token);  // ❌ DON'T DO THIS
```

### Rule 2: Services read user context from headers

✅ **Required:**
```java
@GetMapping("/{id}")
public ResponseEntity<DataDTO> getData(
        @PathVariable UUID id,
        @RequestHeader("X-User-ID") UUID userId) {  // ✅ READ FROM HEADER
    
    // userId is already validated by gateway
    return ResponseEntity.ok(dataService.getData(id, userId));
}
```

### Rule 3: All service-to-service calls route through gateway

❌ **Forbidden:**
```java
// Direct URLs bypass gateway
String url = "http://user-service:8001/users/" + userId;
User user = restTemplate.getForObject(url, User.class);
```

✅ **Required:**
```java
// Load-balanced: routes through gateway + service discovery
@Autowired
@LoadBalanced
private RestTemplate restTemplate;

User user = restTemplate.getForObject(
    "lb://user-service/users/{userId}",  // ✅ Service name, not URL
    User.class,
    userId
);
```

### Rule 4: Services implement authorization, NOT authentication

❌ **Forbidden (Authentication):**
```java
// ❌ DON'T VALIDATE TOKEN (gateway already did)
if (!jwtTokenProvider.isValid(token)) {
    throw new UnauthorizedException("Invalid token");
}
```

✅ **Required (Authorization):**
```java
// ✅ DO CHECK PERMISSIONS (service-level business logic)
List<String> roles = Arrays.asList(
    request.getHeader("X-User-Roles").split(",")
);

if (!roles.contains("admin")) {
    throw new AccessDeniedException("Only admins can delete");
}
```

---

## Request Lifecycle

### Step 1: Client Logs In (First Time)

```
Client                  Gateway                  Auth Service
  │                        │                           │
  ├─POST /auth/login───────►│                          │
  │ {user, pass}            │                          │
  │                         ├──POST /login────────────►│
  │                         │ {user, pass}              │
  │                         │                          │
  │                         │ ◄─ 200 OK ───────────────┤
  │                         │ { token: JWT }            │
  │                         │                          │
  │ ◄─ 200 OK ──────────────┤                          │
  │ { token: JWT }          │                          │
  │                         │                          │
```

**JWT Contains:**
```json
{
  "sub": "user-uuid-here",
  "email": "user@example.com",
  "tenant": "tenant-uuid-here",
  "roles": ["viewer", "editor"],
  "iat": 1722710400,
  "exp": 1722796800
}
```

### Step 2: Client Uses Token (Subsequent Requests)

```
Client                  Gateway                Service
  │                        │                    │
  ├─GET /api/service/data─►│                    │
  │ Bearer token in header  │                    │
  │                        │                    │
  │                 [Gateway Auth Filter]       │
  │                1. Parse JWT                 │
  │                2. Verify signature          │
  │                3. Check expiration          │
  │                4. Extract claims            │
  │                5. Add X-User-ID header      │
  │                        │                    │
  │                        ├──GET /data────────►│
  │                        │ X-User-ID: user-id │
  │                        │ X-Tenant-ID: tenant│
  │                        │ X-User-Roles: list │
  │                        │                    │
  │                        │ ◄──200 OK ────────┤
  │                        │ { data payload }   │
  │                        │                    │
  │ ◄─ 200 OK ────────────┤                    │
  │ { data payload }       │                    │
  │                        │                    │
```

### Step 3: Service Receives Request

```java
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    @GetMapping
    public ResponseEntity<List<DataDTO>> getData(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-Roles") String rolesStr) {
        
        // At this point:
        // ✅ userId is verified by gateway
        // ✅ tenantId is verified by gateway
        // ✅ rolesStr is extracted from token
        // ✅ Request reached service = authentication passed
        
        List<String> roles = Arrays.asList(rolesStr.split(","));
        
        // Check authorization (service level)
        if (!roles.contains("viewer")) {
            throw new AccessDeniedException("No access");
        }
        
        // Continue with business logic
        return ResponseEntity.ok(
            dataService.getAllDataForTenant(tenantId)
        );
    }
}
```

---

## Headers Added by Gateway

### X-User-ID
- **Type:** UUID (string)
- **Added by:** Gateway auth filter
- **When:** After JWT validation
- **Example:** `1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p`
- **Guaranteed:** Yes — gateway validated before adding
- **Service use:** Extract user ID for queries

### X-Tenant-ID
- **Type:** UUID (string)
- **Added by:** Gateway auth filter (extracted from JWT `tenant` claim)
- **When:** After JWT validation
- **Example:** `9z8y7x6w-5v4u-3t2s-1r0q-p0o9n8m7l6k5`
- **Guaranteed:** Yes — verified from JWT claims
- **Service use:** Filter all queries to tenant scope

### X-User-Email
- **Type:** String
- **Added by:** Gateway auth filter (extracted from JWT `email` claim)
- **When:** After JWT validation
- **Example:** `user@example.com`
- **Guaranteed:** Yes — from verified JWT
- **Service use:** Logging, auditing, display

### X-User-Roles
- **Type:** Comma-separated string
- **Added by:** Gateway auth filter (extracted from JWT `roles` array)
- **When:** After JWT validation
- **Example:** `admin,viewer,editor`
- **Guaranteed:** Yes — from verified JWT
- **Service use:** Authorization checks, role-based logic

### X-Request-ID
- **Type:** UUID (string)
- **Added by:** Gateway (for tracing)
- **When:** On every request
- **Example:** `req-12345678-90ab-cdef-1234-567890abcdef`
- **Service use:** Logging, tracing, debugging

---

## Authorization (Service Level)

### Difference: Authentication vs Authorization

| Aspect | Authentication | Authorization |
|--------|---|---|
| **Question** | "Are you who you claim to be?" | "Are you allowed to do this?" |
| **Where** | Gateway (centralized) | Service (business logic) |
| **Who checks** | Auth service + Gateway | Business logic in service |
| **What to check** | JWT signature, expiration | Roles, permissions, ownership |
| **Tools** | JwtTokenProvider | Service business logic |

### Example: Authentication (Gateway)
```java
// In Gateway's AuthFilter
String token = extractBearerToken(request);
if (!jwtTokenProvider.validateSignature(token)) {
    return error401("Invalid signature");  // Reject before routing
}
if (jwtTokenProvider.isExpired(token)) {
    return error401("Token expired");       // Reject before routing
}

// Token is valid, add headers and route
```

### Example: Authorization (Service)
```java
// In Service Controller
public ResponseEntity<DataDTO> deleteData(
        @PathVariable UUID dataId,
        @RequestHeader("X-User-ID") UUID userId,
        @RequestHeader("X-User-Roles") String rolesStr) {
    
    // Authorization check (business logic)
    List<String> roles = Arrays.asList(rolesStr.split(","));
    
    // Can user delete? (Only owner or admin)
    DataDTO data = dataService.getDataById(dataId);
    if (!data.getOwnerId().equals(userId) && !roles.contains("admin")) {
        return ResponseEntity.status(403)
            .body("Only owner or admin can delete");
    }
    
    dataService.delete(dataId);
    return ResponseEntity.ok().build();
}
```

---

## Implementation Checklist for Each Service

### ✅ To Implement
- [ ] Add `@RequestHeader("X-User-ID")` parameter to controllers
- [ ] Extract user/tenant context from headers
- [ ] Create AuthContext injectable bean (optional but recommended)
- [ ] Add interceptor to populate AuthContext from headers
- [ ] Create @LoadBalanced RestTemplate bean
- [ ] Use `lb://service-name` URLs for service-to-service calls
- [ ] Implement authorization checks in service logic

### ❌ To Remove
- [ ] Delete SecurityConfig.java class
- [ ] Delete JwtTokenProvider.java class
- [ ] Remove `@PreAuthorize` annotations from methods
- [ ] Remove `@EnableWebSecurity` annotation
- [ ] Remove Spring Security from pom.xml
- [ ] Remove JWT parser libraries from pom.xml
- [ ] Remove any HttpServletRequest token parsing code
- [ ] Remove `@Secured` annotations

---

## Deployment Checklist

### Before Deploying Service Update
- [ ] Auth Service (8001) is running
- [ ] API Gateway (8080) is running with auth filter configured
- [ ] Eureka Service Discovery (8761) is running
- [ ] Service has `@EnableDiscoveryClient` annotation
- [ ] Service has `@LoadBalanced` RestTemplate bean
- [ ] Spring Boot and Spring Cloud versions aligned

### After Deploying Service Update
- [ ] Service appears in Eureka with "UP" status
- [ ] Gateway routes to service successfully
- [ ] Can authenticate through gateway
- [ ] Service accepts X-User-ID header in requests
- [ ] Service-to-service calls use `lb://` URLs
- [ ] No Spring Security errors in logs
- [ ] No JWT parsing errors in logs

---

## Troubleshooting

### Problem: 401 Unauthorized at gateway
**Cause:** Invalid or expired JWT  
**Fix:** 
1. Get new token from `/api/auth/login`
2. Check token expiration
3. Verify auth service is running

### Problem: Missing X-User-ID header in service
**Cause:** Request bypassed gateway or gateway auth filter failed  
**Fix:**
1. Verify all requests go through gateway (`lb://` URLs)
2. Check gateway auth filter is configured
3. Check Eureka has service registered

### Problem: 403 Forbidden in service
**Cause:** Service-level authorization failed  
**Fix:**
1. Check user has required role in JWT
2. Check service auth check logic
3. Verify X-User-Roles header is present

### Problem: Service-to-service call fails
**Cause:** Direct URL used instead of `lb://` or wrong service name  
**Fix:**
```java
// ❌ WRONG
String url = "http://other-service:8002/endpoint";

// ✅ RIGHT
String url = "lb://other-service/endpoint";
```

### Problem: JWT parsing error in service
**Cause:** Service trying to re-validate token (violates policy)  
**Fix:** Remove JWT parsing code, use headers from gateway:
```java
// ❌ REMOVE THIS
UUID userId = jwtTokenProvider.extractUserId(token);

// ✅ USE THIS
@RequestHeader("X-User-ID") UUID userId
```

---

## Exceptions & Special Cases

### Exception: External Integrations (SIEM, Webhooks)
**Policy:** Hardcoded URLs acceptable (not internal services)
- SIEM integration (external system)
- Webhook endpoints (external consumers)

### Exception: Development/Testing
**Policy:** Localhost calls to services acceptable for testing
- Unit tests
- Local integration tests
- Dev environment testing

### Not an Exception: Service-to-Service
**Policy:** MUST use `lb://` URLs
- All inter-service communication
- Data aggregation services
- Orchestration services

---

## Monitoring & Alerts

### Metrics to Track
```
gateway_auth_success_total
gateway_auth_failures_total{reason="expired_token"}
gateway_auth_failures_total{reason="invalid_signature"}
gateway_auth_latency_seconds
gateway_token_validation_duration_seconds
service_auth_header_missing_total
service_authorization_failures_total
```

### Alerts to Configure
1. **Auth gateway errors > 5% of requests**
   - Indicates auth service issue
   - Action: Page on-call engineer

2. **X-User-ID header missing in services**
   - Indicates bypass of gateway
   - Action: Audit service-to-service calls

3. **Auth validation latency > 100ms**
   - Indicates slow auth service or gateway issue
   - Action: Check auth service performance

---

## Document History

| Version | Date | Change |
|---------|------|--------|
| 1.0 | Aug 2026 | Initial policy document |
| | | Mandated gateway-level auth |
| | | Removed service-level auth |
| | | Added header-based context |

---

## Questions?

**Contact:** Architecture Team  
**Escalation:** Engineering Manager  
**Policy Review:** Quarterly

