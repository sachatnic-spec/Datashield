import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const consentApiDuration = new Trend('consent_api_duration');
const rightsApiDuration = new Trend('rights_api_duration');
const analyticsApiDuration = new Trend('analytics_api_duration');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp up to 10 users
    { duration: '1m', target: 50 },    // Ramp up to 50 users
    { duration: '2m', target: 100 },   // Stay at 100 users
    { duration: '1m', target: 200 },   // Peak at 200 users
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    'http_req_duration': ['p(95)<200', 'p(99)<500'],  // 95% under 200ms, 99% under 500ms
    'http_req_failed': ['rate<0.01'],                  // Error rate < 1%
    'errors': ['rate<0.1'],                            // Custom error rate < 10%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8002';
const AUTH_URL = __ENV.AUTH_URL || 'http://localhost:8001';

// Test data
const tenants = ['tenant-001', 'tenant-002', 'tenant-003'];
let authToken = '';

export function setup() {
  // Login to get auth token
  const loginPayload = JSON.stringify({
    email: 'test@example.com',
    password: 'test123',
  });

  const loginRes = http.post(`${AUTH_URL}/api/v1/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (loginRes.status === 200) {
    const body = JSON.parse(loginRes.body);
    return { token: body.token };
  }

  return { token: 'mock-token-for-testing' };
}

export default function(data) {
  const tenantId = tenants[Math.floor(Math.random() * tenants.length)];
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${data.token}`,
    'X-Tenant-ID': tenantId,
  };

  // Test 1: Consent Service - Create consent
  {
    const payload = JSON.stringify({
      dataPrincipalId: `dp-${__VU}-${__ITER}`,
      purpose: 'MARKETING',
      consentText: 'I agree to receive marketing communications',
      source: 'WEB',
    });

    const res = http.post(`${BASE_URL}/api/v1/consents`, payload, { headers });
    
    consentApiDuration.add(res.timings.duration);
    
    const success = check(res, {
      'consent created': (r) => r.status === 201 || r.status === 200,
      'response time < 200ms': (r) => r.timings.duration < 200,
    });

    errorRate.add(!success);
  }

  sleep(1);

  // Test 2: Rights Service - Submit DSAR
  {
    const payload = JSON.stringify({
      dataPrincipalId: `dp-${__VU}-${__ITER}`,
      requestType: 'ACCESS',
      description: 'Request for data access',
    });

    const res = http.post('http://localhost:8003/api/v1/rights/requests', payload, { headers });
    
    rightsApiDuration.add(res.timings.duration);
    
    const success = check(res, {
      'DSAR submitted': (r) => r.status === 201 || r.status === 200,
      'response time < 300ms': (r) => r.timings.duration < 300,
    });

    errorRate.add(!success);
  }

  sleep(1);

  // Test 3: Analytics Service - Get compliance score
  {
    const res = http.get(
      `http://localhost:8013/api/v1/analytics/score/${tenantId}`,
      { headers }
    );
    
    analyticsApiDuration.add(res.timings.duration);
    
    const success = check(res, {
      'compliance score retrieved': (r) => r.status === 200,
      'response time < 500ms': (r) => r.timings.duration < 500,
    });

    errorRate.add(!success);
  }

  sleep(2);

  // Test 4: Consent Service - List consents
  {
    const res = http.get(`${BASE_URL}/api/v1/consents?page=0&size=20`, { headers });
    
    const success = check(res, {
      'consents listed': (r) => r.status === 200,
      'response time < 200ms': (r) => r.timings.duration < 200,
    });

    errorRate.add(!success);
  }

  sleep(1);
}

export function teardown(data) {
  console.log('Performance test completed');
}
