# ERP Contabilidad OSS API

API en Spring Boot para el modulo de contabilidad.

## Requisitos

- Java 21
- PostgreSQL
- Git (opcional, para clonar)

## Configuracion

Este proyecto usa variables de entorno por defecto y un perfil local con un archivo no versionado.

### Opcion A: Perfil local (rapido para desarrollo)

1) Copia el ejemplo y agrega tus secretos locales:

Git Bash:

```bash
cp src/main/resources/application-local.example.properties \
	src/main/resources/application-local.properties
```

CMD:

```bat
copy src\main\resources\application-local.example.properties ^
	  src\main\resources\application-local.properties
```

PowerShell:

```powershell
Copy-Item src\main\resources\application-local.example.properties \
  src\main\resources\application-local.properties
```

2) Edita `src/main/resources/application-local.properties` con tus credenciales.

3) Asegura que la base de datos exista y que PostgreSQL este ejecutandose.

Nota: `application-local.properties` esta ignorado por git.

### Opcion B: Perfil por defecto (variables de entorno)

Define las variables antes de ejecutar:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS` (opcional)

Estas llaves estan en `src/main/resources/application.properties`.

## Ejecutar el proyecto

Se recomienda usar el Maven Wrapper incluido en el repo.

### Git Bash

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### CMD

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### PowerShell

```powershell
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Para ejecutar con el perfil por defecto (variables de entorno), elimina `-Dspring-boot.run.profiles=local`.

## Notas

- El perfil `local` lee `application-local.properties`.
- El perfil por defecto lee `application.properties` y requiere variables de entorno.
