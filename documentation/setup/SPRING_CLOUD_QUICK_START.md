# Spring Cloud Infrastructure - Quick Start Guide

## Prerequisites
- Java 21 (or 17+)
- Maven 3.8.1+
- PostgreSQL 12+
- Redis 6.0+
- Kafka 3.0+ (optional, for event streaming)

## Quick Setup (5 minutes)

### 1. Start Core Infrastructure

#### Terminal 1: Service Registry
```bash
cd services/service-registry
mvn clean install spring-boot:run
```
✓ Eureka Dashboard: http://localhost:8761/

#### Terminal 2: Config Server
```bash
cd services/config-service
mvn clean install spring-boot:run
```
✓ Config Server: http://localhost:8012/

#### Terminal 3: API Gateway
```bash
cd services/api-gateway
mvn clean install spring-boot:run
```
✓ API Gateway: http://localhost:8080/

### 2. Start Microservices

In separate terminals, start the core services:

```bash
# Terminal 4: Auth Service
cd services/auth-service && mvn clean install spring-boot:run

# Terminal 5: Tenant Service
cd services/tenant-service && mvn clean install spring-boot:run

# Terminal 6: Grievance Service
cd services/grievance-service && mvn clean install spring-boot:run

# Terminal 7: Others...
cd services/consent-service && mvn clean install spring-boot:run
cd services/rights-service && mvn clean install spring-boot:run
cd services/breach-service && mvn clean install spring-boot:run
cd services/audit-service && mvn clean install spring-boot:run
cd services/notification-service && mvn clean install spring-boot:run
```

## Verification Checklist

✓ **Eureka Dashboard:** http://localhost:8761/
- Should show registered services (as they start)
- Status: UP (green)

✓ **Health Checks:**
```bash
curl http://localhost:8761/actuator/health     # Eureka
curl http://localhost:8012/actuator/health     # Config Server
curl http://localhost:8080/actuator/health     # API Gateway
```

✓ **API Gateway Routes:**
```bash
curl http://localhost:8080/actuator/gateway/routes
```

✓ **List Services:**
```bash
curl http://localhost:8761/eureka/apps
```

## Quick Tests

### 1. Through API Gateway
```bash
# Example: Call auth-service through gateway
curl http://localhost:8080/api/auth/health

# Call any service
curl http://localhost:8080/api/<service>/<endpoint>
```

### 2. Direct Service Call
```bash
# Call service directly (bypassing gateway)
curl http://localhost:8081/actuator/health    # Auth service
curl http://localhost:8082/actuator/health    # Tenant service
```

### 3. Config Server Access
```bash
# Get auth-service configuration
curl http://localhost:8012/auth-service/default

# Get with profile
curl http://localhost:8012/auth-service/production
```

### 4. Circuit Breaker Status
```bash
# Check circuit breaker metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

## Common Commands

### Build All Services
```bash
# From project root
mvn clean install -DskipTests

# Or with logging
mvn clean install -DskipTests -X
```

### Build Specific Service
```bash
cd services/auth-service
mvn clean install
```

### Run Tests
```bash
cd services/auth-service
mvn test
```

### Clean Build
```bash
# Remove compiled artifacts
mvn clean

# Remove completely
rm -rf target/
```

### Check Dependencies
```bash
# Show dependency tree
mvn dependency:tree

# Check for updates
mvn versions:display-dependency-updates
```

## Troubleshooting Quick Fixes

### Services Not Showing in Eureka
```bash
# 1. Check service is running
curl http://localhost:<PORT>/actuator/health

# 2. Check Eureka connectivity
docker logs <service-container>  # or check terminal output

# 3. Verify application.yml has Eureka config
cat services/<service>/src/main/resources/application.yml | grep eureka
```

### Configuration Not Updating
```bash
# 1. Check Git repo
cd config-repo && git status

# 2. Trigger config refresh
curl -X POST http://localhost:<SERVICE_PORT>/actuator/refresh

# 3. Check Config Server logs
# Look for Git-related errors
```

### Port Conflicts
```bash
# Check which process uses port
lsof -i :8761    # Eureka
lsof -i :8012    # Config
lsof -i :8080    # Gateway
lsof -i :8081    # Auth service
```

### Build Issues
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U  # Force update dependencies
```

## Useful URLs

| Component | URL | Purpose |
|-----------|-----|---------|
| Eureka Dashboard | http://localhost:8761/ | Service registry & monitoring |
| Config Server | http://localhost:8012/ | Configuration retrieval |
| API Gateway | http://localhost:8080/ | API routing & aggregation |
| Auth Service | http://localhost:8081/ | Authentication & authorization |
| Tenant Service | http://localhost:8082/ | Tenant management |
| Any Service Health | http://localhost:<PORT>/actuator/health | Service health status |
| Prometheus Metrics | http://localhost:<PORT>/actuator/prometheus | Metrics for Prometheus |
| Gateway Routes | http://localhost:8080/actuator/gateway/routes | Available routes |

## Development Tips

### Enable Debug Logging
Add to `application.yml`:
```yaml
logging:
  level:
    org.springframework.cloud: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

### Monitor Service Registration
```bash
# Watch Eureka registry (repeat every 2 seconds)
watch -n 2 'curl -s http://localhost:8761/eureka/apps | grep -i status'
```

### Test Service Discovery
```bash
# From API Gateway or any service
curl http://localhost:8080/actuator/serviceresolverinstances/AUTH-SERVICE
```

### View Gateway Request/Response
Enable detailed logging:
```yaml
logging:
  level:
    org.springframework.cloud.gateway.filter: DEBUG
    org.springframework.cloud.gateway.filter.route: DEBUG
```

## Next Steps

1. ✓ Verify all services are running
2. ✓ Check Eureka dashboard for all services
3. ✓ Test API Gateway routing
4. ✓ Verify circuit breaker functionality
5. Read detailed documentation: `SPRING_CLOUD_INFRASTRUCTURE.md`

## Scale to Multiple Instances

To run multiple instances of a service:

**Terminal 1:**
```bash
cd services/auth-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

**Terminal 2:**
```bash
cd services/auth-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
```

✓ Both instances register with Eureka
✓ API Gateway load balances between them

## Docker Quick Start (Optional)

```bash
# Build images for all services
./scripts/docker-build-all.sh

# Run with Docker Compose
docker-compose -f docker-compose.services.yml up

# Scale services
docker-compose -f docker-compose.services.yml up --scale auth-service=3
```

---

**Happy Microservicing!** 🚀

For detailed documentation, see: `SPRING_CLOUD_INFRASTRUCTURE.md`
