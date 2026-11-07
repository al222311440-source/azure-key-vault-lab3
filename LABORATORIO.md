# Laboratorio I de Azure Key Vault

**Alumno** Felipe de Jesús Serrano Hurtado

**Práctica** Integración de Azure App Service, Key Vault y PostgreSQL

El código de la API se conserva del [repositorio de Jorge Almeida Montiel](https://github.com/jorgealmeidamontiel/school-management-api) en el commit `8f827ae928e3686e9ed4f75405b4483a9977885b`. El procedimiento de referencia está en los [videos de Lab 3](https://github.com/jorgealmeidamontiel/azure-laboratories/tree/main/Lab%203).

## Arquitectura

- App Service Linux B1 ejecuta Spring Boot con Java 25 y HTTPS obligatorio.
- La identidad asignada por el sistema tiene el rol `Key Vault Secrets User` en la bóveda.
- El App Service resuelve la referencia de `DB_PASSWORD` al secreto `postgres-password`.
- El tráfico de App Service a PostgreSQL usa integración con red virtual, Private Link y DNS privado. La base `school_management` usa el puerto 5432 y el perfil `azure` valida el certificado con `sslmode=verify-full`.

## Compilar y publicar el JAR

Con JDK 25 y Maven, desde la raíz del repositorio:

```bash
mvn clean verify
```

Para una instancia de App Service ya configurada, inicia sesión en Azure CLI, verifica la suscripción con `az account show` y asigna `APP_RESOURCE_GROUP` y `APP_NAME` a los recursos de tu laboratorio. Después ejecuta:

```bash
az webapp deploy --resource-group "$APP_RESOURCE_GROUP" --name "$APP_NAME" \
  --src-path target/school-management-0.0.1-SNAPSHOT.jar --type jar
```

| Variable | Valor o procedencia |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `DB_HOST` | FQDN de PostgreSQL Flexible Server |
| `DB_PORT` | `5432` |
| `DB_NAME` | `school_management` |
| `DB_USERNAME` | Usuario de PostgreSQL de la práctica |
| `DB_PASSWORD` | `@Microsoft.KeyVault(VaultName=<boveda>;SecretName=postgres-password)` |

## Validación de la entrega

El 7 de noviembre de 2026 se completaron 16 pruebas Maven. Azure mostró la referencia de Key Vault como `Resolved`. PgAdmin confirmó TLS 1.3 sobre la red privada. El `POST /api/v1/students` creó el registro ficticio `LAB3-DEMO` con HTTP 201 y el `GET /api/v1/students/1` lo recuperó con HTTP 200.

[Swagger del despliegue de la práctica](https://app-utvt-lab3-api-11440.azurewebsites.net/swagger-ui/index.html#/). La disponibilidad depende de la vigencia de la suscripción de Azure.
