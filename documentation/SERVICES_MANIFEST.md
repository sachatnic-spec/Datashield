# DataShield India — Service Manifest (27 services)

| # | Service | Type | Port | Health Path | Registered in parent pom.xml? |
|---|---|---|---|---|---|
| 1 | auth-service | Maven | 8001 | /v1/auth/health | Yes |
| 2 | consent-service | Maven | 8002 | /actuator/health | Yes |
| 3 | rights-service | Maven | 8003 | /actuator/health | Yes |
| 4 | breach-service | Maven | 8004 | /actuator/health | Yes |
| 5 | notification-service | Maven | 8005 | /actuator/health | Yes |
| 6 | audit-service | Maven | 8006 | /actuator/health | Yes |
| 7 | tenant-service | Maven | 8007 | /actuator/health | Yes |
| 8 | vendor-service | Maven | 8008* | /actuator/health | **No — add to <modules>** |
| 9 | policy-service | Maven | 8009* | /actuator/health | **No — add to <modules>** |
| 10 | retention-service | Maven | 8010* | /actuator/health | **No — add to <modules>** |
| 11 | grievance-service | Maven | 8011* | /actuator/health | **No — add to <modules>** |
| 12 | discovery-service | Maven | 8012* | /actuator/health | **No — add to <modules>** |
| 13 | classification-service | Maven | 8013* | /actuator/health | **No — add to <modules>** |
| 14 | lineage-service | Maven | 8014* | /actuator/health | **No — add to <modules>** |
| 15 | workflow-service | Maven | 8015* | /actuator/health | **No — add to <modules>** |
| 16 | analytics-service | Maven | 8016* | /actuator/health | **No — add to <modules>** |
| 17 | report-service | Maven | 8017* | /actuator/health | **No — add to <modules>** |
| 18 | ai-analysis-service | Python | 8018 | /health | N/A (pip-based) |
| 19 | pii-detection-service | Python | 8019 | /health | N/A (pip-based) |
| 20 | risk-scoring-service | Python | 8020 | /health | N/A (pip-based) |
| 21 | anomaly-service | Python | 8021 | /health | N/A (pip-based) |
| 22 | connector-service | Maven | 8022 | /health | Yes |
| 23 | webhook-service | Maven | 8023 | /health | Yes |
| 24 | siem-service | Maven | 8024* | /health | **No — add to <modules>** |
| 25 | dpbi-service | Maven | 8025 | /health | Yes |
| 26 | config-service | Maven | 8026* | /actuator/health | **No — add to <modules>** |
| 27 | search-service | Maven | 8027* | /actuator/health | **No — add to <modules>** |

`*` = port not confirmed in `docker-compose.services.yml` — placeholder assigned sequentially. Confirm/update in the Jenkinsfile's `SERVICE_PORT` env var before first run.

## Action items before these pipelines will actually build green

1. **13 services aren't yet Maven reactor modules.** Add each to the root `pom.xml`:
   ```xml
   <modules>
     ...
     <module>services/vendor-service</module>
     <module>services/policy-service</module>
     <!-- etc. -->
   </modules>
   ```
   Until then, their Jenkins jobs will fail at the `mvn -pl` build stage (that's expected, not a bug in the pipeline — the module just doesn't exist for Maven yet).

2. **Commit the Jenkinsfiles.** Copy the `per-service/<name>/Jenkinsfile` into `services/<name>/Jenkinsfile` in your repo, then commit + push to `main`. Each Jenkins job is already configured to pull its Jenkinsfile from `services/<name>/Jenkinsfile` on the `main` branch.

3. **Commit the shared scripts.** Copy the `scripts/` folder to `jenkins-setup/scripts/` in your repo root (all Jenkinsfiles reference `${WORKSPACE}\jenkins-setup\scripts\...`).

4. **Confirm the 12 TBD ports** against your actual `application.yml`/`.env` per service, and adjust the manifest + Jenkinsfiles accordingly.
