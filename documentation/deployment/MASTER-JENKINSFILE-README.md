# Master Jenkinsfiles - Build Everything at Once

## 📋 Files Created

### 1. **Jenkinsfile-build-all-projects** (Recommended Start)
   - Builds all projects (Maven, Python, UI)
   - **No deployment or startup**
   - All artifacts moved to `${WORKSPACE}/build-output/`
   - **Use for:** CI builds, artifact collection
   - ⏱️ ~15-20 minutes

### 2. **Jenkinsfile-complete-pipeline** (Full Stack)
   - Build → Deploy → Start all services
   - Organizes artifacts in `${WORKSPACE}/deploy/`
   - Starts Java services on ports 8001-8027
   - Starts UIs on ports 3000+
   - Starts Python on ports 5000+
   - **Use for:** Full integration testing, local development
   - ⏱️ ~20-30 minutes

---

## 🎯 Projects Included

### Java/Maven Services (27)
```
ai-analysis-service          analytics-service
anomaly-detection-service    audit-service
auth-service                 breach-service
config-service               connector-service
consent-service              data-classification-service
data-discovery-service       data-lineage-service
dpbi-service                 grievance-service
notification-service         pii-detection-service
policy-service               report-service
retention-service            rights-service
risk-scoring-service         search-service
siem-service                 tenant-service
vendor-service               webhook-service
workflow-service
```

### Python Services (4)
```
ai-analysis
anomaly-detection
pii-detection
risk-scoring
```

### UI Projects (3) - React/Node
```
compliance-dashboard
consent-widget
data-principal-portal
```

---

## 🚀 Jenkins Setup

### Option 1: Jenkins UI

1. **New Pipeline Job**
   - **Name:** `Build-All-Projects`
   - **Type:** Pipeline

2. **Configure Pipeline:**
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** `https://github.com/sachatnic-spec/Datashield.git`
   - **Script Path:** `Jenkinsfile-build-all-projects`

3. **Click Build** ✅

### Option 2: Command Line

```bash
cd /path/to/Datashield
jenkins-cli create-job BuildAll < Jenkinsfile-build-all-projects
```

### Option 3: Jenkinsfile in Root

```bash
# Link to main Jenkinsfile
ln -s Jenkinsfile-build-all-projects Jenkinsfile
# Jenkins will auto-detect and use it
```

---

## 📂 Output Directories (No Hardcoded Paths!)

### Build Artifacts
```
${WORKSPACE}/
├── build-output/          # All build artifacts
│   ├── auth-service/
│   │   └── auth-service.jar
│   ├── analytics-service/
│   │   └── analytics-service.jar
│   ├── ai-analysis/
│   │   ├── requirements.txt
│   │   ├── app/
│   │   └── models/
│   ├── compliance-dashboard/
│   │   ├── index.html
│   │   ├── js/
│   │   └── css/
│   └── ...
│
├── deploy/                # Organized deployment structure
│   ├── logs/              # All service logs
│   ├── services/
│   │   ├── auth-service/
│   │   ├── analytics-service/
│   │   └── ...
│   ├── ui/
│   │   ├── compliance-dashboard/
│   │   ├── consent-widget/
│   │   └── data-principal-portal/
│   ├── ai-analysis/
│   ├── anomaly-detection/
│   ├── pii-detection/
│   ├── risk-scoring/
│   └── BUILD_MANIFEST.txt
```

---

## 🔌 Service Port Mapping

### Java Services (8001-8027)
| Service | Port |
|---------|------|
| auth-service | 8001 |
| config-service | 8002 |
| audit-service | 8003 |
| notification-service | 8004 |
| analytics-service | 8005 |
| ai-analysis-service | 8006 |
| anomaly-detection-service | 8007 |
| breach-service | 8008 |
| consent-service | 8009 |
| connector-service | 8010 |
| data-classification-service | 8011 |
| data-discovery-service | 8012 |
| data-lineage-service | 8013 |
| dpbi-service | 8014 |
| grievance-service | 8015 |
| pii-detection-service | 8016 |
| policy-service | 8017 |
| report-service | 8018 |
| retention-service | 8019 |
| rights-service | 8020 |
| risk-scoring-service | 8021 |
| search-service | 8022 |
| siem-service | 8023 |
| tenant-service | 8024 |
| vendor-service | 8025 |
| webhook-service | 8026 |
| workflow-service | 8027 |

### UI Projects (3000+)
```
compliance-dashboard   → http://localhost:3000
consent-widget         → http://localhost:3001
data-principal-portal  → http://localhost:3002
```

### Python Services (5000+)
```
ai-analysis            → http://localhost:5000
anomaly-detection      → http://localhost:5001
pii-detection          → http://localhost:5002
risk-scoring           → http://localhost:5003
```

---

## 🏗️ Build Features

