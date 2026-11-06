# Run from the project root after mvn clean verify.
param([string]$AgentPath, [string]$JarPath)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $AgentPath) { $AgentPath = Join-Path $projectRoot 'monitoring/applicationinsights-agent.jar' }
if (-not $JarPath) { $JarPath = Join-Path $projectRoot 'target/school-management-0.0.1-SNAPSHOT.jar' }
if (-not $env:APPLICATIONINSIGHTS_CONNECTION_STRING) { throw 'Set APPLICATIONINSIGHTS_CONNECTION_STRING before starting.' }
if (-not (Test-Path $AgentPath)) { throw 'Download the Application Insights Java agent first; see OBSERVABILITY.md.' }
if (-not (Test-Path $JarPath)) { throw 'Build the application first with mvn clean verify.' }
$env:APPLICATIONINSIGHTS_CONFIGURATION_FILE = Join-Path $projectRoot 'monitoring/applicationinsights.json'
& java "-javaagent:$AgentPath" -jar $JarPath
exit $LASTEXITCODE
