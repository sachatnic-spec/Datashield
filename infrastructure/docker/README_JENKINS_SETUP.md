# Jenkins Setup for DataShield

This guide explains how to set up Jenkins for DataShield CI/CD pipelines.

## Quick Start (Docker - Recommended)

### 1. Build and Run Jenkins with Docker Compose

```bash
cd docker
docker-compose -f docker-compose.jenkins.yml up -d
```

**What's included:**
- Jenkins LTS with all build tools pre-installed
- Java 21 + Maven
- Go
- Python 3
- Node.js + npm
- Git, curl, wget
- Jenkins plugins (workflow, git, github, docker)

### 2. Access Jenkins

- URL: `http://localhost:8080`
- First time: Follow setup wizard to get initial admin password
- Create username and password

### 3. Configure GitHub Credentials

1. Jenkins → Manage Jenkins → Credentials
2. Add new credential: GitHub Personal Access Token
   - Kind: Username with password
   - Username: `github-username`
   - Password: `your-github-pat-token`
   - ID: `github-pat`

### 4. Create Per-Service Jobs

**Option A: Automatic (PowerShell)**
```powershell
cd jenkins-setup
.\create-jobs.ps1 -JenkinsUrl "http://localhost:8080" -Username "admin" -ApiToken "YOUR_API_TOKEN"
```

**Option B: Job DSL**
1. Install "Job DSL" plugin
2. Create Freestyle job: `generate-datashield-jobs`
3. Build step: "Process Job DSLs" → `jenkins-setup/generate-jobs.groovy`
4. Run the job

### 5. GitHub Webhook (Optional - for instant builds)

1. GitHub repo → Settings → Webhooks → Add webhook
2. Payload URL: `http://<YOUR_JENKINS_IP>:8080/github-webhook/`
3. Content type: `application/json`
4. Triggers: Push events
5. Jenkins will now auto-trigger on every push

---

## Manual Setup (Without Docker)

### Requirements

Install on your Jenkins server:

```bash
# Ubuntu/Debian
sudo apt-get install -y \
  openjdk-21-jdk-headless \
  maven \
  golang-go \
  python3 \
  python3-pip \
  nodejs \
  npm \
  git

# Verify
java -version
mvn -version
go version
python3 --version
node --version
npm --version
```

### Windows Agent Setup

Install on Windows Jenkins agent:

- **Java**: Download JDK 21 from oracle.com, add to PATH
- **Maven**: Download from maven.apache.org, add to PATH
- **Go**: Download from golang.org, add to PATH
- **Python**: Download from python.org, add to PATH
- **Node**: Download from nodejs.org, add to PATH
- **Git**: Already required by Jenkins

---

## Jenkinsfile Behavior

Each service's Jenkinsfile:
- ✅ Auto-detects Linux (sh) or Windows (bat) agents
- ✅ Runs `mvn clean install` for Java services
- ✅ Runs `go build` for Go services
- ✅ Packages output to `new_build_<timestamp>` folder
- ✅ Starts the service in background

---

## Troubleshooting

### "mvn: not found"
- Cause: Maven not installed on Jenkins agent
- Fix: Use Docker Compose (above) or install Maven manually

### "Branch not found"
- Cause: Jenkins hasn't fetched latest commits
- Fix: Manual build or wait for SCM poll (5 min default)

### Webhook not triggering
- Check GitHub webhook delivery: Settings → Webhooks → Recent deliveries
- Verify Jenkins URL is accessible from GitHub
- Check Jenkins GitHub plugin is installed

### Build times out
- Check: Is the service taking >15 min to build?
- Solution: Increase timeout in Jenkinsfile or optimize build

---

## Environment Variables

Each job has access to:

```groovy
env.BUILD_NUMBER       // e.g., 123
env.BUILD_ID           // e.g., 123
env.WORKSPACE          // /var/jenkins_home/workspace/DataShield-auth-service
env.GIT_COMMIT         // git commit hash
env.GIT_BRANCH         // e.g., main
```

---

## Next Steps

1. ✅ Jenkins running
2. ✅ Per-service jobs created
3. ✅ GitHub webhook configured (optional)
4. → Commit to `services/auth-service/` → build auto-triggers
5. → Check build logs in Jenkins UI
6. → Artifacts in `new_build_<timestamp>/` folder on agent

---

## Support

- Jenkins Docs: https://www.jenkins.io/doc/
- Pipeline Syntax: https://www.jenkins.io/doc/book/pipeline/
- GitHub integration: https://plugins.jenkins.io/github/
