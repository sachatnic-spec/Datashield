# Service Implementation Guide - Gateway Authentication Model

## Quick Start

**Architecture Principle:** Services receive pre-authenticated requests from the gateway. Services extract user context from request headers and do NOT re-authenticate.

**Before & After Comparison:**

| Aspect | Before (Old Model) | After (Gateway Model) |
|--------|-------------------|----------------------|
| Where auth happens | In each service | At API Gateway only |
| Services implement | SecurityConfig, @PreAuthorize | None (trust gateway) |
| How to get user ID | Parse JWT token | Read X-User-ID header |
| Service dependencies | Spring Security + JWT libs | Just business logic |
| Code per service | ~200 lines auth code | 0 lines auth code |
| Token validation | 24 services × 24 | Gateway × 1 |

## Service Checklist: Full Migration

### Step 1: Remove Authentication Dependencies

**File:** `pom.xml`

```xml
<!-- REMOVE these dependencies -->
<!-- ❌ DELETE: Spring Security -->
<!-- <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency> -->

<!-- ❌ DELETE: JWT libraries -->
<!-- <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
</dependency> -->

<!-- KEEP these dependencies -->
<!-- ✅ KEEP: Load balancer for service discovery -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- ✅ KEEP: RestTemplate bean -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Step 2: Remove Security Configuration

**Files to DELETE:**
- `src/main/java/.../config/SecurityConfig.java` (entire file)
- `src/main/java/.../security/JwtTokenProvider.java` (entire file)
- `src/main/java/.../security/AuthFilter.java` (entire file)
- Any other auth-related classes

**Example SecurityConfig.java to DELETE:**
```java
// ❌ DELETE THIS ENTIRE FILE
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Gateway handles all authentication now
    // Services don't need this
}
```

### Step 3: Update Controllers

**Pattern: Extract user from request header**

#### Before (Old - Remove this)
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")  // ❌ REMOVE
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
        // ❌ REMOVE: Parse JWT
        HttpServletRequest request = /* ... */;
        String token = extractTokenFromHeader(request);
        UUID userId = jwtTokenProvider.extractUserId(token);
        
        // Validate token (redundant - gateway already did this)
        if (!jwtTokenProvider.isValid(token)) {
            return ResponseEntity.status(401).build();
        }
        
        return userService.getUser(id);
    }
    
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader.replace("Bearer ", "");
    }
}
```

#### After (New - Implement this)
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID authenticatedUserId) {
        
        // Trust: Gateway validated this header
        // No need to parse JWT or validate token
        
        // Optional: Log for audit trail
        log.info("User {} requested user {}", authenticatedUserId, id);
        
        // Continue with business logic
        UserDTO user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
}
```

### Step 4: Update Service Layer

**Pattern: Use auth context from headers**

#### Before (Old - Remove this)
```java
@Service
public class DataService {
    
    @Autowired
    private DataRepository dataRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private HttpServletRequest request;
    
    public DataDTO getData(UUID dataId) {
        // ❌ REMOVE: Extract and validate JWT
        String token = extractToken();
        UUID userId = jwtTokenProvider.extractUserId(token);
        
        // ❌ REMOVE: Re-validate token
        if (!jwtTokenProvider.isValid(token)) {
            throw new UnauthorizedException("Token expired");
        }
        
        return dataRepository.findByIdAndUserId(dataId, userId)
            .orElseThrow(() -> new NotFoundException("Data not found"));
    }
}
```

#### After (New - Implement this)
```java
@Service
public class DataService {
    
    @Autowired
    private DataRepository dataRepository;
    
    public DataDTO getData(
            UUID dataId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        // Trust: Gateway already validated user
        // X-User-ID header is authentic
        
        return dataRepository.findByIdAndUserId(dataId, userId)
            .orElseThrow(() -> new NotFoundException("Data not found"));
    }
}
```

**Better: Use AuthContext injectable bean**

```java
@Service
public class DataService {
    
    @Autowired
    private DataRepository dataRepository;
    
    @Autowired
    private AuthContext authContext;
    
