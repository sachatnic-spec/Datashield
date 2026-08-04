# DataShield Spring Cloud Infrastructure Setup

## Overview

This document describes the complete Spring Cloud infrastructure setup for the DataShield microservices platform. The infrastructure includes service discovery (Eureka), API Gateway, Config Server, and resilience patterns.

## Architecture Components

### 1. Service Registry (Eureka Server)
**Location:** `services/service-registry`
**Port:** 8761
**Purpose:** Central service discovery and registration for all microservices

**Key Configuration:**
- `eureka.client.registerWithEureka: false` - Server doesn't register itself
- `eureka.client.fetchRegistry: false` - Server doesn't need to fetch registry
- `eureka.server.enable-self-preservation: true` - Prevents false evictions
- Dashboard available at: `http://localhost:8761/`

**Running:**
```bash
cd services/service-registry
mvn clean install
mvn spring-boot:run
```

---

### 2. API Gateway
**Location:** `services/api-gateway`
**Port:** 8080
**Purpose:** Single entry point for all client requests with intelligent routing

**Key Features:**
- **Service Discovery Integration:** Auto-discovers services registered with Eureka
- **Dynamic Routing:** Routes requests to services based on path patterns
- **Circuit Breaker:** Resilience4J circuit breaker for fault tolerance
- **Load Balancing:** Spring Cloud LoadBalancer for distributed requests

**Route Configuration:**
```
/api/auth/**       → auth-service
/api/grievance/**  → grievance-service
/api/consent/**    → consent-service
/api/breach/**     → breach-service
/api/audit/**      → audit-service
/api/policy/**     → policy-service
/api/rights/**     → rights-service
/api/tenant/**     → tenant-service
/api/notification/** → notification-service
```

**Circuit Breaker Settings:**
- Sliding Window Size: 10
- Minimum Calls: 5
- Failure Rate Threshold: 50%
- Half-Open State Calls: 3
- Wait Duration in Open State: 30s

**Running:**
```bash
cd services/api-gateway
mvn clean install
mvn spring-boot:run
```

**Testing:**
```bash
# Example request through gateway
curl http://localhost:8080/api/auth/login

# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

---

### 3. Config Server
**Location:** `services/config-service`
**Port:** 8012
**Purpose:** Centralized configuration management for all microservices

**Key Features:**
- **Spring Cloud Config:** Centralized property configuration
- **Git-based Storage:** Configuration stored in Git repository
- **Eureka Integration:** Registered with Eureka for discovery
- **Configuration Profiles:** Support for different environments

**Configuration Repository Structure:**
```
config-repo/
├── auth-service/
│   └── application.yml
├── tenant-service/
│   └── application.yml
├── api-gateway/
│   └── application.yml
└── common/
    └── application.yml
```

**Git Configuration (in application.yml):**
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: file://./config-repo
          search-paths: '{application}'
```

**Retrieving Configuration:**
```bash
# Get config for auth-service
curl http://localhost:8012/auth-service/default

# Get specific profile
curl http://localhost:8012/auth-service/production
```

**Running:**
```bash
cd services/config-service
mvn clean install
mvn spring-boot:run
```

---

### 4. Resilience4J Circuit Breaker
**Location:** `libs/common-lib`
**Purpose:** Fault tolerance and resilience patterns

**Included Dependencies:**
- `resilience4j-spring-boot3` - Spring Boot integration
- `resilience4j-circuitbreaker` - Circuit breaker pattern
- `resilience4j-retry` - Automatic retry logic

**Circuit Breaker States:**

1. **CLOSED** - Normal operation, requests pass through
2. **OPEN** - Circuit breaker open, fast failure, no requests passed
3. **HALF_OPEN** - Testing if service recovered, limited requests

**Configuration Parameters:**
- **slidingWindowSize:** Number of calls recorded (default: 10)
- **minimumNumberOfCalls:** Min calls before circuit breaker evaluates state (default: 5)
- **failureRateThreshold:** Threshold to open circuit (default: 50%)
- **slowCallRateThreshold:** Threshold for slow calls (default: 50%)
- **slowCallDurationThreshold:** Duration threshold for slow calls (default: 5s)
- **waitDurationInOpenState:** Time to wait before attempting half-open (default: 30s)

