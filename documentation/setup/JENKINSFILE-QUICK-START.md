# Quick Reference - Master Jenkinsfiles

## 🎯 CHOOSE YOUR PIPELINE

### Option A: Build Only (Recommended First Use)
```
Script Path: Jenkinsfile-build-all-projects
Output: ${WORKSPACE}/build-output/
Time: ~15-20 min
Action: Builds all, no deployment
```

### Option B: Full Stack (Dev/Test)
```
Script Path: Jenkinsfile-complete-pipeline
Output: ${WORKSPACE}/deploy/
Time: ~20-30 min
Action: Builds, deploys, starts all services
```

---

## 📦 WHAT GETS BUILT

| Type | Count | Projects |
|------|-------|----------|
| Java/Maven | 27 | auth, config, audit, analytics, etc |
| Python | 4 | ai-analysis, anomaly-detection, pii-detection, risk-scoring |
| React UI | 3 | compliance-dashboard, consent-widget, data-principal-portal |
| **TOTAL** | **34** | **All tech stacks in one build** |

---

## 🚀 QUICK START

### Step 1: Jenkins Setup
```
1. Create new Pipeline job
2. Pipeline → Definition: "Pipeline script from SCM"
3. SCM → Repository: your git repo
4. Pipeline → Script Path: Jenkinsfile-build-all-projects
5. Click BUILD
```

### Step 2: Wait for Build
```
Logs show:
- Checking out...
- Building all Maven services...
- Building Python services...
- Building UI projects...
- Complete!
```

### Step 3: Access Artifacts
```
Workspace/build-output/
├── auth-service/auth-service.jar
├── analytics-service/analytics.jar
├── ai-analysis/requirements.txt
├── compliance-dashboard/index.html
└── ...
```

---

## 🔥 PARALLEL BUILDS (FAST!)

```
WITHOUT Parallelization:
build1: 2 min → build2: 2 min → build3: 2 min = 6 minutes

WITH Parallelization (OUR PIPELINE):
build1: 2 min
build2: 2 min
build3: 2 min
= 2 minutes (all run together!)

SPEED GAIN: 3x faster ⚡
```

---

## 📊 PORT MAPPING (Jenkinsfile-complete-pipeline)

```
Java Services:     8001-8027
React UIs:         3000-3002
Python Services:   5000-5003

Access:
http://localhost:8001   → auth-service
http://localhost:3000   → compliance-dashboard
http://localhost:5000   → ai-analysis
```

---

## 📂 OUTPUT STRUCTURE

```
Jenkinsfile-build-all-projects:
${WORKSPACE}/build-output/
├── *-service/
│   └── *.jar
├── ai-analysis/
│   ├── requirements.txt
│   └── app/
└── compliance-dashboard/
    ├── index.html
    └── js/

Jenkinsfile-complete-pipeline:
${WORKSPACE}/deploy/
├── services/          (all Java JARs)
├── ui/                (React builds)
├── logs/              (service logs)
└── BUILD_MANIFEST.txt
```

---

## 🔧 KEY FEATURES

```
✓ NO hardcoded paths (D:/, /opt/, etc)
✓ Works anywhere (Windows, Linux, macOS)
✓ Uses ${WORKSPACE} - Jenkins portable
✓ Parallel builds (all at once)
✓ Auto-organizes artifacts
✓ Health checks included
✓ Comprehensive logging
✓ Error handling
```

---

## ❓ COMMON QUESTIONS

**Q: How long does build take?**
A: 15-30 min depending on machine (parallel = faster)

**Q: Can I use it without Jenkins?**
A: Yes, they're Groovy/Bash - can run locally

**Q: Do I need to modify paths?**
A: No! Uses ${WORKSPACE} - portable

**Q: What if a service fails?**
A: Pipeline stops, shows error, others are still built

**Q: Can I run just one service?**
A: Yes, remove from list or run individual mvn/npm/pip

**Q: Where are logs?**
A: `${WORKSPACE}/deploy/logs/service-name.log`

---

## 🚨 TROUBLESHOOTING 30-SECONDS

```
Maven not found?
→ Update Jenkins Docker image with Maven

npm fails?
→ Add: npm install --legacy-peer-deps -q

Port 8001 in use?
→ pkill -f java

Services not starting?
→ Check logs: tail -f deploy/logs/*.log

Artifact not copied?
→ Check BUILD_MANIFEST.txt
```

---

## 💡 TIPS

1. Use build-all-projects for CI (faster)
2. Use complete-pipeline for local dev (full verification)
3. Check BUILD_MANIFEST.txt for summary
4. Parallel = uses lots of CPU/Memory
5. Logs auto-cleaned (keeps last 10 builds)
6. Archive artifacts auto-enabled in Jenkins

---

## 🎯 RECOMMENDED WORKFLOW

```
1. First run: Jenkinsfile-build-all-projects
   ↓ Verify all projects compile
   
2. Second run: Jenkinsfile-complete-pipeline  
   ↓ Verify services start and are healthy
   
3. Then use in: CI/CD pipelines
   ↓ Automate your entire build system
```

---

## 📞 GET HELP

1. Check: MASTER-JENKINSFILE-README.md (detailed docs)
2. Check: Jenkins logs (red text = errors)
3. Check: deploy/logs/service-name.log (service logs)
4. Check: BUILD_MANIFEST.txt (what was built)

---

**Platform Independent | Zero Hardcoded Paths | Full Stack Support**

Created 2026-07-25
