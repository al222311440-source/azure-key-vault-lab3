# OpenAPI and Application Insights

## OpenAPI

The project uses `springdoc-openapi-starter-webmvc-ui` 3.1.1 for Spring Boot 4.1.1. `OpenApiConfig` defines the title, version, UTVT contact and relative server URL. The controller documents operations and HTTP response codes. DTO schemas and validation constraints are discovered automatically.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

On Azure, replace localhost with your web app's HTTPS hostname. Use `OPENAPI_ENABLED=false` to disable both the documentation endpoints and UI when desired. This switch does not provide API authentication.

## Application logging

The application uses SLF4J with Spring Boot's default Logback implementation. `StudentService` writes INFO logs for successful persistence operations; `GlobalExceptionHandler` writes WARN logs for expected business conflicts. Application logs include internal IDs and status, not names, emails, student numbers, passwords or complete request bodies. Logs are operational diagnostics, not an immutable transaction audit trail.

`APP_LOG_LEVEL` defaults to INFO. The agent independently captures INFO and higher. Both the application and agent logging thresholds must allow a message for it to reach Application Insights.

## Azure App Service: managed agent

For Java SE deployment, enable Application Insights on the web app and select your Application Insights resource. The App Service-managed Java agent automatically instruments Logback, requests, dependencies and exceptions.

Configure these application settings in addition to the database settings in AZURE.md:

| Name | Value |
| --- | --- |
| APPLICATIONINSIGHTS_CONNECTION_STRING | Full connection string from your Application Insights resource |
| ApplicationInsightsAgent_EXTENSION_VERSION | ~3 on Linux; ~2 on Windows |
| XDT_MicrosoftApplicationInsights_Java | 1 on Windows only |
| APPLICATIONINSIGHTS_CONFIGURATION_CONTENT | JSON shown below |
| APP_LOG_LEVEL | INFO |

Inline agent configuration:

```json
{"role":{"name":"utvt-school-management"},"sampling":{"percentage":100},"instrumentation":{"logging":{"level":"INFO"}}}
```

The same JSON is in `monitoring/applicationinsights.json` for local/manual-agent runs. The connection string is deliberately excluded from this file. Set it using `APPLICATIONINSIGHTS_CONNECTION_STRING`. Restart the app after saving settings. The 100% sampling setting is useful for this low-volume lab; reduce it or use adaptive sampling as traffic grows to control ingestion volume. Use a distinct role name or Application Insights resource for each environment if needed.

Only one Java agent should instrument the application. With App Service's managed agent enabled, use normal JAR deployment and do not also add a manual `-javaagent`. Setting a connection string alone does not load an agent.

## Local or VM run: explicit Java agent

1. Download the documented Java agent version from Microsoft's release page:

```powershell
Invoke-WebRequest -Uri 'https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.9/applicationinsights-agent-3.7.9.jar' -OutFile 'monitoring/applicationinsights-agent.jar'
```

The agent binary is not included in the source ZIP. You can substitute a newer supported release from the official page. Build the application and set the database variables as explained in README.md or AZURE.md. Do not enable the App Service managed agent on the same process.

2. Configure the full connection string, then run the provided script:

```powershell
$env:APPLICATIONINSIGHTS_CONNECTION_STRING = '<copy-the-full-connection-string-from-Azure>'
mvn clean verify
.\scripts\start-with-insights.ps1
```

The script loads the Java agent before Spring Boot starts and points it to the configuration file. It checks that the agent, executable JAR and connection string exist. No telemetry is sent by the normal test suite.

Linux/macOS equivalent, after downloading the same agent:

```bash
export APPLICATIONINSIGHTS_CONNECTION_STRING='<full-connection-string>'
export APPLICATIONINSIGHTS_CONFIGURATION_FILE="$PWD/monitoring/applicationinsights.json"
java -javaagent:monitoring/applicationinsights-agent.jar -jar target/school-management-0.0.1-SNAPSHOT.jar
```

Outbound HTTPS access to Azure Monitor ingestion endpoints is required. A private PostgreSQL endpoint does not establish access to Azure Monitor.

## Verify telemetry

Start the instrumented application, open Swagger UI and create or update a student. Allow ingestion time and inspect Application Insights > Logs. Query logs from the Application Insights resource:

```kusto
traces
| where timestamp > ago(30m)
| where cloud_RoleName == "utvt-school-management"
| project timestamp, severityLevel, message, operation_Id
| order by timestamp desc
```

For HTTP requests:

```kusto
requests
| where timestamp > ago(30m)
| where cloud_RoleName == "utvt-school-management"
| project timestamp, name, resultCode, success, duration, operation_Id
| order by timestamp desc
```

For correlation, use the operation_Id returned by either query to inspect related telemetry. Querying from the linked Log Analytics workspace uses `AppTraces` and `AppRequests` with columns such as `TimeGenerated`, `AppRoleName` and `OperationId` instead. Exceptions are available in `exceptions` (resource scope) or `AppExceptions` (workspace scope).

If no logs arrive, verify the connection string, agent startup messages, application and agent logging levels, sampling, resource selection and outbound connectivity. `System.out.println` is not a substitute for SLF4J logging.

## Validation scope

Automated tests verify student behavior and serve the generated OpenAPI document and Swagger UI. Live telemetry ingestion cannot be verified without your Application Insights resource configuration. Enabling the managed agent, deploying to Azure and running the manual-agent script are instructions for you; they were not performed against your subscription.

## Official references

- https://springdoc.org/getting-started.html
- https://springdoc.org/faq.html
- https://learn.microsoft.com/en-us/azure/azure-monitor/app/codeless-app-service?tabs=java
- https://learn.microsoft.com/en-us/azure/azure-monitor/app/java-standalone-config
- https://learn.microsoft.com/en-us/azure/azure-monitor/app/opentelemetry-enable?tabs=java
- https://github.com/microsoft/ApplicationInsights-Java/releases