### ✅ Included
- **Parallel Building:** All services build simultaneously (much faster)
- **Dynamic Paths:** Uses `${WORKSPACE}` - works anywhere
- **Multi-Stack:** Handles Maven, Python, Node/React
- **Artifact Organization:** Automatic folder structure
- **Logging:** All output captured in `deploy/logs/`
- **Health Checks:** Verifies services started
- **Build Manifest:** Summary of all built artifacts
- **Error Handling:** Graceful failures with detailed messages

### ❌ NOT Included
- ❌ No hardcoded paths (D:/, /opt/, etc)
- ❌ No fixed Jenkins home directory
- ❌ No server/environment assumptions
- ❌ Platform independent (works on Linux, Windows, macOS)

---

## 📊 Usage Examples

### Example 1: Build Only (CI Pipeline)
```bash
# Jenkins Job:
# - Script Path: Jenkinsfile-build-all-projects
# Output: ${WORKSPACE}/build-output/
# Result: All artifacts ready for deployment
```

### Example 2: Full Stack (Development/Testing)
```bash
# Jenkins Job:
# - Script Path: Jenkinsfile-complete-pipeline
# Output: Services running on ports 8001-8027, 3000-3002, 5000+
# Result: Full stack online and testable
```

### Example 3: Check Specific Service
```bash
# After pipeline runs:
tail -f deploy/logs/auth-service.log
curl http://localhost:8001/actuator/health
```

### Example 4: Manual Local Build
```bash
# Run without Jenkins:
WORKSPACE=$(pwd) bash -x Jenkinsfile-build-all-projects
```

---

## 🔧 Customization

### Change Build Directory
Edit in Jenkinsfile:
```groovy
environment {
    BUILD_DIR = "${WORKSPACE}/my-build-folder"
    DEPLOY_DIR = "${WORKSPACE}/my-deploy-folder"
}
```

### Add New Service
Add to `mavenServices` list:
```groovy
def mavenServices = [
    'new-awesome-service',  // Add here
    'existing-service',
    ...
]
```

### Exclude Service
Remove from lists:
```groovy
// Remove from this list:
def mavenServices = [
    // 'service-to-skip',
    'auth-service',
]
```

### Change Port Assignment
Edit `servicePorts` map:
```groovy
def servicePorts = [
    'auth-service': '9001',  // Changed from 8001
    'config-service': '9002'  // Changed from 8002
]
```

### Add Build Flags
Modify Maven command:
```groovy
mvn clean package -DskipTests -X  // Add -X for debug
mvn clean package -P production   // Use production profile
```

---

## 🐛 Troubleshooting

### Issue: Maven not found
**Solution:** Update Jenkins Docker image:
```yaml
# docker-compose.yml
services:
  jenkins:
    image: jenkins/jenkins:lts-jdk17
    # ... add to entrypoint:
    entrypoint: |
      sh -c 'apt-get update && apt-get install -y maven && /usr/local/bin/jenkins.sh'
```

### Issue: npm install fails
**Solution:** Use `--legacy-peer-deps`:
```groovy
npm install --legacy-peer-deps -q
```

### Issue: Python dependency errors
**Solution:** Check logs:
```bash
tail -f deploy/logs/ai-analysis.log
```

### Issue: Port already in use
**Solution:** Kill previous instances:
```bash
pkill -f java
pkill -f python
```

### Issue: Build takes too long
**Solution:** Parallel builds are already enabled. Check:
```bash
top  # Monitor CPU/Memory
ps aux | grep -E "java|npm|python"  # See running processes
```

---

## 📝 Environment Variables

All paths use `${WORKSPACE}`:
```groovy
BUILD_DIR        = "${WORKSPACE}/build-output"
DEPLOY_DIR       = "${WORKSPACE}/deploy"
LOGS_DIR         = "${WORKSPACE}/deploy/logs"
SERVICES_DIR     = "${WORKSPACE}/services"
FRONTEND_DIR     = "${WORKSPACE}/frontend"
MAVEN_OPTS       = "-Xmx2g -Xms512m"
NODE_ENV         = "production"
TIMESTAMP        = Current timestamp
```

---

## ✨ Best Practices

1. **Use Jenkinsfile-build-all-projects for CI** - faster, cleaner
2. **Use Jenkinsfile-complete-pipeline for integration testing** - full verification
3. **Check logs** - `tail -f deploy/logs/*.log`
4. **Monitor resources** - parallel builds use significant CPU/Memory
5. **Archive artifacts** - Jenkins auto-archives for history
6. **Version builds** - timestamp automatically included
7. **Clean up** - old builds auto-deleted (keep last 10)

---

## 📞 Support

Issues? Check:
1. Build logs in Jenkins UI
2. Logs: `deploy/logs/` directory
3. Manifest: `deploy/BUILD_MANIFEST.txt`
4. Individual service logs for errors

---

**Created:** 2026-07-25  
**Datashield - Complete Build Pipeline**  
**Platform Independent · Zero Hardcoded Paths · Full Stack Support**