**Usage Example:**
```java
@CircuitBreaker(name = "authServiceCB")
public ResponseEntity<?> callAuthService() {
    // Service call implementation
}
```

---

## Service Registration Flow

### 1. Service Startup
Each microservice registers itself with Eureka automatically:

**Required Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter</artifactId>
</dependency>
```

**Configuration (application.yml):**
```yaml
spring:
  application:
    name: <service-name>
  cloud:
    discovery:
      enabled: true

eureka:
  instance:
    hostname: localhost
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### 2. Heartbeat & Health Check
- Services send heartbeat every 30 seconds
- Eureka confirms registration and updates lease
- If heartbeat stops, Eureka marks instance as DOWN
- After timeout period, removes instance from registry

### 3. Service Discovery
- Clients query Eureka for available service instances
- Load balancer selects instance for request
- If instance fails, load balancer tries next instance

---

## Starting the Infrastructure

### Step 1: Start Eureka Server
```bash
cd services/service-registry
mvn spring-boot:run
# Wait for startup message: "Started ServiceRegistryApplication"
```

### Step 2: Start Config Server
```bash
cd services/config-service
mvn spring-boot:run
# Monitor: http://localhost:8012/actuator/health
```

### Step 3: Start Microservices
Start services in any order - they'll auto-register with Eureka:
```bash
cd services/auth-service
mvn spring-boot:run

# In other terminals:
cd services/tenant-service && mvn spring-boot:run
cd services/grievance-service && mvn spring-boot:run
# ... continue with other services
```

### Step 4: Start API Gateway (last)
```bash
cd services/api-gateway
mvn spring-boot:run
```

---

## Monitoring & Debugging

### Eureka Dashboard
- **URL:** http://localhost:8761/
- **Shows:**
  - Registered services and instances
  - Instance status (UP/DOWN)
  - Instance metadata
  - Application status

### Health Checks
```bash
# Service Registry health
curl http://localhost:8761/actuator/health

# Config Server health
curl http://localhost:8012/actuator/health

# API Gateway health
curl http://localhost:8080/actuator/health

# Any service health
curl http://localhost:<SERVICE_PORT>/actuator/health
```

### Metrics
```bash
# Prometheus metrics endpoint
curl http://localhost:8080/actuator/prometheus

# Gateway metrics
curl http://localhost:8080/actuator/metrics

# Circuit breaker status
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

### Service Discovery Debug
```bash
# List all registered services in Eureka
curl http://localhost:8761/eureka/apps

# Get specific service details
curl http://localhost:8761/eureka/apps/AUTH-SERVICE

# Check service instance
curl http://localhost:8761/eureka/apps/AUTH-SERVICE/DESKTOP-XXXX
```

---

## Configuration Management

### Adding New Service Configuration
1. Create directory: `config-repo/<service-name>/`
2. Add `application.yml` with service configuration
3. Commit to Git repository
4. Service retrieves configuration on startup via Config Server

### Configuration Priority (Highest to Lowest)
1. Environment variables
2. Spring Boot property files
3. Config Server Git repository
4. Application default properties

### Updating Configuration at Runtime
```bash
# Trigger config refresh (if Spring Cloud Bus configured)
curl -X POST http://localhost:<SERVICE_PORT>/actuator/refresh

# For all services (requires messaging)
curl -X POST http://localhost:8080/actuator/busrefresh
```

---

## Resilience & Fault Tolerance

### Circuit Breaker Patterns

**Pattern 1: Fast Failure**
```
Request → Circuit Breaker (CLOSED) → Service
                ↓
           Service Fails → OPEN
                ↓
Request → Circuit Breaker (OPEN) → Fast Failure (no service call)
```

**Pattern 2: Self-Healing**
```
OPEN state → Wait 30s → HALF_OPEN
                          ↓
                    Test Request → Service
                          ↓
                    Success → CLOSED
                    Failure → OPEN (wait again)
