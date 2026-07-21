# Setup Instructions

## 1. What's already done
- All 27 Jenkins jobs exist under the `Datasheild` folder, each configured as
  "Pipeline script from SCM" pointing at `services/<name>/Jenkinsfile` on `main`
  of `https://github.com/sachatnic-spec/Datashield.git`.
- Each job has the GitHub push trigger enabled (so it will build on push, once
  the webhook below is wired up).

## 2. What you need to do
1. Copy `per-service/<name>/Jenkinsfile` → `services/<name>/Jenkinsfile` in your local clone (27 files).
2. Copy `scripts/*.ps1` → `jenkins-setup/scripts/` in your local clone.
3. `git add . && git commit -m "Add per-service Jenkinsfiles and shared deploy scripts" && git push origin main`
4. Add the 13 missing modules to the root `pom.xml` (see manifest doc) so `mvn -pl` can find them.
5. Confirm the 12 placeholder ports against your actual service configs.

## 3. GitHub Webhook (using your existing ngrok setup)
Since Jenkins runs locally on port 9250 and ngrok is already installed at `C:\tools\ngrok`:

```powershell
C:\tools\ngrok\ngrok.exe http 9250
```

Copy the `https://<random>.ngrok-free.app` URL it gives you, then in GitHub:
`Settings → Webhooks → Add webhook`
- Payload URL: `https://<random>.ngrok-free.app/github-webhook/`
- Content type: `application/json`
- Events: "Just the push event"

Every push to `main` will now hit all 27 jobs. Each job's `checkout scm` pulls the
full repo but only builds its own module — so this is safe, if not maximally
efficient (see "future" section below for per-service filtering).

**Note on ngrok for production**: ngrok tunnels are fine for local dev/testing,
but a real ngrok free-tier URL changes every restart, breaking the webhook until
you update it. For anything beyond your own testing, replace this with a static
reverse-proxy (e.g. Jenkins behind IIS/Nginx with a real domain) — see Step 6
in the full plan.

## 4. Simple → Production migration path
This setup is deliberately simple (declarative Jenkinsfile per service, shared
PowerShell scripts, one Jenkins folder). To evolve it later without a rewrite:

- **Now**: PowerShell scripts called inline from each Jenkinsfile.
  **Later**: move them into a Jenkins **Shared Library** (`vars/deployService.groovy`)
  so all 27 Jenkinsfiles shrink to a single `deployService(...)` call.
- **Now**: `bat`/`powershell` steps run directly on the Jenkins host.
  **Later**: wrap each service in its Dockerfile target (already present at repo
  root) and swap `Start-JavaService.ps1`/`Start-PythonService.ps1` for
  `docker run`/`docker compose up -d <service>`.
- **Now**: single Jenkins controller runs all jobs.
  **Later**: add Jenkins agents/nodes, or migrate to Kubernetes with the
  Kubernetes plugin — each pipeline stage becomes a pod.
- **Now**: GitHub push trigger fires every job on every push.
  **Later**: install the **Generic Webhook Trigger** plugin (or switch to a
  **Multibranch Pipeline** per service with path-based `changesets`) so only
  the job whose `services/<name>/**` files changed actually builds.
- **Now**: JAR replaced directly on disk, PID-file based stop/start.
  **Later**: blue/green or rolling deploys via container orchestration.

None of the "later" changes require restructuring the repo layout you already
have (`services/<name>/Jenkinsfile`), so this is a safe starting point.