    public DataDTO getData(UUID dataId) {
        // Get authenticated user from context
        // Context populated by interceptor from headers
        UUID userId = authContext.getCurrentUserId();
        
        return dataRepository.findByIdAndUserId(dataId, userId)
            .orElseThrow(() -> new NotFoundException("Data not found"));
    }
}
```

### Step 5: Create AuthContext Component (Optional)

**File:** `src/main/java/.../context/AuthContext.java`

```java
package io.datasheild.common.context;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class AuthContext {
    
    private static final ThreadLocal<AuthContextData> context = new ThreadLocal<>();
    
    public void setUserContext(
            UUID userId,
            UUID tenantId,
            String email,
            List<String> roles) {
        context.set(new AuthContextData(userId, tenantId, email, roles));
    }
    
    public UUID getCurrentUserId() {
        AuthContextData data = context.get();
        return data != null ? data.getUserId() : null;
    }
    
    public UUID getCurrentTenantId() {
        AuthContextData data = context.get();
        return data != null ? data.getTenantId() : null;
    }
    
    public String getCurrentEmail() {
        AuthContextData data = context.get();
        return data != null ? data.getEmail() : null;
    }
    
    public List<String> getCurrentRoles() {
        AuthContextData data = context.get();
        return data != null ? data.getRoles() : Collections.emptyList();
    }
    
    public boolean hasRole(String role) {
        List<String> roles = getCurrentRoles();
        return roles != null && roles.contains(role);
    }
    
    public void clear() {
        context.remove();
    }
    
    @lombok.Data
    public static class AuthContextData {
        private final UUID userId;
        private final UUID tenantId;
        private final String email;
        private final List<String> roles;
        
        public AuthContextData(UUID userId, UUID tenantId, String email, List<String> roles) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.email = email;
            this.roles = roles;
        }
    }
}
```

### Step 6: Create Interceptor to Populate AuthContext

**File:** `src/main/java/.../interceptor/AuthContextInterceptor.java`

```java
package io.datasheild.common.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.datasheild.common.context.AuthContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class AuthContextInterceptor implements HandlerInterceptor {
    
    @Autowired
    private AuthContext authContext;
    
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        
        try {
            // Extract headers added by gateway
            String userIdHeader = request.getHeader("X-User-ID");
            String tenantIdHeader = request.getHeader("X-Tenant-ID");
            String emailHeader = request.getHeader("X-User-Email");
            String rolesHeader = request.getHeader("X-User-Roles");
            
            // If headers exist, populate context
            if (userIdHeader != null && tenantIdHeader != null) {
                UUID userId = UUID.fromString(userIdHeader);
                UUID tenantId = UUID.fromString(tenantIdHeader);
                String email = emailHeader != null ? emailHeader : "unknown";
                List<String> roles = rolesHeader != null
                    ? Arrays.asList(rolesHeader.split(","))
                    : Collections.emptyList();
                
                authContext.setUserContext(userId, tenantId, email, roles);
            }
        } catch (Exception e) {
            // Log but don't fail - request will proceed without context
            // (Gateway validation already happened)
            log.warn("Failed to populate auth context", e);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) throws Exception {
        // Clean up ThreadLocal to prevent memory leaks
        authContext.clear();
    }
}
```

**Register in WebConfig:**
```java
package io.datasheild.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.datasheild.common.interceptor.AuthContextInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthContextInterceptor authContextInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authContextInterceptor);
    }
}
```

### Step 7: Update Service-to-Service Calls

**Pattern: Use LoadBalanced RestTemplate**

#### Before (Old - Direct URLs)
```java
@Service
public class UserService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public UserDTO getDetailedUser(UUID userId) {
        // ❌ WRONG: Direct service URL bypasses gateway
        String url = "http://profile-service:8085/profiles/" + userId;
        ProfileDTO profile = restTemplate.getForObject(url, ProfileDTO.class);
        
        return new UserDTO(userId, profile);
    }
}
```

#### After (New - Gateway routed)
```java
@Service
public class UserService {
    
    @Autowired
    @LoadBalanced  // ✅ REQUIRED: Load-balanced bean
    private RestTemplate restTemplate;
    
    public UserDTO getDetailedUser(UUID userId) {
        // ✅ CORRECT: Routes through gateway via Eureka discovery
        ProfileDTO profile = restTemplate.getForObject(
            "lb://profile-service/profiles/" + userId,
            ProfileDTO.class
        );
        
        return new UserDTO(userId, profile);
    }
}
```

### Step 8: Update Service-to-Service Authorization

**Pattern: Trust X-User-ID header for cross-service calls**

```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Autowired
    private ProfileService profileService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(
            @PathVariable UUID userId,
            @RequestHeader("X-User-ID") UUID requestingUserId) {
        
        // Authorization: Check if requestingUser can view this profile
        // (Not authentication - gateway did that)
        ProfileDTO profile = profileService.getProfile(userId);
        
        if (profile.isPrivate() && !profile.getOwnerId().equals(requestingUserId)) {
            // Service-level authorization check
            throw new AccessDeniedException("Cannot access private profile");
        }
        
        return ResponseEntity.ok(profile);
    }
}
```

## Example Service Transformation

### Service: Data Service (Before → After)

**Before: Full auth code**
```java
// ❌ BEFORE: With authentication logic
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private HttpServletRequest request;
    
    @PostMapping
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<DataDTO> createData(@RequestBody CreateDataRequest req) {
        // 1. Extract JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        
        // 2. Validate token
        if (!jwtTokenProvider.isValid(token)) {
            return ResponseEntity.status(401).build();
        }
        
        // 3. Extract user ID
        UUID userId = jwtTokenProvider.extractUserId(token);
        
        // 4. Create data
        DataDTO data = dataService.create(req, userId);
        return ResponseEntity.status(201).body(data);
    }
}
```

**After: Gateway-authenticated**
```java
// ✅ AFTER: Trust gateway
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    @Autowired
    private DataService dataService;
    
    @PostMapping
    public ResponseEntity<DataDTO> createData(
            @RequestBody CreateDataRequest req,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-User-Roles") String rolesString) {
        
        // Authorization: Check if user can create data
        List<String> roles = Arrays.asList(rolesString.split(","));
        if (!roles.contains("editor")) {
            throw new AccessDeniedException("Cannot create data");
        }
        
        // Trust: userId is already validated by gateway
        DataDTO data = dataService.create(req, userId);
        return ResponseEntity.status(201).body(data);
    }
}
```

**Lines of code removed:** ~20 lines (30% reduction)
**Security guarantee:** Improved (gateway validates centrally)

## File Structure After Migration

```
service-directory/
├── pom.xml (authentication deps REMOVED)
├── src/main/java/io/datasheild/{service}/
│   ├── {Service}Application.java
│   │   └── @EnableDiscoveryClient ✅
│   ├── config/
│   │   └── RestClientConfig.java (ONLY config needed)
│   ├── controller/
│   │   ├── SomeController.java
│   │   │   └── Uses @RequestHeader("X-User-ID") ✅
│   │   └── (No SecurityConfig.java)
│   ├── service/
│   │   ├── SomeService.java
│   │   │   └── Reads AuthContext ✅
│   │   └── (No JwtTokenProvider.java)
│   └── (No security/ folder)
├── src/main/resources/
│   └── application.yml
└── src/test/
    └── (No SecurityFilter tests)
