# API de Inmuebles

Este es el backend para la aplicación de Inmuebles, desarrollado con Spring Boot, Spring Security y Flyway.

## Configuración inicial para desarrollo

Sigue estos pasos **una sola vez** para configurar tu entorno de desarrollo local.

### Prerrequisitos

* Git
* Java JDK 23
* Maven
* Docker Desktop (corriendo)

---

### 1. Clonar el Repositorio

git clone https://github.com/AxelLuna04/InmueblesApi.git
cd InmueblesApi

### 2. Iniciar el Servidor de Base de Datos

Esto levantará un contenedor de SQL Server Express en Docker.

docker-compose up -d

### 3. Crear la Base de Datos

Crea la base de datos Inmuebles (aún vacía) usando el usuario SA del contenedor.

docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "inmueblesApi!" -C -Q "CREATE DATABASE Inmuebles;"

### 4. Crear el Usuario de la App

Crea el usuario app_inmuebles que usa la API para conectarse y le da permisos de dueño sobre la BD.

docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "inmueblesApi!" -C -Q "CREATE LOGIN app_inmuebles WITH PASSWORD = 'InmueblesApi123'; USE Inmuebles; CREATE USER app_inmuebles FOR LOGIN app_inmuebles; ALTER ROLE db_owner ADD MEMBER app_inmuebles;"

### 5. Arrancar la API

Ya puedes arrancar la aplicación de Spring Boot. Flyway se encargará de crear todas las tablas automáticamente (V1, V2, V3, etc.).

### 6. Insertar datos

Una vez creadas las tablas, es necesario ejecutar este comando para insertar los datos: docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "inmueblesApi!" -C -d Inmuebles < Data.sql