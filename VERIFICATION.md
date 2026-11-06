# Verification

- JDK: Eclipse Temurin 25.0.4.1.
- Maven: 3.9.11.
- Spring Boot: 4.1.1.
- Command: `mvn clean verify`.
- Result: BUILD SUCCESS.
- Tests: 4, failures: 0, errors: 0, skipped: 0.
- Application source compiled with `release 25`.
- Service tests exercise persistence through H2. HTTP tests start the embedded server and verify 201, 200, 400, 404, 409 and ProblemDetail content type.
- PostgreSQL migration and Docker Compose execution were not verified in this environment.

## Azure update

- Rebuilt with JDK 25: BUILD SUCCESS; all 4 integration tests passed.
- Azure YAML configuration parsed successfully.
- Added Azure PostgreSQL profile with full TLS verification and environment-based credentials.
- Live Azure connectivity, permissions, migrations and App Service deployment were not tested because no server endpoint or credentials were provided.

## OpenAPI and Application Insights update

- Compiled with JDK 25: BUILD SUCCESS.
- 5 integration tests passed, with zero failures/errors.
- HTTP test verified `/v3/api-docs`, API title, student paths and response schema, and Swagger UI assets.
- Application Insights JSON parsed successfully.
- Added SLF4J logs and managed/manual agent startup configuration.
- Agent installation, live ingestion and App Service settings were not tested against Azure.
