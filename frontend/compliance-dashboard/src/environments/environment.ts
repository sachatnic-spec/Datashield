export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8000/api/v1',
  services: {
    auth: 'http://localhost:8001/api/v1',
    consent: 'http://localhost:8002/api/v1',
    rights: 'http://localhost:8003/api/v1',
    breach: 'http://localhost:8004/api/v1',
    notification: 'http://localhost:8005/api/v1',
    audit: 'http://localhost:8006/api/v1',
    tenant: 'http://localhost:8007/api/v1',
    workflow: 'http://localhost:8008/api/v1',
    policy: 'http://localhost:8009/api/v1',
    vendor: 'http://localhost:8010/api/v1',
    retention: 'http://localhost:8011/api/v1',
    grievance: 'http://localhost:8012/api/v1',
    analytics: 'http://localhost:8013/api/v1',
    report: 'http://localhost:8014/api/v1',
    discovery: 'http://localhost:8015/api/v1',
    classification: 'http://localhost:8016/api/v1',
    lineage: 'http://localhost:8017/api/v1'
  },
  wsUrl: 'ws://localhost:8000/ws',
  refreshInterval: 30000 // 30 seconds
};
