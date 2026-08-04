# Gateway-Level Authentication Model

## Overview

DataShield implements a **centralized authentication model** where ALL authentication validation occurs at the API Gateway level. Individual services do NOT implement authentication — they trust pre-authenticated requests from the gateway and extract user context from HTTP headers.

This architecture provides:
- **Single authentication policy** across entire platform
- **Simplified service code** — no security filters or auth decorators
- **Easier security updates** — change rules at gateway, not in 24+ services
- **Better observability** — centralized auth logs and metrics
- **Improved performance** — no redundant JWT validation in services

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          External Client                         │
│  (Browser / Mobile App / API Consumer)                           │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                    POST /login + credentials
                           │
                           ▼
          ┌────────────────────────────────────────┐
          │        API Gateway (Port 8080)         │
          │  ┌─────────────────────────────────┐  │
          │  │ 1. Parse JWT Token              │  │
          │  │ 2. Validate Signature           │  │
          │  │ 3. Check Expiration             │  │
          │  │ 4. Extract User Claims          │  │
          │  │ 5. Add Auth Headers             │  │
          │  └─────────────────────────────────┘  │
          │  Headers Added:                       │
          │  - X-User-ID: {userId}                │
          │  - X-Tenant-ID: {tenantId}           │
          │  - X-User-Email: {email}              │
          │  - X-User-Roles: {roles}              │
          └────────────┬────────────────────────┘
                       │
         Authenticated request + Headers
                       │
         ┌─────────────┼──────────────────────┐
         │             │                      │
         ▼             ▼                      ▼
    ┌─────────────────────────────────────────────────┐
    │  Service Discovery (Eureka)                     │
    │  Routes: auth → lb://auth-service              │
    │          user → lb://user-service              │
    │          data → lb://data-service              │
    └─────────────┬─────────────────────────────────┘
                  │
    ┌─────────────┼─────────────────────────────────────┐
    │             │                                     │
    ▼             ▼                                     ▼
┌──────────┐  ┌──────────┐  ┌──────────┐   ...    ┌──────────┐
│ Auth     │  │ User     │  │ Data     │          │ Report   │
│ Service  │  │ Service  │  │ Service  │          │ Service  │
│          │  │          │  │          │          │          │
│ No Auth  │  │ No Auth  │  │ No Auth  │          │ No Auth  │
│ Filters  │  │ Filters  │  │ Filters  │          │ Filters  │
│          │  │          │  │          │          │          │
│ Extract: │  │ Extract: │  │ Extract: │          │ Extract: │
│ Headers  │  │ Headers  │  │ Headers  │          │ Headers  │
│ → UserId │  │ → UserId │  │ → UserId │          │ → UserId │
└──────────┘  └──────────┘  └──────────┘          └──────────┘
```

## Authentication Flow

### Step 1: Client Authentication (First Time)
```
Client                          Gateway                          Auth Service
  │                               │                                  │
  ├──POST /api/auth/login────────►│                                  │
  │ {username, password}          │                                  │
  │                               ├──────────GET /login────────────►│
  │                               │ {username, password}              │
  │                               │◄─────────200 + JWT Token─────────┤
  │◄──────────200 + JWT Token─────┤                                  │
  │                               │                                  │
```

**JWT Format:**
```json
{
  "sub": "user123",
  "email": "user@example.com",
  "tenant": "tenant456",
  "roles": ["admin", "viewer"],
  "exp": 1719417600,
  "iat": 1719331200
}
```

### Step 2: Subsequent Requests (With Authentication)
```
Client                          Gateway                          Service
  │                               │                                  │
  ├──GET /api/service/resource───►│                                  │
  │ Header: Authorization: Bearer {JWT}                              │
  │                               │                                  │
  │                     ┌─────────────────────────────┐              │
  │                     │ Gateway Auth Filter:        │              │
  │                     │ 1. Extract JWT from header  │              │
  │                     │ 2. Validate signature       │              │
  │                     │ 3. Check expiration         │              │
  │                     │ 4. Extract claims           │              │
  │                     │ 5. Add X-User-ID header     │              │
  │                     └─────────────────────────────┘              │
  │                               │                                  │
  │                               ├─GET /resource────────────────────►
  │                               │ Header: X-User-ID: user123       │
  │                               │ Header: X-Tenant-ID: tenant456   │
  │                               │ Header: X-User-Roles: admin      │
  │                               │                                  │
  │                               │        ┌──────────────────┐      │
  │                               │        │ Service Auth:    │      │
  │                               │        │ 1. Trust gateway │      │
  │                               │        │ 2. Extract header│      │
  │                               │        │ 3. Continue      │      │
  │                               │        └──────────────────┘      │
  │                               │◄─────────200 + Data──────────────┤
  │◄──────────────200 + Data──────┤                                  │
  │                               │                                  │