```

### Fallback Mechanisms
API Gateway provides fallback endpoint: `/fallback/*`
```json
{
  "status": "SERVICE_UNAVAILABLE",
  "message": "Service is temporarily unavailable. Please try again later.",
  "timestamp": 1234567890
}
```

---

## Production Deployment

### Environment Variables
```bash
# Eureka Configuration
EUREKA_INSTANCE_HOSTNAME=eureka.example.com
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka.example.com:8761/eureka/

# Config Server
CONFIG_GIT_URI=https://github.com/your-org/config-repo
CONFIG_GIT_USERNAME=<github-user>
CONFIG_GIT_PASSWORD=<github-token>

# Individual Services
SPRING_APPLICATION_NAME=<service-name>
SERVER_PORT=<port>

# Database
DB_URL=jdbc:postgresql://db.example.com:5432/datasheild
DB_USERNAME=<username>
DB_PASSWORD=<password>

# Redis
REDIS_HOST=redis.example.com
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092
```

### Docker Deployment
Each service has a `Dockerfile` for containerization:
```bash
docker build -t datasheild/service-registry:1.0.0 ./services/service-registry
docker run -p 8761:8761 datasheild/service-registry:1.0.0
```

### Kubernetes Deployment
Services can be deployed to Kubernetes with proper service discovery configuration:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
spec:
  ports:
    - port: 8761
      targetPort: 8761
  selector:
    app: eureka-server
```

---

## Troubleshooting

### Service Not Registered
**Symptoms:** Service doesn't appear in Eureka dashboard
**Solutions:**
1. Check network connectivity to Eureka server
2. Verify `eureka.client.service-url.defaultZone` is correct
3. Check service startup logs for errors
4. Ensure `spring.application.name` is set

### Circuit Breaker Always Open
**Symptoms:** All requests fail immediately
**Solutions:**
1. Check downstream service health
2. Verify network connectivity
3. Increase `failureRateThreshold` if too sensitive
4. Check circuit breaker metrics: `/actuator/metrics/resilience4j.circuitbreaker.state`

### Configuration Not Updating
**Symptoms:** Configuration changes not reflected
**Solutions:**
1. Verify changes are committed to Git repo
2. Clear local cache: `spring.cloud.config.discovery.enabled: true`
3. Trigger refresh: `POST /actuator/refresh` on service
4. Check Config Server logs for Git errors

### High Latency Through Gateway
**Symptoms:** Slow response times
**Solutions:**
1. Check gateway route configuration
2. Monitor downstream service performance
3. Verify load balancing is working
4. Check for circuit breaker open states

---

## Performance Tuning

### Eureka Configuration
```yaml
eureka:
  server:
    response-cache-auto-expiration-in-seconds: 180
    response-cache-update-interval-ms: 30000
    enable-self-preservation: true
    renewal-percent-threshold: 0.85
```

### Circuit Breaker Tuning
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 50      # Increase for better detection
        minimumNumberOfCalls: 10   # Wait for more calls
        failureRateThreshold: 60   # Higher threshold = less sensitive
        waitDurationInOpenState: 60s  # Longer wait for recovery
```

### Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

---

## Security Considerations

### 1. Eureka Server Security
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: https://user:password@eureka.example.com:8761/eureka/
```

### 2. Config Server Security
- Use encrypted Git repositories
- Implement Spring Cloud Config encryption
- Use environment variables for sensitive data

### 3. API Gateway Security
- Enable HTTPS
- Implement API key authentication
- Rate limiting per consumer

### 4. Service-to-Service Communication
- Use OAuth2/JWT for inter-service calls
- Enable mutual TLS if in Kubernetes
- Implement service account authentication

---

## References

- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- [Resilience4J](https://resilience4j.readme.io/)
- [Spring Cloud Discovery](https://spring.io/projects/spring-cloud-commons)

---

## Support & Maintenance

### Regular Maintenance Tasks
1. Monitor Eureka registry for stale instances
2. Audit circuit breaker states weekly
3. Review Config Server Git commits
4. Update dependencies monthly

### Backup & Recovery
1. Backup Config Server Git repository
2. Backup Eureka instance metadata (if persisted)
3. Document current routing configuration
4. Maintain disaster recovery runbook

---

**Document Version:** 1.0.0
**Last Updated:** 2024
**Maintained By:** DataShield Platform Team
