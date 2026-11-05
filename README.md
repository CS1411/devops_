# Service Status Monitor

A small Spring Boot (Java 17) REST service that checks a list of URLs and reports which are up.
It is the sample app for a DevOps pipeline: Git -> Maven -> Jenkins -> Ansible -> Docker -> Kubernetes.

## Endpoints

| Path      | What it returns |
|-----------|-----------------|
| `/`       | HTML dashboard: one green (UP) or red (DOWN) row per URL |
| `/status` | JSON list: `url`, `up`, `httpCode`, `latencyMs`, `error` for every configured URL |
| `/health` | `{"status":"UP","servedBy":"<hostname>","monitoredUrls":N}`, never calls external URLs |

## Configuration

| Env variable   | Default |
|----------------|---------|
| `SERVER_PORT`  | `8081` |
| `MONITOR_URLS` | `https://www.google.com,https://github.com,https://this-site-does-not-exist.invalid` |

## Build and run

```bash
mvn clean package
java -jar target/status-monitor-1.0.jar
curl localhost:8081/health
```

## Layout

| Path | Purpose |
|------|---------|
| `Jenkinsfile` | CI pipeline: Checkout, Test, Build, Archive (versioned JAR) |
| `ansible/` | Playbook + `status_monitor` role that installs Java and runs the JAR as a systemd service |
| `Dockerfile` | Runtime image on `eclipse-temurin:17-jre` |
| `k8s/` | ConfigMap, Deployment (3 replicas, probes) and NodePort Service |

See `RUNBOOK.md` for the full step-by-step demo.

CI: Jenkins job `status-monitor-pipeline` polls this repo every minute and builds `main` automatically.
Live demo Wed Nov  4 13:57:43 UTC 2026
Live demo Thu Nov  5 06:25:31 UTC 2026