```

## Gateway Authentication Implementation

### Required Configuration (api-gateway/application.yml)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - name: AuthFilter
            - name: StripPrefix
              args:
                parts: 2
            - name: CircuitBreaker
              args:
                name: userServiceCB
```

### Gateway Auth Filter (AuthFilter.java)

```java
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<Config> {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                // Step 1: Extract JWT from Authorization header
                String authHeader = exchange.getRequest()
                    .getHeaders().getFirst("Authorization");
                
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Missing or invalid Authorization header");
                }
                
                String token = authHeader.replace("Bearer ", "");
                
                // Step 2: Validate token
                if (!jwtTokenProvider.validateToken(token)) {
                    return onError(exchange, "Invalid or expired token");
                }
                
                // Step 3: Extract user claims
                UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                UUID tenantId = jwtTokenProvider.getTenantIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                
                // Step 4: Create new request with auth headers
                ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-ID", userId.toString())
                    .header("X-Tenant-ID", tenantId.toString())
                    .header("X-User-Email", email)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();
                
                // Step 5: Continue to next filter with authenticated request
                return chain.filter(exchange.mutate().request(request).build());
                
            } catch (Exception e) {
                return onError(exchange, "Authentication failed: " + e.getMessage());
            }
        };
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().writeWith(
            Mono.fromCallable(() -> {
                DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(errorMsg.getBytes());
                return buffer;
            })
        );
    }

    public static class Config {}
}
```

## Service-Level Implementation

### Pattern 1: Extract User ID from Header

**INSTEAD OF:** Parsing JWT in service
```java
// ❌ DON'T DO THIS ANYMORE
String token = request.getHeader("Authorization").replace("Bearer ", "");
UUID userId = jwtTokenProvider.extractUserId(token);
```

**DO THIS:** Extract from header added by gateway
```java
// ✅ DO THIS
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID authenticatedUserId) {
        
        // Gateway already validated this header
        // Safely assume request is authenticated
        return userService.getUser(id);
    }
}
```

### Pattern 2: Extract Tenant Information

```java
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    @Autowired
    private DataService dataService;
    
    @GetMapping
    public ResponseEntity<List<DataDTO>> getData(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-Roles") String rolesString) {
        
        // Parse roles from comma-separated header
        List<String> roles = Arrays.asList(rolesString.split(","));
        
        // Use context for authorization (service-level)
        return dataService.getDataForTenant(tenantId);
    }
}
```

### Pattern 3: Create Auth Context Bean

```java
@Component
public class AuthContext {
    
    private static final ThreadLocal<AuthContextData> context = new ThreadLocal<>();
    
    public void setContext(UUID userId, UUID tenantId, List<String> roles) {
        context.set(new AuthContextData(userId, tenantId, roles));
    }
    
    public UUID getCurrentUserId() {
        return context.get().getUserId();
    }
    
    public UUID getCurrentTenantId() {
        return context.get().getTenantId();
    }
    
    public List<String> getCurrentRoles() {
        return context.get().getRoles();
    }
    
    public void clear() {
        context.remove();
    }
    
    // Data class
    @Data
    public static class AuthContextData {
        private UUID userId;
        private UUID tenantId;
        private List<String> roles;
        
        public AuthContextData(UUID userId, UUID tenantId, List<String> roles) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.roles = roles;
        }
    }
}
```

### Pattern 4: Interceptor to Populate Auth Context

```java
@Component
public class AuthContextInterceptor implements HandlerInterceptor {
    
    @Autowired
    private AuthContext authContext;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        
        String userIdHeader = request.getHeader("X-User-ID");
        String tenantIdHeader = request.getHeader("X-Tenant-ID");
        String rolesHeader = request.getHeader("X-User-Roles");
        
        if (userIdHeader != null) {
            UUID userId = UUID.fromString(userIdHeader);
            UUID tenantId = UUID.fromString(tenantIdHeader);
            List<String> roles = Arrays.asList(rolesHeader.split(","));
            
            authContext.setContext(userId, tenantId, roles);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        authContext.clear();
    }
}
```

