# 🏠 Inmuebles a tu Alcance — API

API REST para una plataforma de compra, venta y renta de inmuebles. Los vendedores publican sus propiedades, los clientes las buscan, agendan visitas y contactan al vendedor, y un administrador modera las publicaciones antes de que aparezcan en el catálogo.

Este repositorio es el **backend**. El cliente web está en [inmueblesFrontend](https://github.com/AxelLuna04/inmueblesFrontend).

## ✨ Funcionalidades

- **Registro e inicio de sesión** con JWT (token de acceso + token de renovación) y contraseñas cifradas con BCrypt.
- **Verificación de correo** y cambio de correo con enlace enviado por email.
- **Tres roles:** cliente, vendedor (lister) y administrador.
- **Publicaciones de inmuebles** con fotos, tipo de inmueble, características y tipo de operación (venta o renta).
- **Moderación:** el administrador aprueba o rechaza publicaciones e indica el motivo.
- **Agenda de visitas:** el vendedor define sus horarios y el cliente agenda citas en las horas disponibles.
- **Pago de acceso** para desbloquear el contacto del vendedor.
- **Historial** de cada inmueble y registro de ventas.
- **Feed "Para ti"** con publicaciones recomendadas.

## 🧰 Tecnologías

| Área | Herramientas |
|---|---|
| Lenguaje y framework | Java 23, Spring Boot 3.3 |
| Seguridad | Spring Security, JWT, BCrypt |
| Base de datos | SQL Server (en Docker), Spring Data JPA |
| Migraciones | Flyway (`src/main/resources/db/migration`) |
| Correo | Spring Mail con Gmail SMTP |
| Pruebas | JUnit 5 y Mockito (21 clases, más de 100 pruebas de servicios) |

## 🗂️ Estructura

```
src/main/java/com/inmapi
├── auth/         # Registro, login y verificación de correo
├── config/       # Seguridad, CORS y manejo de errores
├── controller/   # Endpoints REST
├── dto/          # Objetos de entrada y salida
├── modelo/       # Entidades JPA
├── repository/   # Acceso a datos
├── security/     # Filtro y servicio de JWT
├── service/      # Lógica de negocio
└── spec/         # Filtros de búsqueda dinámicos
```

## ⚙️ Cómo correrlo en local

### Prerrequisitos

- Java JDK 23
- Maven
- Docker Desktop (corriendo)

### 1. Clonar el repositorio

```bash
git clone https://github.com/AxelLuna04/InmueblesApi.git
cd InmueblesApi
```

### 2. Configurar las variables de entorno

Las contraseñas y claves **no están en el código**. Copia el archivo de ejemplo y llena los valores:

```bash
cp .env.example .env
```

| Variable | Para qué sirve |
|---|---|
| `DB_SA_PASSWORD` | Contraseña del usuario `SA` del contenedor de SQL Server |
| `DB_PASSWORD` | Contraseña del usuario `app_inmuebles` que usa la API |
| `JWT_SECRET` | Clave para firmar los tokens (genera una con `openssl rand -hex 32`) |
| `MAIL_PASSWORD` | [Contraseña de aplicación de Gmail](https://myaccount.google.com/apppasswords) para enviar correos |

El archivo `.env` está en `.gitignore`, así que nunca se sube a GitHub.

### 3. Levantar SQL Server

```bash
docker-compose up -d
```

Docker Compose lee `DB_SA_PASSWORD` del archivo `.env`.

### 4. Crear la base de datos y el usuario de la API

Reemplaza `TU_SA_PASSWORD` y `TU_DB_PASSWORD` por los valores que pusiste en `.env`:

```bash
docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "TU_SA_PASSWORD" -C -Q "CREATE DATABASE Inmuebles;"

docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "TU_SA_PASSWORD" -C -Q "CREATE LOGIN app_inmuebles WITH PASSWORD = 'TU_DB_PASSWORD'; USE Inmuebles; CREATE USER app_inmuebles FOR LOGIN app_inmuebles; ALTER ROLE db_owner ADD MEMBER app_inmuebles;"
```

### 5. Arrancar la API

```bash
mvn spring-boot:run
```

Flyway crea todas las tablas automáticamente. La API queda en `http://localhost:8080` (puedes probar `GET /api/health`).

### 6. Insertar los datos iniciales

Con las tablas ya creadas, carga los catálogos (tipos de inmueble, características, etc.):

```bash
docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "TU_SA_PASSWORD" -C -d Inmuebles < Data.sql
```

## 🧪 Pruebas

```bash
mvn test
```

## 🔌 Endpoints principales

| Ruta base | Descripción |
|---|---|
| `/api/v1/auth` | Registro, login, renovación de token y verificación de correo |
| `/api/v1/me` | Perfil del usuario, foto, cambio de correo y contraseña |
| `/api/v1/publicaciones` | Catálogo, detalle, agenda y contacto del vendedor |
| `/api/v1/mis-publicaciones` | Publicaciones del vendedor: crear, editar, marcar como vendida |
| `/api/v1/agenda` | Citas del usuario |
| `/api/v1/admin/publicaciones` | Moderación (solo administradores) |
| `/api/v1/tipos-inmueble`, `/api/v1/tipos-pago` | Catálogos |

<!--
## 👤 Mi participación
Este fue un proyecto en equipo. Describe aquí qué partes desarrollaste tú,
por ejemplo: "Desarrollé la autenticación con JWT y el módulo de agenda de citas."
-->
