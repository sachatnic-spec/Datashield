# DataShield India — Jenkins local auto-deploy

Files in this folder:

```
jenkins-setup/
├── docker-compose.yml       # Jenkins controller (Docker)
├── Jenkinsfile              # Pipeline: checkout → build → deploy
└── scripts/
    ├── build-all.bat        # Builds everything, timestamped logs
    └── deploy-all.bat       # Stops old instances, starts new ones
```

Copy `Jenkinsfile` and `scripts/` into the **root** of your DataShield
India repo (same level as `services/`, `frontend/`, `tools/`). Keep
`docker-compose.yml` wherever you like — it only runs the controller.

---

## 1. Start the Jenkins controller

```
docker compose up -d
```

Open `http://localhost:8080`. Get the initial admin password:

```
docker exec jenkins-controller cat /var/jenkins_home/secrets/initialAdminPassword
```

Install **suggested plugins**, then also install (Manage Jenkins →
Plugins → Available):
- **GitHub** (webhook trigger support)
- **Pipeline** (usually already in "suggested")

Create your admin user when prompted.

## 2. Register your Windows machine as an agent

The controller is in Docker, but `mvn`, `npm`, `python`, and `java`
need to run for real on your Windows host — so the host itself
becomes an agent node named `windows-local` (matches the
`agent { label 'windows-local' }` in the Jenkinsfile).

1. Manage Jenkins → Nodes → **New Node** → name it `windows-local`,
   type "Permanent Agent".
2. Remote root directory: e.g. `C:\jenkins-agent`
3. Labels: `windows-local`
4. Launch method: **Launch agent by connecting it to the controller**
   (JNLP/inbound)
5. Save. Jenkins shows you a command + a link to download
   `agent.jar`.
6. On your Windows machine, in the agent folder, run the command
   Jenkins gives you, e.g.:
   ```
   java -jar agent.jar -url http://localhost:8080/ -secret <SECRET> -name "windows-local" -workDir "C:\jenkins-agent"
   ```
   (Since the controller is in Docker with port `8080` published to
   the host, `localhost:8080` from the Windows side works fine.)
7. To keep this always running, install it as a Windows service:
   `java -jar agent.jar ... -install` from an elevated prompt, or use
   NSSM/Task Scheduler to launch it at login.

Once connected, the node shows "Connected" in Manage Jenkins → Nodes.

## 3. Expose Jenkins to GitHub via ngrok

GitHub needs a public URL to send the webhook to; your local Jenkins
isn't reachable from the internet by default.

```
ngrok http 8080
```

