# DataShield India - Deployment Guide

**Version:** 1.0.0  
**Last Updated:** 2026-06-24  
**Environment:** Production  

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Infrastructure Setup](#infrastructure-setup)
3. [Backend Deployment](#backend-deployment)
4. [Frontend Deployment](#frontend-deployment)
5. [Database Setup](#database-setup)
6. [Monitoring & Logging](#monitoring--logging)
7. [Security Checklist](#security-checklist)
8. [Rollback Procedures](#rollback-procedures)

---

## Prerequisites

### Required Software

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21+ | Backend services |
| Node.js | 20+ | Frontend apps |
| PostgreSQL | 16+ | Primary database |
| Redis | 7+ | Caching |
| Kafka | 3+ | Message broker |
| Elasticsearch | 8+ | Search service |
| Docker | 20.10+ | Containerization |
| Kubernetes | 1.24+ | Orchestration |

### Cloud Resources (AWS Example)

- **Compute**: EC2 instances or EKS cluster
- **Database**: RDS PostgreSQL (Multi-AZ)
- **Cache**: ElastiCache Redis
- **Message Queue**: MSK (Managed Kafka)
- **Storage**: S3 for static files and archives
- **CDN**: CloudFront for frontend apps
- **DNS**: Route 53
- **Secrets**: AWS Secrets Manager / HashiCorp Vault
- **Monitoring**: CloudWatch + Prometheus + Grafana

---

## Infrastructure Setup

### 1. AWS Infrastructure (Terraform)

```hcl
# infra/terraform/main.tf
provider "aws" {
  region = "ap-south-1"  # Mumbai region (India data residency)
}

# VPC Setup
resource "aws_vpc" "datasheild" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
  
  tags = {
    Name = "datasheild-vpc"
    Environment = "production"
  }
}

# RDS PostgreSQL
resource "aws_db_instance" "datasheild_db" {
  identifier             = "datasheild-postgres"
  engine                = "postgres"
  engine_version        = "16.1"
  instance_class        = "db.r6g.xlarge"
  allocated_storage     = 100
  storage_encrypted     = true
  multi_az              = true
  publicly_accessible   = false
  
  username = var.db_username
  password = var.db_password
  
  backup_retention_period = 30
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  tags = {
    Name = "datasheild-db"
    Compliance = "DPDP-2023"
  }
}

# ElastiCache Redis
resource "aws_elasticache_cluster" "datasheild_redis" {
  cluster_id           = "datasheild-redis"
  engine              = "redis"
  engine_version      = "7.0"
  node_type           = "cache.r6g.large"
  num_cache_nodes     = 1
  parameter_group_name = "default.redis7"
  port                = 6379
  
  tags = {
    Name = "datasheild-redis"
  }
}

# EKS Cluster
resource "aws_eks_cluster" "datasheild" {
  name     = "datasheild-cluster"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.28"
  
  vpc_config {
    subnet_ids = aws_subnet.private[*].id
  }
}
```

### 2. Kubernetes Cluster Setup

```bash
# Create EKS cluster
eksctl create cluster \
  --name datasheild-cluster \
  --region ap-south-1 \
  --nodegroup-name datasheild-nodes \
  --node-type t3.xlarge \
  --nodes 3 \
  --nodes-min 3 \
  --nodes-max 10 \
  --managed

# Configure kubectl
aws eks update-kubeconfig --region ap-south-1 --name datasheild-cluster
```

---

## Backend Deployment

### Option 1: Docker Compose (Development/Staging)

```bash
# Build all services
cd services
mvn clean package -DskipTests

# Start all services
docker-compose -f docker-compose.services.yml up -d

# Check status
docker-compose ps
```

### Option 2: Kubernetes (Production)

#### Create Namespace

```bash
kubectl create namespace datasheild-prod
kubectl config set-context --current --namespace=datasheild-prod
```

#### Deploy Services

```bash
# Deploy config maps
kubectl apply -f infra/kubernetes/configmaps/

# Deploy secrets
kubectl apply -f infra/kubernetes/secrets/

# Deploy services (1-27)
kubectl apply -f infra/kubernetes/services/

# Deploy ingress
kubectl apply -f infra/kubernetes/ingress.yml
```

#### Example Service Deployment (Auth Service)

```yaml
# infra/kubernetes/services/auth-service.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: datasheild-prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: datasheild/auth-service:1.0.0
        ports:
        - containerPort: 8001
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8001
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8001
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: datasheild-prod
spec:
  selector:
    app: auth-service
  ports:
  - protocol: TCP
    port: 8001
    targetPort: 8001
  type: ClusterIP
```

### Health Check Verification

```bash
# Check all services
kubectl get pods -n datasheild-prod

# Check specific service
kubectl describe pod auth-service-xxxxx -n datasheild-prod

# View logs
kubectl logs -f auth-service-xxxxx -n datasheild-prod

# Port forward for testing
kubectl port-forward svc/auth-service 8001:8001 -n datasheild-prod
```

---

## Frontend Deployment

### Build Frontend Apps

```bash
# Compliance Dashboard
cd frontend/compliance-dashboard
npm install
npm run build
# Output: dist/compliance-dashboard/

# Data Principal Portal
cd ../data-principal-portal
npm install
npm run build
# Output: dist/data-principal-portal/

# Consent Widget
cd ../consent-widget
npm install
npm run build
# Output: dist/consent-widget.js
```

### Option 1: AWS S3 + CloudFront

```bash
# Upload to S3
aws s3 sync dist/compliance-dashboard/ s3://datasheild-dashboard-prod/ --delete
aws s3 sync dist/data-principal-portal/ s3://datasheild-portal-prod/ --delete
aws s3 cp dist/consent-widget.js s3://datasheild-cdn/widget/v1/consent-widget.min.js

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id E1234567890ABC \
  --paths "/*"
```

### Option 2: Nginx (Server Deployment)

```nginx
# /etc/nginx/sites-available/datasheild

# Compliance Dashboard
server {
    listen 443 ssl http2;
    server_name dashboard.datasheild.in;
    
    ssl_certificate /etc/letsencrypt/live/datasheild.in/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/datasheild.in/privkey.pem;
    
    root /var/www/datasheild/dashboard;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API proxy
    location /api/ {
        proxy_pass http://localhost:8001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}

# Data Principal Portal
server {
    listen 443 ssl http2;
    server_name portal.datasheild.in;
    
    ssl_certificate /etc/letsencrypt/live/datasheild.in/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/datasheild.in/privkey.pem;
    
    root /var/www/datasheild/portal;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## Database Setup

### Initial Schema Setup

```bash
# Connect to PostgreSQL
psql -h datasheild-postgres.xxxxx.ap-south-1.rds.amazonaws.com -U datasheild -d postgres

# Create schemas for all 27 services
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS consent;
CREATE SCHEMA IF NOT EXISTS rights;
CREATE SCHEMA IF NOT EXISTS breach;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS tenant;
CREATE SCHEMA IF NOT EXISTS workflow;
CREATE SCHEMA IF NOT EXISTS policy;
CREATE SCHEMA IF NOT EXISTS vendor;
CREATE SCHEMA IF NOT EXISTS retention;
CREATE SCHEMA IF NOT EXISTS grievance;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS report;
CREATE SCHEMA IF NOT EXISTS discovery;
CREATE SCHEMA IF NOT EXISTS classification;
CREATE SCHEMA IF NOT EXISTS lineage;
CREATE SCHEMA IF NOT EXISTS ai_analysis;
CREATE SCHEMA IF NOT EXISTS pii_detection;
CREATE SCHEMA IF NOT EXISTS risk_scoring;
CREATE SCHEMA IF NOT EXISTS anomaly_detection;
CREATE SCHEMA IF NOT EXISTS connector;
CREATE SCHEMA IF NOT EXISTS webhook;
CREATE SCHEMA IF NOT EXISTS siem;
CREATE SCHEMA IF NOT EXISTS dpbi;
CREATE SCHEMA IF NOT EXISTS config;
CREATE SCHEMA IF NOT EXISTS search;
```

### Backup Strategy

```bash
# Full backup
pg_dump -h <host> -U datasheild -d datasheild_prod -F c -f backup_$(date +%Y%m%d).dump

# Restore
pg_restore -h <host> -U datasheild -d datasheild_prod backup_20260624.dump

# Automated daily backups (cron)
0 2 * * * /usr/local/bin/backup-datasheild.sh
```

---

## Monitoring & Logging

### Prometheus Setup

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'datasheild-services'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names:
            - datasheild-prod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
```

### Grafana Dashboards

Import dashboards for:
- Service health metrics
- API latency (p50, p95, p99)
- Error rates
- Kafka lag
- Database connections
- Cache hit rates

### Log Aggregation (ELK Stack)

```yaml
# filebeat.yml
filebeat.inputs:
- type: container
  paths:
    - '/var/lib/docker/containers/*/*.log'
  processors:
    - add_kubernetes_metadata:
        host: ${NODE_NAME}
        matchers:
        - logs_path:
            logs_path: "/var/lib/docker/containers/"

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "datasheild-logs-%{+yyyy.MM.dd}"
```

---

## Security Checklist

### Pre-Deployment

- [ ] All secrets stored in AWS Secrets Manager / Vault
- [ ] TLS certificates configured (Let's Encrypt or AWS ACM)
- [ ] Database encryption at rest enabled
- [ ] Database encryption in transit (SSL) enabled
- [ ] Redis AUTH enabled
- [ ] Kafka SASL/SSL enabled
- [ ] API rate limiting configured
- [ ] CORS policies configured
- [ ] Security headers configured (Nginx/CloudFront)
- [ ] IAM roles configured (least privilege)
- [ ] Network security groups configured
- [ ] VPC peering configured
- [ ] WAF rules configured

### Post-Deployment

- [ ] Vulnerability scan (OWASP ZAP, Burp Suite)
- [ ] Penetration testing scheduled
- [ ] Security audit logs enabled
- [ ] Incident response plan documented
- [ ] Backup and recovery tested
- [ ] Disaster recovery drill completed

---

## Rollback Procedures

### Kubernetes Rollback

```bash
# View deployment history
kubectl rollout history deployment/auth-service -n datasheild-prod

# Rollback to previous version
kubectl rollout undo deployment/auth-service -n datasheild-prod

# Rollback to specific revision
kubectl rollout undo deployment/auth-service --to-revision=2 -n datasheild-prod

# Check rollout status
kubectl rollout status deployment/auth-service -n datasheild-prod
```

### Database Rollback

```bash
# Restore from backup
pg_restore -h <host> -U datasheild -d datasheild_prod backup_pre_deployment.dump

# Run rollback migrations (Flyway)
flyway -configFiles=flyway.conf undo
```

### Frontend Rollback

```bash
# S3 versioning enabled - restore previous version
aws s3api list-object-versions --bucket datasheild-dashboard-prod

# Restore specific version
aws s3api copy-object \
  --copy-source datasheild-dashboard-prod/index.html?versionId=<version-id> \
  --bucket datasheild-dashboard-prod \
  --key index.html
```

---

## Production Checklist

### Before Go-Live

- [ ] All 27 services deployed and healthy
- [ ] All 3 frontend apps accessible
- [ ] Database migrations applied
- [ ] Kafka topics created
- [ ] Elasticsearch indices created
- [ ] Redis connected
- [ ] SSL certificates valid
- [ ] DNS records configured
- [ ] Monitoring dashboards live
- [ ] Alert rules configured
- [ ] Log aggregation working
- [ ] Backup jobs scheduled
- [ ] Load testing completed
- [ ] Security scan passed
- [ ] Documentation complete
- [ ] Runbooks prepared
- [ ] On-call rotation setup

### Post Go-Live

- [ ] Monitor error rates (< 1%)
- [ ] Monitor latency (p95 < 200ms)
- [ ] Monitor resource usage (CPU, memory)
- [ ] Verify backup jobs running
- [ ] Verify monitoring alerts working
- [ ] Conduct post-mortem if issues arise

---

## Support & Escalation

- **L1 Support**: support@datasheild.in
- **L2 Engineering**: eng@datasheild.in
- **Emergency Hotline**: +91-1800-PRIVACY
- **Slack**: #datasheild-prod-alerts

---

**Document Version**: 1.0  
**Last Review**: 2026-06-24  
**Next Review**: 2026-09-24
