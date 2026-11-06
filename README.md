# UTVT School Management

Student REST API using a traditional layered architecture, Maven, JDK 25 and Spring Boot 4.1.1.

## Coordinates

- Group: `mx.edu.utvt`
- Artifact: `school-management`
- Root package: `mx.edu.utvt.schoolmanagement`
- Packaging: executable JAR

## Architecture

`StudentController -> StudentService -> StudentRepository -> PostgreSQL`

Packages under the root package:

| Package | Responsibility |
| --- | --- |
| controller | HTTP endpoints and request validation |
| service | Business rules and transactions |
| repository | Spring Data JPA database access |
| entity | JPA persistence entities |
| dto.request | Validated input records |
| dto.response | API response records and pagination |
| mapper | Explicit DTO/entity mapping |
| exception | Business exceptions and centralized HTTP errors |

The service is a concrete class: no redundant interface is required for a single implementation. Database migrations are in `src/main/resources/db/migration`. Mapping is manual, with no Lombok or annotation processor dependency.

## Prerequisites

JDK 25, Maven 3.9+, and PostgreSQL (or Docker Compose for the local database). Check `java -version` and `mvn -version`; both must use JDK 25.

## Run on Windows PowerShell

```powershell
$env:DB_PASSWORD = 'ChooseYourLocalPassword'
docker compose up -d
mvn clean verify
mvn spring-boot:run
```

Keep the same terminal environment for the database and application. Docker Compose runs only PostgreSQL. Alternatively create the `school_management` database and user yourself, then set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for that server. The default database host is localhost.

## Run on Linux/macOS

```bash
export DB_PASSWORD='ChooseYourLocalPassword'
docker compose up -d
mvn clean verify
mvn spring-boot:run
```

Flyway creates the schema at startup; Hibernate validates it. Do not edit a migration already applied; add `V2__...sql` for future changes.

## Endpoints

| Method | URL | Result |
| --- | --- | --- |
| POST | /api/v1/students | Create (201 and Location header) |
| GET | /api/v1/students?page=0&size=20 | Paginated list (200) |
| GET | /api/v1/students?active=true&studentNumber=UTVT-001 | Exact optional filters |
| GET | /api/v1/students/{id} | Read (200) |
| PUT | /api/v1/students/{id} | Replace editable details (200) |
| PATCH | /api/v1/students/{id}/status | Change active status (200) |

Pagination is zero-based; size is 1 to 100. Results are sorted by id. PUT includes all editable details; nullable fields may be cleared. Student numbers are case-sensitive and allow letters, digits and hyphens. Both service checks and a database unique constraint protect student numbers. No uniqueness rule is imposed on email.

## Sample requests

`POST http://localhost:8080/api/v1/students` with `Content-Type: application/json`:

```json
{
  "studentNumber": "UTVT-001",
  "firstName": "Ana",
  "paternalSurname": "Lopez",
  "maternalSurname": "Garcia",
  "email": "ana@utvtol.edu.mx",
  "dateOfBirth": "2005-02-15"
}
```

`PATCH http://localhost:8080/api/v1/students/1/status`:

```json
{"active": false}
```

Errors use Spring ProblemDetail: 400 for invalid input, 404 for missing students, and 409 for duplicates, constraint conflicts or concurrent updates. Entities are never returned directly.

## Tests and scope

`mvn clean verify` runs service and HTTP integration tests with H2 in PostgreSQL compatibility mode. They cover create/read/update/status, filters, duplicate numbers and missing students. H2 tests do not validate PostgreSQL migrations; start the real PostgreSQL database to verify migrations and deployment.

This initial module implements students. Courses, enrollments, teachers and academic terms can be added to the same layers. Authentication, authorization and deployment pipelines are not implemented in this starter; add access control before exposing real student data.

## Azure

Use the `azure` profile to connect to Azure Database for PostgreSQL Flexible Server with full TLS verification. See [AZURE.md](AZURE.md) for local PowerShell execution, App Service settings, deployment, networking and troubleshooting.

## OpenAPI and monitoring

Swagger UI is served at `/swagger-ui.html`; JSON at `/v3/api-docs`. Application Insights captures SLF4J/Logback logs when its Java agent is enabled. See [OBSERVABILITY.md](OBSERVABILITY.md) for App Service and local startup instructions, environment settings and KQL queries.
