// Jenkins Job DSL - Generate per-service pipeline jobs
// Use Jenkins Job DSL plugin to auto-create jobs for each service

def gitRepo = 'https://github.com/sachatnic-spec/Datashield.git'
def gitBranch = '*/main'
def servicesDir = 'services'

// Detect all services with Jenkinsfiles
def services = [
  'auth-service',
  'ai-analysis-service',
  'analytics-service',
  'anomaly-detection-service',
  'audit-service',
  'breach-service',
  'config-service',
  'connector-service',
  'consent-service',
  'data-classification-service',
  'data-discovery-service',
  'data-lineage-service',
  'dpbi-service',
  'grievance-service',
  'notification-service',
  'pii-detection-service',
  'policy-service',
  'report-service',
  'retention-service',
  'rights-service',
  'risk-scoring-service',
  'search-service',
  'siem-service',
  'tenant-service',
  'vendor-service',
  'webhook-service',
  'workflow-service'
]

services.each { service ->
  pipelineJob("DataShield-${service}") {
    description("DataShield - Build & Deploy ${service}")
    
    triggers {
      // Poll Git every 5 minutes
      pollSCM('H/5 * * * *')
    }
    
    properties {
      pipelineTriggersJobProperty {
        triggers {
          // Optional: GitHub hook trigger
          githubPush()
        }
      }
    }
    
    definition {
      cpsScm {
        scm {
          git {
            remote {
              url(gitRepo)
              credentials('sachatnic-spec-jenkins')
            }
            branches {
              branch(gitBranch)
            }
          }
        }
        scriptPath("services/${service}/Jenkinsfile")
      }
    }
  }
}

// Optional: Folder for organizing jobs
folder('DataShield') {
  description('DataShield Microservices CI/CD')
}