### Pattern 5: Service-Level Authorization (NOT Authentication)

```java
@Service
public class DataService {
    
    @Autowired
    private AuthContext authContext;
    
    public DataDTO getData(UUID dataId) {
        UUID tenantId = authContext.getCurrentTenantId();
        UUID userId = authContext.getCurrentUserId();
        List<String> roles = authContext.getCurrentRoles();
        
        // Authorization: Check if user can access this data
        // (NOT authentication - that's done at gateway)
        if (!roles.contains("viewer") && !roles.contains("admin")) {
            throw new AccessDeniedException("Insufficient permissions");
        }
        
        // Get data for tenant
        return dataRepository.findByIdAndTenantId(dataId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Data not found"));
    }
}
```

## What Services Should NOT Do

### ❌ DO NOT: Parse JWT tokens
```java
// WRONG - gateway already validated it
String token = request.getHeader("Authorization").replace("Bearer ", "");
Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
```

### ❌ DO NOT: Use @EnableWebSecurity
```java
// WRONG - authentication happens at gateway
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Remove this entire class
}
```

### ❌ DO NOT: Use @PreAuthorize or @Secured
```java
// WRONG - authorization should happen in service logic
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/{id}")
public DataDTO getData(@PathVariable UUID id) { }
```

### ❌ DO NOT: Make direct service-to-service calls
```java
// WRONG - bypasses gateway
String url = "http://user-service:8081/users/" + userId;
RestTemplate template = new RestTemplate();
User user = template.getForObject(url, User.class);
```

## What Services SHOULD Do

### ✅ DO: Use Load-Balanced RestTemplate
```java
@Autowired
@LoadBalanced
private RestTemplate restTemplate;

// Correct: gateway routes via discovery
User user = restTemplate.getForObject(
    "lb://user-service/users/" + userId, 
    User.class
);
```

### ✅ DO: Extract user context from headers
```java
@GetMapping("/{id}")
public DataDTO getData(
        @PathVariable UUID id,
        @RequestHeader("X-User-ID") UUID userId) {
    // Gateway validated userId is legitimate
    // Proceed with business logic
}
```

### ✅ DO: Implement service-level authorization
```java
// Get user roles from header
List<String> roles = Arrays.asList(
    request.getHeader("X-User-Roles").split(",")
);

// Check authorization (not authentication)
if (!roles.contains("editor")) {
    throw new AccessDeniedException("Cannot edit data");
}
```

### ✅ DO: Log authentication events at gateway
```java
// Gateway filter logs all auth attempts
log.info("Auth attempt: user={}, tenant={}, success={}", 
         userId, tenantId, isValid);
```

## Security Guarantees

### What the Gateway Guarantees
- ✅ JWT signature is valid (cryptographically verified)
- ✅ Token has not expired
- ✅ Token was issued by our auth system
- ✅ User identity is authentic (X-User-ID header is trusted)

### What Services Assume
- ✅ Request came through gateway (headers are authentic)
- ✅ X-User-ID header contains a real, validated user ID
- ✅ X-Tenant-ID header contains user's actual tenant
- ✅ X-User-Roles header contains accurate role list

### Attack Prevention
| Attack Type | Prevention |
|-------------|-----------|
| Expired token accepted | Gateway validates expiration |
| Forged token accepted | JWT signature verified at gateway |
| User ID spoofing | X-User-ID header added after validation |
| Invalid token bypass | Gateway rejects before routing |
| Token replay | Short TTL + refresh token rotation |
| Service bypass | All routes go through gateway |

## Configuration Checklist

- [ ] **api-gateway/pom.xml**
  - [ ] `spring-cloud-starter-gateway` dependency present
  - [ ] `spring-cloud-starter-netflix-eureka-client` present
  - [ ] Auth filter implementation included

- [ ] **api-gateway/application.yml**
  - [ ] AuthFilter registered in gateway configuration
  - [ ] All service routes defined with load-balanced URIs
  - [ ] Circuit breaker configured for resilience

