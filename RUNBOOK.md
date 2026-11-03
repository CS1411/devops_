# RUNBOOK

Step-by-step demo of the full pipeline. Screenshot names are the files the report expects in
`report/screenshots/`.

Repository: https://github.com/mirfarzin/devops_p1

## Component 1: Git, Maven, Jenkins

### 1. Build and run locally

```bash
mvn clean package                      # 8 tests, BUILD SUCCESS
java -jar target/status-monitor-1.0.jar &
curl localhost:8081/health
curl localhost:8081/status
```

Screenshots: `c1_maven_build.png`, `c1_app_local.png` (dashboard at http://localhost:8081).

### 2. Git history

Work is done on feature branches and merged into `main` with `git merge --no-ff`.

```bash
git log --oneline --graph --all
git push -u origin --all
```

Screenshots: `c1_git_history.png`, `c1_github_branches.png`.

### 3. Jenkins

Jenkins runs in Docker (`jenkins/jenkins:lts-jdk17`, port 8080). The job
`status-monitor-pipeline` is "Pipeline script from SCM" on `*/main`, Script Path `Jenkinsfile`,
Poll SCM `* * * * *`. Stages: Checkout, Test, Build, Archive.

Push a commit and wait about a minute: Jenkins starts a build by itself ("Started by an SCM change").

Screenshots: `c1_jenkins_config.png`, `c1_jenkins_console.png`, `c1_jenkins_auto_trigger.png`,
`c1_artifacts.png`, `c1_tests.png`, `c1_stage_view.png`, `c1_versioned_artifact.png`,
`c1_test_failure.png` (a failing test stops the pipeline at Test; Build and Archive do not run).

## Component 2: Ansible, Docker, Kubernetes

### 4. Ansible

```bash
mvn clean package
cd ansible
ansible-playbook -i inventory.ini playbook.yml -K     # first run: handler restarts the service
ansible-playbook -i inventory.ini playbook.yml -K     # second run: nothing changed, handler skipped
systemctl status status-monitor --no-pager
curl -s localhost:8082/health
sudo systemctl stop status-monitor
```

Screenshots: `c2_ansible_run.png`, `c2_ansible_rerun.png`, `c2_ansible_service.png`.

### 5. Docker

```bash
docker build -t status-monitor:1.0 .
docker images status-monitor
docker run -d --name sm-test -p 8083:8080 status-monitor:1.0
docker ps --filter name=sm-test
curl -s localhost:8083/health
docker rm -f sm-test
```

Screenshots: `c2_docker_build.png`, `c2_docker_run.png`.

### 6. Minikube

```bash
minikube start --driver=docker --memory=4096 --cpus=2
minikube image load status-monitor:1.0
kubectl apply -f k8s/
kubectl rollout status deployment/status-monitor
kubectl get pods -l app=status-monitor -o wide
kubectl get svc status-monitor-service
kubectl get pods --show-labels
kubectl describe svc status-monitor-service | grep -E "Selector|Endpoints"
minikube service status-monitor-service --url      # keep running, use the printed URL
for i in 1 2 3 4 5 6; do curl -s <url>/health; echo; done
```

Screenshots: `c2_pods_3.png`, `c2_svc.png`, `c2_labels.png`, `c2_app_response.png`,
`c2_load_balancing.png`.

### 7. Scaling

```bash
kubectl scale deployment status-monitor --replicas=5
kubectl get pods -l app=status-monitor
kubectl scale deployment status-monitor --replicas=2
kubectl get pods -l app=status-monitor
```

Screenshots: `c2_scale_up.png`, `c2_scale_down.png`.

### 8. ConfigMap and probes

`SERVER_PORT` and `MONITOR_URLS` come from `k8s/configmap.yaml`. Pods read env variables only at
start, so a ConfigMap change needs a rollout restart.

```bash
kubectl apply -f k8s/configmap.yaml
kubectl rollout restart deployment/status-monitor
kubectl rollout status deployment/status-monitor
kubectl get configmap status-monitor-config -o yaml
kubectl exec deploy/status-monitor -- printenv MONITOR_URLS
kubectl describe pod -l app=status-monitor | grep -E "Liveness|Readiness" | head -2
```

Screenshots: `c2_configmap.png`, `c2_probes.png`.
