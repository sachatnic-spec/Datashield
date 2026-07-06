export const environment = {
  production: false,
  services: {
    auth: 'http://localhost:8001/v1',
    consent: 'http://localhost:8002/v1',
    rights: 'http://localhost:8003/v1',
    breach: 'http://localhost:8004/v1',
    notification: 'http://localhost:8005/v1',
    audit: 'http://localhost:8006/v1',
    tenant: 'http://localhost:8007/v1',
    policy: 'http://localhost:8009/v1',
    vendor: 'http://localhost:8010/v1',
    retention: 'http://localhost:8011/v1',
    grievance: 'http://localhost:8012/v1',
    analytics: 'http://localhost:8013/v1',
    report: 'http://localhost:8014/v1',
    discovery: 'http://localhost:8015/api/v1',
    classification: 'http://localhost:8016/api/v1',
    lineage: 'http://localhost:8017/api/v1'
  },
  defaultTenantId: 'default',
  refreshInterval: 30000
};
