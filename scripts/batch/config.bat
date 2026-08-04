@echo off
REM ==========================================================
REM DataShield India - Build Configuration
REM ==========================================================

set ROOT=%~dp0..
set SERVICES=%ROOT%\services
set FRONTEND=%ROOT%\frontend

set BUILD_HOME=%ROOT%\build
set LOG_DIR=%BUILD_HOME%\logs
set REPORT_DIR=%BUILD_HOME%\reports
set ERROR_DIR=%LOG_DIR%\errors

set MVN=%ROOT%\tools\apache-maven-3.9.16\bin\mvn.cmd
if not exist "%MVN%" set MVN=mvn

set PYTHON=C:\Users\NIC\AppData\Local\Programs\Python\Python311\python.exe

set MAX_PARALLEL=4
set SKIP_TESTS=-DskipTests

REM ===== Shared Libraries =====
set SHARED_LIBS=common-lib event-schemas

REM ===== Java Services =====
set JAVA_SERVICES=auth-service consent-service rights-service breach-service dpbi-service notification-service audit-service tenant-service vendor-service policy-service workflow-service config-service connector-service search-service webhook-service siem-service analytics-service report-service retention-service grievance-service data-classification-service data-discovery-service data-lineage-service pii-detection-service ai-analysis-service anomaly-detection-service risk-scoring-service

REM ===== Python Services =====
set PYTHON_SERVICES=pii-detection ai-analysis anomaly-detection risk-scoring
