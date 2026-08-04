# Complete Authentication Architecture - Integration Guide

**Document Type:** Technical Integration Reference  
**Audience:** Architecture Team, Engineering Leads  
**Status:** Ready for Implementation

---

## Architecture Overview

### The Complete Picture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EXTERNAL CLIENTS                                   │
│  (Web Browser, Mobile App, Desktop Client, Third-Party API Consumer)        │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
                               │ 1. POST /auth/login
                               │    {username, password}
                               ▼
        ┌──────────────────────────────────────────────────┐
        │            API Gateway (8080)                    │
        │  ┌──────────────────────────────────────────┐   │
        │  │ EndpointA: /api/user/**                  │   │
        │  │ EndpointB: /api/data/**                  │   │
        │  │ EndpointC: /api/report/**                │   │
        │  │ ... (22 total endpoints)                 │   │
        │  └──────────────────────────────────────────┘   │
        │                     ▼                            │
        │  ┌──────────────────────────────────────────┐   │
        │  │ Authentication Filter (for all routes)   │   │
        │  │                                          │   │
        │  │ 1. Check Authorization header            │   │
        │  │ 2. Extract JWT token                     │   │
        │  │ 3. Call auth-service to validate JWT     │   │
        │  │ 4. Extract user claims (userId, roles)   │   │
        │  │ 5. Create X-User-ID header               │   │
        │  │ 6. Create X-Tenant-ID header             │   │
        │  │ 7. Create X-User-Roles header            │   │
        │  │ 8. Route to service via load balancer    │   │
        │  └──────────────────────────────────────────┘   │
        │                     ▼                            │
        │  ┌──────────────────────────────────────────┐   │
        │  │ Service Discovery (Route Mapping)        │   │
        │  │ URI: lb://user-service →                 │   │
        │  │      Find instance in Eureka             │   │
        │  └──────────────────────────────────────────┘   │
        └──────────────────────────────────────────────────┘
                               ▼
         ┌─────────────────────────────────────────┐
         │   Eureka Service Registry (8761)         │
         │                                          │
         │ ✓ auth-service: 10.0.1.1:8001 (UP)     │
         │ ✓ user-service: 10.0.1.2:8001 (UP)     │
         │ ✓ data-service: 10.0.1.3:8001 (UP)     │
         │ ✓ report-service: 10.0.1.4:8001 (UP)   │
         │ ... (24 services total)                  │
         └────────────────────────────────────────┘
                               ▼
     ┌─────────────────────────────────────────────────────┐
     │               Individual Services                    │
     │                                                      │
     │ ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
     │ │ User Service │  │ Data Service │  │ Auth Srv   │ │
     │ │   (8001)     │  │    (8001)    │  │  (8001)    │ │
     │ │              │  │              │  │            │ │
     │ │ Controllers: │  │ Controllers: │  │ Endpoint:  │ │
     │ │ @GetMapping  │  │ @GetMapping  │  │ /validate  │ │
     │ │ Accept:      │  │ Accept:      │  │            │ │
     │ │ X-User-ID    │  │ X-User-ID    │  │ Input:     │ │
     │ │ X-Tenant-ID  │  │ X-Tenant-ID  │  │ JWT token  │ │
     │ │ X-User-Roles │  │ X-User-Roles │  │            │ │
     │ │              │  │              │  │ Output:    │ │
     │ │ Impl:        │  │ Impl:        │  │ {valid: t} │ │
     │ │ NO Security  │  │ NO Security  │  │            │ │
     │ │ NO @Secured  │  │ NO @Secured  │  │ Validate   │ │
     │ │ NO JWT parse │  │ NO JWT parse │  │ signature  │ │
     │ │              │  │              │  │ Check exp  │ │
     │ └──────────────┘  └──────────────┘  └────────────┘ │
     │         ▼                 ▼                 ▼        │
     │     PostgreSQL        PostgreSQL      PostgreSQL    │
     │                                                      │
     └──────────────────────────────────────────────────────┘
```

---

## The Three Layers

### Layer 1: Gateway (Port 8080) - Authentication

**Responsibility:** Validate JWT tokens, extract user context, route requests

**How It Works:**
```java
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<Config> {
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // STEP 1: Extract Authorization header
            String authHeader = exchange.getRequest()
                .getHeaders().getFirst("Authorization");
            
            if (!authHeader.startsWith("Bearer ")) {
                return sendError(exchange, 401, "Missing token");
            }
            
            // STEP 2: Get token
            String token = authHeader.replace("Bearer ", "");
            
            // STEP 3: Validate token (call auth-service)
            TokenValidationResponse validation = 
                authService.validateToken(token);
            
            if (!validation.isValid()) {
                return sendError(exchange, 401, "Invalid token");
            }
            
            // STEP 4: Extract claims
            UUID userId = validation.getUserId();
            UUID tenantId = validation.getTenantId();
            List<String> roles = validation.getRoles();
            
            // STEP 5: Add headers to request
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-ID", userId.toString())
                .header("X-Tenant-ID", tenantId.toString())
                .header("X-User-Roles", String.join(",", roles))
                .build();
            
            // STEP 6: Continue to service
            return chain.filter(
                exchange.mutate().request(request).build()
            );
        };
    }
}
```

**Routes Configured:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        # All routes protected by AuthFilter
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - AuthFilter
            
        - id: data-service
          uri: lb://data-service
          predicates:
            - Path=/api/data/**
          filters:
            - AuthFilter
            
        # ... 20 more routes
```

### Layer 2: Service Discovery (Port 8761) - Routing

**Responsibility:** Maintain registry of all service instances, resolve service names to IPs

**How Services Register:**
```yaml
# In each service's application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
```

**How Gateway Routes:**
```
Gateway wants to reach: lb://user-service
                              ▼
Eureka query: "Give me instances for user-service"
                              ▼
Eureka responds: [
  {host: 10.0.1.2, port: 8001, status: UP},
  {host: 10.0.1.3, port: 8001, status: UP}
]
                              ▼
LoadBalancer picks: Round-robin → 10.0.1.2:8001
                              ▼
Request sent to: http://10.0.1.2:8001/endpoint
```

### Layer 3: Services (8001+) - Business Logic

**Responsibility:** Implement business logic, trust headers from gateway, implement authorization

**How Services Receive Requests:**
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId,      // From gateway
            @RequestHeader("X-Tenant-ID") UUID tenantId,  // From gateway
            @RequestHeader("X-User-Roles") String roles) { // From gateway
        
        // GATEWAY GUARANTEE:
        // ✓ Gateway validated JWT
        // ✓ Gateway extracted these values from token
        // ✓ These headers are authentic
        
        // SERVICE TASK: Implement business logic + authorization
        
        // Authorization check (service level)
        if (!canUserViewOtherUser(userId, id, roles)) {
            return ResponseEntity.status(403).build();
        }
        
        // Fetch data (scoped to tenant)
        return ResponseEntity.ok(
            userService.getUserById(id, tenantId)
        );
    }
    
    private boolean canUserViewOtherUser(
            UUID requestingUser, 
            UUID targetUser, 
            String rolesStr) {
        
        List<String> roles = Arrays.asList(rolesStr.split(","));
        
        // User can view themselves
        if (requestingUser.equals(targetUser)) return true;
        
        // Admins can view anyone
        if (roles.contains("admin")) return true;
        
        // Others cannot view other users
        return false;
    }
}
```

---

## Communication Patterns

### Pattern 1: Client → Gateway → Service

```
CLIENT REQUEST:
GET /api/user/profile
Header: Authorization: Bearer eyJhbGc...

↓

GATEWAY:
1. Extract JWT
2. Validate at auth-service
3. Extract userId, tenantId, roles
4. Add headers:
   X-User-ID: user-123
   X-Tenant-ID: tenant-456
   X-User-Roles: viewer,editor

↓

SERVICE RECEIVES:
GET /profile
Header: X-User-ID: user-123
Header: X-Tenant-ID: tenant-456
Header: X-User-Roles: viewer,editor

✓ No Authorization header (gateway stripped it)
✓ Headers added by gateway (authenticated context)

↓

SERVICE RESPONDS:
200 OK {user data scoped to tenant}
```

### Pattern 2: Service-to-Service Communication

```
SERVICE A NEEDS TO CALL SERVICE B:

In Service A:
@Autowired
@LoadBalanced
private RestTemplate restTemplate;

// Call Service B through gateway via Eureka discovery
OtherServiceDTO result = restTemplate.getForObject(
    "lb://other-service/api/endpoint/{id}",
    OtherServiceDTO.class,
    id
);

↓

THE CALL:
1. LoadBalancer queries Eureka: "Where is other-service?"
2. Eureka responds: [10.0.1.5:8001, 10.0.1.6:8001]
3. LoadBalancer picks one (round-robin): 10.0.1.5:8001
4. Gateway NOT involved (direct Eureka-routed call)
5. Request hits Service B with original headers preserved

↓

SERVICE B RECEIVES:
GET /api/endpoint/id-value
Header: X-User-ID: user-123  (from original client request)
Header: X-Tenant-ID: tenant-456

✓ Service A trusts Service B (no re-auth)
✓ Service B knows original requestor
```

### Pattern 3: Token Refresh

```
CLIENT REFRESH:
POST /api/auth/refresh
Header: Authorization: Bearer {refresh-token}

↓

GATEWAY:
1. Route to auth-service (special route, no auth filter)

↓

AUTH SERVICE:
1. Validate refresh token
2. Generate new access token
3. Return new JWT

↓

CLIENT GETS NEW TOKEN:
200 OK
{
  "access_token": "new_jwt_here",
  "expires_in": 3600
}

✓ Refresh without calling gateway validation
✓ Get new token to use for next requests
```

---

## Trust Chain

### The Complete Trust Chain

```
┌─────────────────────────────────────────────────────────┐
│ TRUST FOUNDATION: Crypto Key                            │
│ (Shared between Auth Service & Gateway)                 │
│ Used to verify JWT signature                            │
└─────────────────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────────────────┐
│ LEVEL 1: Client obtains JWT from Auth Service           │
│ - User provides username/password                       │
│ - Auth Service verifies against database                │
│ - Auth Service creates JWT with signed claims           │
│ - Client receives JWT token                             │
└─────────────────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────────────────┐
│ LEVEL 2: Client sends JWT to Gateway                    │
│ - Client includes Authorization header: Bearer {JWT}    │
│ - Gateway receives request                              │
└─────────────────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────────────────┐
│ LEVEL 3: Gateway validates JWT                          │
│ - Extract token from header                             │
│ - Verify signature using shared key                     │
│ - Check expiration timestamp                            │
│ - If invalid → reject (401)                             │
│ - If valid → continue                                   │
└─────────────────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────────────────┐
│ LEVEL 4: Gateway extracts user context                  │
│ - Parse JWT claims                                      │
│ - Extract: userId, tenantId, roles, email              │
│ - These come from Auth Service (trusted)                │
│ - Add as headers: X-User-ID, X-Tenant-ID, X-User-Roles │
└─────────────────────────────────────────────────────────┘
                    ▼
┌─────────────────────────────────────────────────────────┐
│ LEVEL 5: Service receives authenticated request         │
│ - Request contains X-User-ID header (added by gateway)  │
│ - Service reads header (no re-validation needed)        │
│ - Service trusts gateway (gateway validated it)         │
│ - Service implements authorization (business logic)     │
└─────────────────────────────────────────────────────────┘
```

---

## Data Flow Examples

### Example 1: Login Request

```
┌─────────────────────────────────────────────────────────┐
│ CLIENT SENDS LOGIN REQUEST                              │
└─────────────────────────────────────────────────────────┘
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "john@example.com",
  "password": "secure_password"
}

                    ▼

┌─────────────────────────────────────────────────────────┐
│ GATEWAY ROUTES (No auth filter for /auth/login)         │
│ Request → lb://auth-service/login                       │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ AUTH SERVICE PROCESSES                                  │
│ 1. Hash password                                        │
│ 2. Compare with database                                │
│ 3. Create JWT payload:                                  │
│    {                                                     │
│      "sub": "550e8400-e29b-41d4-a716-446655440000",   │
│      "email": "john@example.com",                       │
│      "tenant": "999e8400-e29b-41d4-a716-446655440999", │
│      "roles": ["viewer", "editor"],                     │
│      "iat": 1722710400,                                 │
│      "exp": 1722796800                                  │
│    }                                                     │
│ 4. Sign with private key                                │
│ 5. Return JWT                                           │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ CLIENT RECEIVES JWT TOKEN                               │
└─────────────────────────────────────────────────────────┘
200 OK

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6ImpvaG5AZXhhbXBsZS5jb20iLCJ0ZW5hbnQiOiI5OTllODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDA5OTkiLCJyb2xlcyI6WyJ2aWV3ZXIiLCJlZGl0b3IiXSwiaWF0IjoxNzIyNzEwNDAwLCJleHAiOjE3MjI3OTY4MDB9.signature_here",
  "expires_in": 86400
}

✓ Token is now stored by client (localStorage, cookies, etc.)
```

### Example 2: User Makes Authenticated Request

```
┌─────────────────────────────────────────────────────────┐
│ CLIENT SENDS REQUEST WITH TOKEN                         │
└─────────────────────────────────────────────────────────┘
GET http://localhost:8080/api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

                    ▼

┌─────────────────────────────────────────────────────────┐
│ GATEWAY AUTH FILTER                                     │
│ 1. Intercept request                                    │
│ 2. Extract Authorization header                         │
│ 3. Parse JWT                                            │
│ 4. Verify signature using private key                   │
│ 5. Check exp: 1722796800 > now? YES ✓                  │
│ 6. Extract claims:                                      │
│    - sub (user ID): 550e8400-e29b-41d4-a716-446655440000
│    - tenant: 999e8400-e29b-41d4-a716-446655440999      │
│    - roles: ["viewer", "editor"]                        │
│ 7. Create new request with headers:                     │
│    X-User-ID: 550e8400-e29b-41d4-a716-446655440000     │
│    X-Tenant-ID: 999e8400-e29b-41d4-a716-446655440999   │
│    X-User-Roles: viewer,editor                          │
│ 8. Route via load balancer                              │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ SERVICE DISCOVERY (Eureka)                              │
│ Query: "Where is user-service?"                         │
│ Response: [10.0.1.2:8001, 10.0.1.3:8001]               │
│ LoadBalancer picks: 10.0.1.2:8001                       │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ USER SERVICE RECEIVES                                   │
│ GET http://10.0.1.2:8001/profile                        │
│ X-User-ID: 550e8400-e29b-41d4-a716-446655440000        │
│ X-Tenant-ID: 999e8400-e29b-41d4-a716-446655440999      │
│ X-User-Roles: viewer,editor                             │
│                                                          │
│ Controller code:                                         │
│ @GetMapping("/profile")                                 │
│ public ResponseEntity<ProfileDTO> getProfile(           │
│     @RequestHeader("X-User-ID") UUID userId) {          │
│     return ResponseEntity.ok(                           │
│         profileService.getProfile(userId)               │
│     );                                                   │
│ }                                                        │
│                                                          │
│ ✓ Trust: userId came from gateway (already validated)   │
│ ✓ No JWT parsing needed                                 │
│ ✓ No token re-validation needed                         │
│ ✓ Continue with business logic                          │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ USER SERVICE QUERIES DATABASE                           │
│ SELECT * FROM users WHERE id = userId AND              │
│                              tenant_id = tenantId       │
│                                                          │
│ ✓ Filter by tenant (data isolation)                     │
│ ✓ Query returns profile data                            │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ RESPONSE SENT BACK                                      │
│ 200 OK                                                  │
│ {                                                       │
│   "id": "550e8400-e29b-41d4-a716-446655440000",        │
│   "email": "john@example.com",                         │
│   "name": "John Doe",                                   │
│   "tenant_id": "999e8400-e29b-41d4-a716-446655440999"  │
│ }                                                       │
└─────────────────────────────────────────────────────────┘

                    ▼

┌─────────────────────────────────────────────────────────┐
│ CLIENT RECEIVES RESPONSE                                │
│ ✓ Request authenticated                                 │
│ ✓ User data returned                                    │
│ ✓ Can now use data in application                       │
└─────────────────────────────────────────────────────────┘
```

---

## Key Components Summary

| Component | Port | Role | Tech Stack |
|-----------|------|------|-----------|
| API Gateway | 8080 | Routes requests, validates JWT, adds headers | Spring Cloud Gateway |
| Service Discovery | 8761 | Maintains service registry, health checks | Eureka |
| Auth Service | 8001 | Issues JWTs, validates tokens | Spring Boot + DB |
| Services | 8001+ | Business logic, authorization | Spring Boot + DB |
| Database | 5432 | Stores users, roles, data | PostgreSQL |
| Message Broker | 9092 | Event streaming | Kafka |

---

## Critical Paths

### Path 1: Authentication Critical Path
```
Client → Gateway [VALIDATE JWT] → Service [USE HEADERS] → Database [QUERY]
Duration: < 50ms (target)
Fail point: Gateway (if JWT invalid)
```

### Path 2: Authorization Critical Path
```
Service [RECEIVES REQUEST] → [READ HEADERS] → [CHECK ROLES] → [ALLOW/DENY]
Duration: < 10ms (target)
Fail point: Service (if insufficient permissions)
```

### Path 3: Service-to-Service Path
```
Service A [USE LOAD-BALANCED URL] → Eureka [RESOLVE] → Service B [RECEIVE]
Duration: < 30ms (target)
Fail point: Eureka (if service down)
```

---

## Failure Scenarios & Recovery

### Scenario 1: Invalid JWT
```
Client sends: Authorization: Bearer invalid_token

Gateway checks: JWT signature invalid ✗

Gateway responds: 401 Unauthorized
                  (Service never invoked)

Client action: Refresh token or re-authenticate
```

### Scenario 2: Service Down
```
Gateway resolves: lb://user-service → Eureka

Eureka responds: All instances DOWN ✗

Gateway responds: 503 Service Unavailable
                  (Circuit breaker triggered)

Recovery: Restart service, Eureka detects UP, traffic resumes
```

### Scenario 3: Expired Token
```
JWT claim: exp = 1722796800
Current time: 1722796801

Gateway validates: Token expired ✗

Gateway responds: 401 Unauthorized

Client action: Use refresh endpoint to get new token
```

---

## Monitoring & Observability

### Metrics to Track

**Gateway Metrics:**
```
gateway_auth_success_total
gateway_auth_failures_total{reason="invalid_signature"}
gateway_auth_failures_total{reason="expired_token"}
gateway_auth_latency_seconds{quantile="0.95"}
```

**Service Metrics:**
```
service_requests_total{service="user-service", status="200"}
service_requests_total{service="data-service", status="403"}
service_latency_seconds{quantile="0.99"}
```

**Discovery Metrics:**
```
eureka_client_request_duration_seconds
eureka_client_registration_count
eureka_instance_status{status="UP", service="auth-service"}
```

### Logs to Monitor

**Gateway Logs:**
```
INFO Gateway: AuthFilter - Token valid for user 550e8400...
WARN Gateway: AuthFilter - Token expired
ERROR Gateway: AuthFilter - Invalid signature for token
```

**Service Logs:**
```
INFO UserService: Processing request for user 550e8400... from tenant 999e8400...
WARN UserService: Authorization failed - insufficient roles
ERROR UserService: Database query failed for user 550e8400...
```

---

## Deployment Checklist

- [ ] Auth Service running (port 8001)
- [ ] Eureka running (port 8761)
- [ ] API Gateway running (port 8080) with AuthFilter
- [ ] All services registered with Eureka (health check: UP)
- [ ] Gateway routes to all 24 services
- [ ] JWT signing key configured (shared between Auth & Gateway)
- [ ] Services have @LoadBalanced RestTemplate bean
- [ ] Services have removed SecurityConfig class
- [ ] Monitoring & logging configured
- [ ] End-to-end test: login → call service → get data