```

## Common Patterns

### Pattern 1: Get Authenticated User ID
```java
@GetMapping("/profile")
public ResponseEntity<ProfileDTO> getProfile(
        @RequestHeader("X-User-ID") UUID userId) {
    return ResponseEntity.ok(profileService.getProfile(userId));
}
```

### Pattern 2: Get Tenant Scope
```java
@GetMapping("/reports")
public ResponseEntity<List<ReportDTO>> getReports(
        @RequestHeader("X-Tenant-ID") UUID tenantId) {
    return ResponseEntity.ok(
        reportService.getReportsForTenant(tenantId)
    );
}
```

### Pattern 3: Check Role (Service-Level Authorization)
```java
@PostMapping("/delete/{id}")
public ResponseEntity<?> deleteReport(
        @PathVariable UUID id,
        @RequestHeader("X-User-Roles") String rolesString) {
    
    List<String> roles = Arrays.asList(rolesString.split(","));
    if (!roles.contains("admin") && !roles.contains("editor")) {
        return ResponseEntity.status(403)
            .body("Only admins can delete reports");
    }
    
    reportService.delete(id);
    return ResponseEntity.ok().build();
}
```

### Pattern 4: Use AuthContext (Injectable)
```java
@Service
public class SomeService {
    
    @Autowired
    private AuthContext authContext;
    
    @Autowired
    private SomeRepository repository;
    
    public SomeDTO getSomething(UUID id) {
        UUID userId = authContext.getCurrentUserId();
        UUID tenantId = authContext.getCurrentTenantId();
        
        // Get data for user's tenant
        return repository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Not found"));
    }
}
```

### Pattern 5: Service-to-Service Call with Gateway
```java
@Service
public class OrchestratorService {
    
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;
    
    public CombinedDTO getCombinedData(UUID id) {
        // Call user-service via gateway + discovery
        UserDTO user = restTemplate.getForObject(
            "lb://user-service/users/{id}",
            UserDTO.class,
            id
        );
        
        // Call profile-service via gateway + discovery
        ProfileDTO profile = restTemplate.getForObject(
            "lb://profile-service/profiles/{id}",
            ProfileDTO.class,
            id
        );
        
        return new CombinedDTO(user, profile);
    }
}
```

## Testing Your Service

### Unit Test (Mock AuthContext)
```java
@SpringBootTest
class DataServiceTest {
    
    @MockBean
    private DataRepository dataRepository;
    
    @MockBean
    private AuthContext authContext;
    
    @InjectMocks
    private DataService dataService;
    
    @Test
    void testGetData_WithValidUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authContext.getCurrentUserId()).thenReturn(userId);
        
        DataEntity entity = new DataEntity();
        entity.setId(UUID.randomUUID());
        when(dataRepository.findByIdAndUserId(entity.getId(), userId))
            .thenReturn(Optional.of(entity));
        
        // Act
        DataDTO result = dataService.getData(entity.getId());
        
        // Assert
        assertThat(result.getId()).isEqualTo(entity.getId());
    }
}
```

### Integration Test (Through Gateway)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataServiceIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    void testGetData_WithValidToken() {
        // Get token from gateway
        String token = getToken();
        
        // Call service through gateway
        ResponseEntity<DataDTO> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/data/123",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders() {{
                setBearerAuth(token);
            }}),
            DataDTO.class
        );
        
        // Assert: Gateway adds X-User-ID header before routing
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## Rollback Plan

**If something breaks after removing auth:**

1. Keep the old SecurityConfig.java in git history (revert if needed)
2. Add back Spring Security dependency to pom.xml
3. Restore JwtTokenProvider class
4. Test locally before re-deploying

**But:** The gateway authentication model is simpler and more maintainable long-term.

