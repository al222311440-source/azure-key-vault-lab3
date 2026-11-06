# Azure connection and deployment

This configuration targets **Azure Database for PostgreSQL Flexible Server** with database-password authentication. It works from a local computer or Azure App Service. Azure SQL Database requires a different driver and configuration.

No Azure SDK is needed for JDBC. Azure CLI login does not authenticate the application's database connection. This update does not create cloud resources or deploy the application.

## 1. Prepare the database

Create the `school_management` database in your existing Flexible Server if it does not exist. Use a dedicated database login instead of the server administrator for normal operation. For this starter, that login must be able to create/modify the application tables because Flyway runs at startup. Production deployments can separate migration and runtime logins.

Copy the exact hostname from the Azure portal, for example `your-server.postgres.database.azure.com`. Use the Flexible Server login name; do not append `@server` as required by the retired Single Server service.

## 2. Run locally with PowerShell

```powershell
$env:SPRING_PROFILES_ACTIVE = 'azure'
$env:DB_HOST = 'your-server.postgres.database.azure.com'
$env:DB_NAME = 'school_management'
$env:DB_USERNAME = 'your-database-login'
$credential = Get-Credential -UserName $env:DB_USERNAME -Message 'Enter the PostgreSQL database password'
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
mvn spring-boot:run
```

The password is collected interactively rather than stored in source or command history. Maven and Java must use JDK 25. Opening a new terminal does not retain these variables. To return to the local Docker database, remove `SPRING_PROFILES_ACTIVE` and set the local database credentials.

## 3. Configure network access

- **Public database access:** allow your computer's public outbound IP in the PostgreSQL firewall for local runs. For App Service, allow its appropriate outbound IP addresses. The JDBC hostname still uses TLS verification.
- **Private database access:** App Service requires outbound VNet integration to a network that can reach the database. Link the appropriate PostgreSQL private DNS zone to that VNet and allow TCP 5432 through the relevant rules. A private endpoint on the web app controls inbound access and does not replace outbound VNet integration.
- A local computer needs VPN connectivity, a reachable VM inside the network or another approved private route to reach a private database. Azure login alone does not provide network access.
- DNS must resolve the actual database hostname. Use `Resolve-DnsName $env:DB_HOST` and `Test-NetConnection $env:DB_HOST -Port 5432` in PowerShell. A successful TCP test does not validate the password or TLS certificate.

## 4. Configure an existing Azure App Service

Choose **Java SE / Java 25** for this executable Spring Boot JAR. Use the portal to confirm the runtime available for your app and operating system. This project contains an embedded servlet container.

Under the web app's environment variables / application settings, add:

| Name | Value |
| --- | --- |
| SPRING_PROFILES_ACTIVE | azure |
| DB_HOST | Actual Flexible Server FQDN |
| DB_NAME | school_management |
| DB_USERNAME | Database login |
| DB_PASSWORD | Database password |
| DB_PORT | 5432 (optional) |
| DB_POOL_MAX_SIZE | 5 (optional, per app instance) |
| SERVER_PORT | Set only if your hosting configuration requires it |

Do not use App Service's generic connection-string field for this setup: the code expects the explicit environment variables above. Saving settings restarts the application. Keep App Service HTTPS Only enabled. Restrict who can read/change application settings; Key Vault references can be introduced later.

Build and deploy to an **existing** app after configuring its runtime, database variables and networking:

```powershell
mvn clean verify
az login
$resourceGroup = 'your-resource-group'
$webAppName = 'your-existing-web-app'
az webapp deploy --resource-group $resourceGroup --name $webAppName --src-path target/school-management-0.0.1-SNAPSHOT.jar --type jar
```

The last command deploys externally; run it only when you intend to update the specified app. It is included as an instruction and was not executed while generating this project. Do not copy local test settings to Azure.

## 5. TLS and certificate validation

The Azure profile sets `sslmode=verify-full` and `sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory`. Traffic is encrypted and the hostname and certificate chain are validated against the JVM truststore. Keep your JDK updated to receive current trusted root certificates. If Azure's certificate chain is absent, use the Microsoft guidance below to import the required roots into a dedicated Java truststore and configure `javax.net.ssl.trustStore` at JVM startup. Do not disable certificate verification to fix a truststore error.

## 6. Diagnose connection issues

| Symptom | Check |
| --- | --- |
| Missing DB_HOST / DB_USERNAME / DB_PASSWORD placeholder | Required Azure environment variables |
| Unknown host | FQDN and public/private DNS |
| Connection timeout | Firewall, VNet route, NSG and TCP 5432 |
| Password authentication failed | Database login, password and authentication mode |
| Database does not exist | Create DB_NAME before startup |
| PKIX or SSL handshake failure | JVM trusted CA certificates and hostname |
| Flyway permission denied | Migration login's database/schema privileges |

Student endpoints remain `/api/v1/students`. This starter does not implement user authentication/authorization: add it before making real student records publicly accessible. The `azure` profile is independent of DEV/QA/PRD; use separate environment values and databases for each environment.

## References

- https://learn.microsoft.com/en-us/azure/postgresql/connectivity/connect-java
- https://learn.microsoft.com/en-us/azure/postgresql/security/security-tls-how-to-connect
- https://learn.microsoft.com/en-us/azure/postgresql/security/security-update-trusted-root-java
- https://learn.microsoft.com/en-us/azure/app-service/language-support-policy
- https://learn.microsoft.com/en-us/azure/app-service/configure-language-java-deploy-run

## Application Insights

See [OBSERVABILITY.md](OBSERVABILITY.md) to enable the App Service Java agent and configure logs.
