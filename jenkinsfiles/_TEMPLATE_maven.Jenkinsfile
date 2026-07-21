// ============================================================
// Jenkinsfile for {{SERVICE_NAME}}  (Spring Boot / Maven module)
// Auto-generated scaffold - safe to hand-edit per service.
// Uses forward slashes for all paths - Windows/cmd/PowerShell/Java
// all accept them, and it avoids Groovy string-escaping pitfalls.
// ============================================================
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        SERVICE_NAME   = '{{SERVICE_NAME}}'
        MODULE_PATH    = 'services/{{SERVICE_NAME}}'
        SERVICE_PORT   = '{{PORT}}'
        HEALTH_PATH    = '{{HEALTH_PATH}}'
        DEPLOY_ROOT    = 'D:/Development Practice/Datasheild/deploy'
        JAR_NAME       = "${SERVICE_NAME}.jar"
        SCRIPTS_DIR    = "${WORKSPACE}/jenkins-setup/scripts"
        TARGET_JAR     = "${DEPLOY_ROOT}/${SERVICE_NAME}/${JAR_NAME}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                bat "mvn -pl ${MODULE_PATH} -am -DskipTests=false clean package"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${MODULE_PATH}/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Backup Current Artifact') {
            steps {
                powershell "& '${SCRIPTS_DIR}/Backup-Current.ps1' -ServiceName '${SERVICE_NAME}' -CurrentArtifactPath '${TARGET_JAR}' -DeployRoot '${DEPLOY_ROOT}'"
            }
        }

        stage('Stop Running Service') {
            steps {
                powershell "& '${SCRIPTS_DIR}/Stop-Service.ps1' -ServiceName '${SERVICE_NAME}' -Port ${SERVICE_PORT}"
            }
        }

        stage('Deploy New JAR') {
            steps {
                powershell "New-Item -ItemType Directory -Force -Path '${DEPLOY_ROOT}/${SERVICE_NAME}' | Out-Null"
                bat "copy /Y \"${MODULE_PATH}/target/*.jar\" \"${TARGET_JAR}\""
            }
        }

        stage('Start Service') {
            steps {
                powershell "& '${SCRIPTS_DIR}/Start-JavaService.ps1' -ServiceName '${SERVICE_NAME}' -JarPath '${TARGET_JAR}' -Port ${SERVICE_PORT} -DeployRoot '${DEPLOY_ROOT}'"
            }
        }

        stage('Health Check') {
            steps {
                powershell "& '${SCRIPTS_DIR}/Test-Health.ps1' -ServiceName '${SERVICE_NAME}' -Port ${SERVICE_PORT} -HealthPath '${HEALTH_PATH}'"
            }
        }
    }

    post {
        failure {
            echo "==> Build/deploy failed for ${SERVICE_NAME}. Rolling back to last known-good artifact."
            powershell "& '${SCRIPTS_DIR}/Rollback.ps1' -ServiceName '${SERVICE_NAME}' -TargetArtifactPath '${TARGET_JAR}' -DeployRoot '${DEPLOY_ROOT}'"
        }
        always {
            archiveArtifacts artifacts: "${MODULE_PATH}/target/*.jar", allowEmptyArchive: true
            archiveArtifacts artifacts: "${DEPLOY_ROOT}/${SERVICE_NAME}/logs/*.log", allowEmptyArchive: true
        }
        success {
            echo "==> ${SERVICE_NAME} deployed and healthy on port ${SERVICE_PORT}"
        }
    }
}