- [ ] **All services' pom.xml**
  - [ ] ❌ NO Spring Security dependency
  - [ ] ❌ NO JWT parser dependency
  - [ ] ✅ RestTemplate `@LoadBalanced` bean present

- [ ] **All services' SecurityConfig.java**
  - [ ] ❌ DELETE this class (no longer needed)

- [ ] **All services' Controllers**
  - [ ] ✅ Accept `@RequestHeader("X-User-ID")` parameter
  - [ ] ✅ Read tenant from `X-Tenant-ID` header
  - [ ] ❌ DO NOT implement @PreAuthorize
  - [ ] ❌ DO NOT parse JWT tokens

## Testing Gateway Authentication

### Test 1: Valid JWT Token
```bash
# Get token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass123"}' | jq -r '.token')

# Use token to call service
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with user data
# Gateway adds X-User-ID header before routing to service
```

### Test 2: Invalid Token
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer invalid_token_here"

# Expected: 401 Unauthorized at gateway level
# Service never receives the request
```

### Test 3: Missing Token
```bash
curl -X GET http://localhost:8080/api/user/profile

# Expected: 401 Unauthorized
# Gateway rejects immediately
```

### Test 4: Expired Token
```bash
# Use a token with exp claim in the past
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer expired_token"

# Expected: 401 Unauthorized
# Gateway validates expiration before routing
```

## Migration Guide

### For Each Service:
1. **Remove authentication code**
   ```bash
   rm src/main/java/.../SecurityConfig.java
   rm src/main/java/.../JwtTokenProvider.java
   ```

2. **Update dependencies in pom.xml**
   ```xml
   <!-- REMOVE: spring-boot-starter-security -->
   <!-- REMOVE: jjwt (JWT library) -->
   <!-- KEEP: @LoadBalanced RestTemplate for service-to-service calls -->
   ```

3. **Update controllers**
   ```java
   // CHANGE: Add X-User-ID parameter
   @GetMapping("/{id}")
   public DTO get(
       @PathVariable UUID id,
       @RequestHeader("X-User-ID") UUID userId) {  // Add this
       // Use userId from header (already validated by gateway)
   }
   ```

4. **Remove @PreAuthorize decorators**
   ```java
   // CHANGE: Move authorization logic into service
   public DataDTO getData(UUID id) {
       if (!canAccess(id)) {
           throw new AccessDeniedException("Forbidden");
       }
       return repository.findById(id).orElseThrow();
   }
   ```

5. **Test**
   ```bash
   # Requests still go through gateway
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/service/endpoint
   
   # Gateway validates token, adds headers, routes to service
   # Service receives request with X-User-ID header
   ```

## Monitoring & Logging

### Gateway Auth Metrics
```
gateway_auth_attempts_total{service="user-service", status="success"}
gateway_auth_failures_total{reason="expired_token"}
gateway_auth_latency_seconds
gateway_token_validation_failures_total{service="auth-service"}
```

### Service Logging
```java
log.info("Request from user={}, tenant={}", 
         request.getHeader("X-User-ID"),
         request.getHeader("X-Tenant-ID"));
```

### Alerts to Configure
- High rate of 401 responses at gateway
- Gateway auth latency > 100ms
- Service-level authorization failures increasing
- Token validation errors from auth-service

## FAQ

**Q: What if a service needs to call another service?**
A: Use @LoadBalanced RestTemplate to call via gateway:
```java
@Autowired
@LoadBalanced
private RestTemplate restTemplate;

// Routes through gateway with service discovery
User user = restTemplate.getForObject("lb://user-service/users/{id}", User.class, userId);
```

**Q: How do services know which user made the request?**
A: Gateway adds X-User-ID header after validation:
```java
@RequestHeader("X-User-ID") UUID userId
```

**Q: What about service-to-service authentication?**
A: Service-to-service calls also go through gateway, which adds X-User-ID header based on original request context.

**Q: Can services implement their own auth for special cases?**
A: No. All auth is at gateway level. Services assume all requests are pre-authenticated. Use service-level authorization for business logic (e.g., "only document owner can edit").

**Q: What if gateway is down?**
A: Services are unreachable. This is by design — force client to know about gateway outage. Alternative: Implement service resilience with fallback auth, but not recommended.

**Q: How to handle token refresh?**
A: All token refresh happens at gateway's auth endpoint (/api/auth/refresh). Services don't need to know about token lifecycle.