This gives you a URL like `https://a1b2c3d4.ngrok-free.app`. Keep this
terminal running (or use `ngrok`'s paid static domain so the URL
doesn't change every restart).

## 4. Configure the GitHub webhook

In your GitHub repo → Settings → Webhooks → **Add webhook**:
- Payload URL: `https://<your-ngrok-domain>/github-webhook/`
  (trailing slash matters)
- Content type: `application/json`
- Events: just the **push** event
- Save

## 5. Create the Pipeline job

Jenkins → New Item → name it `datashield-india` → **Pipeline**.

- Under "Pipeline", set **Definition** to "Pipeline script from SCM"
- SCM: Git, paste your repo URL, add credentials if the repo is
  private (a GitHub PAT works: username = your GitHub username,
  password = the token)
- Script Path: `Jenkinsfile`
- Save

The `triggers { githubPush() }` block in the Jenkinsfile handles the
rest — no extra checkbox needed once the job has run at least once
with that trigger defined.

## 6. Test it

```
git commit --allow-empty -m "test jenkins webhook"
git push
```

Within a few seconds Jenkins should start a build automatically. Watch
it under the job's **Build History**. Each build:

1. Checks out your latest code onto the Windows agent
2. Runs `build-all.bat` — logs land in
   `logs\build_<timestamp>\`, one file per service plus
   `summary.log`. Any Maven compile error shows up in that service's
   own log and the last 15 lines get echoed into the console output,
   so you can see which service and which error without opening
   files manually.
3. If the build stage fails, Jenkins stops there and marks the build
   red — deploy never runs against a broken build.
4. Runs `deploy-all.bat` — kills whatever was previously listening
   on each service's port, starts the freshly built jars / uvicorn
   processes, waits for `/actuator/health` (or `/health`) on each.
5. Archives everything under `logs/**` on the build page, so you can
   open any past build in Jenkins and download its exact logs.

## Building / deploying a single service

Both scripts and the Jenkinsfile now accept an optional service name:

```
scripts\build-all.bat consent-service     :: builds only consent-service
scripts\deploy-all.bat consent-service    :: restarts only consent-service
scripts\build-all.bat frontend            :: builds only the Angular app
scripts\build-all.bat pii-detection       :: builds only that Python service
```

Run with no argument to build/deploy everything, same as before.

Notes on single-service mode:
- `build-all.bat <name>` **skips the root `mvn install`** step to keep
  it fast. If you've changed a shared library / parent POM, run
  `build-all.bat` with no argument at least once first so `.m2` has
  the latest shared jars.
- `deploy-all.bat <name>` only kills and restarts the port for that
  one service — every other running service is left untouched.
- An unrecognized name (typo, wrong casing on the check but names are
  matched case-insensitively) causes the script to log
  `[ERROR] Unknown service name: ...` and exit non-zero, so Jenkins
  will show it clearly rather than silently doing nothing.

**From the Jenkins UI**: open the job → **Build with Parameters** →
type the service name into `SERVICE_NAME` → Build. Leave it blank for
a full build+deploy. Note the automatic webhook-triggered runs (on
git push) always do a full build, since GitHub's push event can't
supply a parameter value — parameterized single-service runs are a
manual/on-demand action.

## Individual jobs per service (recommended for day-to-day work)

The single-parameterized-job setup above is great for "rebuild
everything." But if you want to **enable/disable, retrigger, or check
build history for one service independently of the others**, use one
Jenkins job per service instead.

### What's different

Each service gets its own tiny `Jenkinsfile`, colocated with its code:

```
services/consent-service/Jenkinsfile
services/auth-service/Jenkinsfile
...
frontend/Jenkinsfile
services/pii-detection/Jenkinsfile      <- Python
```

Every one of these:
- Has its own `githubPush()` trigger
- Only actually **builds/deploys if a file under that service's own
  folder changed** (via `when { changeset "services/<name>/**" } }`)
  — a push that only touches `consent-service` won't rebuild
  `auth-service`'s job, even though both jobs receive the webhook
  ping and run a quick checkout
- Always runs when you click **Build Now** manually, regardless of
  changeset (via the `triggeredBy 'UserIdCause'` check)
- Archives its own logs, has its own success/failure history

The 32 generated Jenkinsfiles are in this package under
`per-service-jenkinsfiles/<service-name>/Jenkinsfile` — copy each one
into the matching real folder in your repo (`services/<name>/` for
backend/Python services, `frontend/` for the Angular app), commit,
and push.

### Creating the Jenkins jobs

For each service, in Jenkins: **New Item** → name it e.g.
`ds-consent-service` → **Pipeline** →
- Pipeline → Definition: "Pipeline script from SCM"
- SCM: Git, same repo URL/credentials as before
- Script Path: `services/consent-service/Jenkinsfile` (adjust per
  service; `frontend/Jenkinsfile` for the frontend one)
- Save

Repeat for every service you want managed independently. It's
tedious to click through 30+ times by hand — if you'd rather script
job creation, Jenkins' **Job DSL plugin** or the `curl -X POST
.../createItem` REST API can create them all from a loop; ask me and
I'll write that script too.

Once created, each job shows up separately in the Jenkins dashboard
with its own status icon, build history, and a "disable project"
option — so if `webhook-service` is being flaky, you can disable just
that job without touching the other 31.

### Why the "still runs a checkout" tradeoff is fine

A `git push` fires the webhook once, and every job with
`githubPush()` wakes up and runs its `Checkout` stage — that's a
few seconds each, not a full Maven build. The `when { changeset }`
guard means only the job(s) whose folder actually changed proceed
past that into `mvn clean package` / restart. For 30+ jobs this is a
handful of quick git operations, not 30 full builds, every time you
push.

## Notes

- `build-all.bat` and `deploy-all.bat` are plain `.bat` files — you
  can also run either one by hand from a Windows terminal, no Jenkins
  required, for local debugging.
- The service list is hardcoded at the top/middle of each script.
  Add or remove `call :build_service "..."` / `call :deploy_java "..." "port"`
  lines if your service list changes.
- If you'd rather trigger on a schedule than a webhook (e.g. no
  ngrok), replace `triggers { githubPush() }` with
  `triggers { pollSCM('H/5 * * * *') }` in the Jenkinsfile — polls
  every 5 minutes, no public URL needed.
