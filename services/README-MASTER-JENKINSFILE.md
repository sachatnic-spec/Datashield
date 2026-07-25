# Master Jenkinsfiles - Build All Services at Once

## Files Created

### 1. **Jenkinsfile-build-all** (Simple Build & Deploy)
   - **Purpose:** Builds all 27 Maven services in parallel
   - **Output:** Deploys JARs to `deploy/` folder
   - **Time:** ~10-15 minutes
   - **Use case:** CI/CD builds without starting services

### 2. **Jenkinsfile-build-deploy-run** (Full Pipeline)
   - **Purpose:** Build → Deploy → Start all services
   - **Features:**
     - Builds all services in parallel
     - Deploys to individual service folders
     - Stops previous instances
     - Starts all services with unique ports (8001-8027)
     - Health checks on key services
   - **Time:** ~15-20 minutes
   - **Use case:** Full stack startup/testing

---

## Jenkins Setup

### Using in Jenkins UI:

1. **Create a new Pipeline job** in Jenkins
2. **Pipeline section:**
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** Your git repo
   - **Script Path:** `services/Jenkinsfile-build-all` (or `-build-deploy-run`)

OR

3. **For declarative pipeline:**
   ```
   @Library('shared-library') _
   
   // In Pipeline script:
   pipeline {
       agent any
       stages {
           stage('Build All') {
               steps {
                   sh 'mvn -pl services -am clean package -DskipTests'
               }
           }
       }
   }
   ```

---

## Services Included (27 Total)

### Maven Services:
- ai-analysis-service
- analytics-service
- anomaly-detection-service
- audit-service
- auth-service
- breach-service
- config-service
- connector-service
- consent-service
- data-classification-service
- data-discovery-service
- data-lineage-service
- dpbi-service
- grievance-service
- notification-service
- pii-detection-service
- policy-service
- report-service
- retention-service
- rights-service
- risk-scoring-service
- search-service
- siem-service
- tenant-service
- vendor-service
- webhook-service
- workflow-service

### Separate Services (not in master build):
- **Python:** ai-analysis, anomaly-detection, pii-detection, risk-scoring
- **Integration:** integration-tests

---

## Port Mapping (Jenkinsfile-build-deploy-run)

| Service | Port | Service | Port |
|---------|------|---------|------|
| auth-service | 8001 | grievance-service | 8015 |
| config-service | 8002 | pii-detection-service | 8016 |
| audit-service | 8003 | policy-service | 8017 |
| notification-service | 8004 | report-service | 8018 |
| analytics-service | 8005 | retention-service | 8019 |
| ai-analysis-service | 8006 | rights-service | 8020 |
| anomaly-detection-service | 8007 | risk-scoring-service | 8021 |
| breach-service | 8008 | search-service | 8022 |
| consent-service | 8009 | siem-service | 8023 |
| connector-service | 8010 | tenant-service | 8024 |
| data-classification-service | 8011 | vendor-service | 8025 |
| data-discovery-service | 8012 | webhook-service | 8026 |
| data-lineage-service | 8013 | workflow-service | 8027 |
| dpbi-service | 8014 | | |

---

## Usage Examples

### Build Only (No Startup):
```bash
# In Jenkins, use: Jenkinsfile-build-all
# Or manually:
cd services
for dir in *-service; do
    cd $dir && mvn clean package -DskipTests && cd ..
done
```

### Build & Deploy & Run All:
```bash
# In Jenkins, use: Jenkinsfile-build-deploy-run
# Services start on ports 8001-8027
```

### Check Service Status:
```bash
curl http://localhost:8001/actuator/health  # auth-service
curl http://localhost:8005/actuator/health  # analytics-service
```

### View Logs:
```bash
# All logs in:
tail -f deploy/logs/*.log
```

---

## Troubleshooting

### Maven Not Found:
- Ensure Jenkins Docker image includes Maven (see docker-compose.yml)
- Run: `docker-compose down && docker-compose up -d`

### Port Already in Use:
- Kill previous Java processes: `pkill -f java`
- Or change port mapping in Jenkinsfile-build-deploy-run

### Build Failures:
- Check individual service logs: `tail -f deploy/logs/service-name.log`
- Ensure all dependencies are available (check pom.xml)

---

## Customization

### To modify ports:
Edit the `servicePorts` map in `Jenkinsfile-build-deploy-run`:
```groovy
def servicePorts = [
    'auth-service': '9001',      // Changed from 8001
    'config-service': '9002',    // Changed from 8002
    // ... etc
]
```

### To exclude a service:
Remove from the `mavenServices` list in either Jenkinsfile

### To add custom build flags:
Modify the mvn command:
```groovy
mvn clean package -DskipTests -X  // Add debug output
mvn clean package -P prod         // Use prod profile
```

---

## Notes

- **Parallelization:** Uses `parallel` to build all services simultaneously
- **Timeout:** Set to 2-3 hours to prevent premature termination
- **Artifacts:** Automatically archived in Jenkins (last 10 builds)
- **Logs:** Stored in `deploy/logs/` for debugging

---

Created: 2026-07-25
Datasheild - All Services Master Build Pipeline
