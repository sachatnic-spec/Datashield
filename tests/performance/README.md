# Performance Testing

## Overview

Performance tests for DataShield India platform using k6 for load testing.

## Prerequisites

Install k6:
```bash
# Windows (Chocolatey)
choco install k6

# macOS (Homebrew)
brew install k6

# Linux
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

## Running Tests

### Load Test

```bash
k6 run load-test.js
```

### Stress Test (High Load)

```bash
k6 run --vus 500 --duration 5m load-test.js
```

### Spike Test

```bash
k6 run --stage 10s:100,1m:1000,10s:100 load-test.js
```

### With Environment Variables

```bash
k6 run --env BASE_URL=http://prod-server:8002 load-test.js
```

## Performance Targets

| Metric | Target | Threshold |
|--------|--------|-----------|
| p95 latency | < 200ms | PASS if < 200ms |
| p99 latency | < 500ms | PASS if < 500ms |
| Error rate | < 1% | PASS if < 1% |
| Throughput | 100 TPS | Target |

## Test Scenarios

### 1. Consent Creation
- **Endpoint**: POST /api/v1/consents
- **Target**: < 200ms p95
- **Load**: 100 concurrent users

### 2. DSAR Submission
- **Endpoint**: POST /api/v1/rights/requests
- **Target**: < 300ms p95
- **Load**: 50 concurrent users

### 3. Analytics Query
- **Endpoint**: GET /api/v1/analytics/score/{tenantId}
- **Target**: < 500ms p95
- **Load**: 200 concurrent users

## Results

View results in:
- Console output (real-time)
- JSON report: `--out json=results.json`
- HTML report: Use k6-reporter

## CI/CD Integration

```yaml
# GitHub Actions example
- name: Run performance tests
  run: k6 run --out json=results.json tests/performance/load-test.js
  
- name: Upload results
  uses: actions/upload-artifact@v2
  with:
    name: k6-results
    path: results.json
```
