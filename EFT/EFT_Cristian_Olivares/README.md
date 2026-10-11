# EFT Semana 9 - Desarrollo Backend III

## Desarrollo Backend Avanzado: Spring Cloud, Batch y despliegue en AWS

Proyecto correspondiente a la **Evaluación Final Transversal (EFT) - Semana 9** de la asignatura **Desarrollo Backend III (PBY2203)**.

---

## 📌 Objetivo del proyecto

El objetivo de la EFT es migrar un sistema bancario legacy basado en COBOL y scripts Shell sobre mainframe hacia una arquitectura moderna de microservicios, desplegada en la nube (AWS). La solución integra:

- **Migración de procesos batch** a Spring Batch.
- **Patrón Backend for Frontend (BFF)** para tres canales (web, móvil, cajeros).
- **Microservicios resilientes y seguros** con Spring Cloud, OAuth2, Resilience4j, Kafka y Eureka.
- **Despliegue completo en AWS EC2** con Docker y Docker Compose.

---

## 🏗️ Arquitectura de la solución

El proyecto utiliza una arquitectura de microservicios compuesta por:

- **Config Server**: centraliza la configuración de los microservicios.
- **Eureka Server**: permite el registro y descubrimiento de servicios.
- **auth-server**: servidor de autorización OAuth 2.0 encargado de emitir tokens JWT.
- **bank-api**: administra transacciones y su flujo de eventos Kafka.
- **cuentas-api**: administra apertura, mantenimiento, consulta y cierre de cuentas bancarias.
- **clientes-api**: administra perfiles de clientes con validación de RUT/correo.
- **pagos-api**: registra transferencias idempotentes y publica eventos desde un transactional outbox hacia Kafka.
- **intereses-api**: microservicio encargado de las operaciones relacionadas con intereses.
- **batch-service**: ejecuta los jobs de reporte diario, cálculo mensual de intereses y estados de cuenta anuales con Spring Batch.
- **bff-web, bff-mobile, bff-atm**: adaptan las respuestas y permisos OAuth2 para cada canal.
- **Apache Kafka**: broker utilizado para la comunicación asíncrona basada en eventos.
- **H2**: base de datos en memoria utilizada por los microservicios.
- **Resilience4j**: proporciona mecanismos de tolerancia a fallos mediante Circuit Breaker y Retry.
- **Docker & Docker Compose**: contenerización y orquestación de todos los componentes.

---

## 🔐 Seguridad con OAuth 2.0 y JWT

La seguridad del sistema está basada en **OAuth 2.0** con tokens **JWT**, implementado mediante **Spring Security**.

### Componentes de seguridad

- **auth-server**: actúa como **Authorization Server**. Se encarga de autenticar a los clientes y emitir los tokens JWT.
- **bank-api, cuentas-api, clientes-api, pagos-api, intereses-api y batch-service**: actúan como **Resource Servers**. Validan los tokens JWT antes de permitir el acceso a sus endpoints.

### Configuración del cliente OAuth 2.0

    Client ID:     bank-client
    Client Secret: secret
    Grant Types:   client_credentials, authorization_code, refresh_token
    Scopes:        openid, profile, read, write, web.read, mobile.read, atm.read, atm.write

Los endpoints GET de las APIs requieren `SCOPE_read` y los endpoints POST requieren `SCOPE_write`. Los BFF usan scopes diferenciados: `web.read`, `mobile.read`, `atm.read` y `atm.write`. Así, por ejemplo, un token con `scope=mobile.read` puede usar el BFF móvil, pero no el BFF web ni las transferencias del cajero.

El `auth-server` configura explícitamente su issuer como `http://auth-server:9000` mediante el bean `AuthorizationServerSettings`. Sin este ajuste, el issuer sería `http://localhost:9000` y todos los Resource Servers rechazarían los tokens dentro de la red Docker.

### Flujo de autenticación

    Cliente (Postman)
           |
           | 1. Solicita token
           v
       auth-server (Puerto 9000)
           |
           | 2. Emite JWT
           v
       Cliente
           |
           | 3. Usa JWT para consumir API
           v
       Resource Servers
           |
           | 4. Valida JWT
           v
       200 OK / 401 Unauthorized / 403 Forbidden

---

## 🐳 Contenerización y despliegue en AWS EC2

Todos los componentes se encuentran contenerizados con **Docker** y orquestados con **Docker Compose**, permitiendo levantar toda la arquitectura con un solo comando. El despliegue se realizó en una instancia **AWS EC2 t3.large** con Ubuntu 24.04 LTS.

### Contenedores definidos

| Contenedor | Imagen | Puerto |
|---|---|---|
| kafka | apache/kafka:4.3.1 | 9092 |
| eureka-server | Dockerfile propio | 8761 |
| config-server | Dockerfile propio | 8888 |
| auth-server | Dockerfile propio | 9000 |
| bank-api | Dockerfile propio | 8081 |
| cuentas-api | Dockerfile propio | 8084 (host) → 8082 (contenedor) |
| intereses-api | Dockerfile propio | 8083 |
| batch-service | Dockerfile propio | 8085 |
| clientes-api | Dockerfile propio | 8086 |
| pagos-api | Dockerfile propio | 8087 |
| bff-web | Dockerfile propio | 8091 |
| bff-mobile | Dockerfile propio | 8092 |
| bff-atm | Dockerfile propio | 8093 |

### Red interna

Todos los contenedores se comunican a través de la red interna `banco-network`. Esto permite que los microservicios se encuentren entre sí usando sus nombres de servicio (por ejemplo `http://config-server:8888`) en lugar de `localhost`.

Eureka Server y Config Server incluyen healthchecks. Los demás servicios esperan a que esos servidores estén saludables antes de iniciar; `bank-api` y `pagos-api` también esperan a que Kafka haya iniciado.

### Despliegue en AWS EC2

La solución completa se despliega en una instancia **EC2 t3.large** ejecutando el mismo `docker-compose.yaml`. Los puertos se abren en el Security Group solo hacia la IP del desarrollador. El mapeo explícito del puerto 8085 en `batch-service` (`"8085:8085"`) es crítico: sin él, Docker asigna un puerto aleatorio y las peticiones externas fallan con `ECONNREFUSED`.

---

## ⚙️ Tecnologías utilizadas

- Java 17
- Spring Boot 3.x
- Spring Cloud
- Spring Cloud Config
- Netflix Eureka
- Spring Security
- Spring OAuth2 Authorization Server
- Spring OAuth2 Resource Server
- Spring Data JPA
- Spring Batch
- Apache Kafka
- Spring Kafka
- Resilience4j
- H2 Database
- Maven
- Docker
- Docker Compose
- AWS EC2
- Postman

---

## 📂 Estructura general

    EFT_Cristian_Olivares/
    |
    |-- auth-server/
    |-- bank-api/
    |-- cuentas-api/
    |-- clientes-api/
    |-- pagos-api/
    |-- intereses-api/
    |-- batch-service/
    |-- bff-web/
    |-- bff-mobile/
    |-- bff-atm/
    |-- config-server/
    |-- eureka-server/
    |-- docker-compose.yaml
    |-- despliegue.md
    |-- instrucciones.md
    |-- README.md

---

## 🔌 Puertos utilizados

| Componente | Puerto |
|---|---:|
| Eureka Server | 8761 |
| Config Server | 8888 |
| auth-server | 9000 |
| bank-api | 8081 |
| cuentas-api | 8084 (host) → 8082 (contenedor) |
| intereses-api | 8083 |
| batch-service | 8085 |
| clientes-api | 8086 |
| pagos-api | 8087 |
| bff-web | 8091 |
| bff-mobile | 8092 |
| bff-atm | 8093 |
| Apache Kafka | 9092 |

---

## 🚀 Ejecución del proyecto

### Requisitos previos

- Docker y Docker Compose v2.
- Postman para las pruebas.

### Levantar toda la arquitectura

Desde la carpeta raíz del proyecto:

```bash
docker compose up -d --build
docker compose ps
```

### Verificar el registro en Eureka

Abrir en el navegador (local: `http://localhost:8761`, EC2: `http://<IP_PUBLICA>:8761`).

Deberían aparecer los servicios backend y BFF en estado `UP`.

### Detener la arquitectura

```bash
docker compose down
```

---

## 🧪 Pruebas de la solución

Revisar `instrucciones.md` para el detalle completo. Los pasos principales son:

1. Obtener tokens JWT por canal (`web.read`, `mobile.read`, `atm.read atm.write`, `read write`).
2. Crear clientes (`POST :8086/api/v1/clientes`).
3. Crear cuentas (`POST :8084/api/v1/cuentas-bancarias`).
4. Consultar BFF Web (`GET :8091/api/v1/bff/web/clientes/{id}/dashboard`).
5. Consultar BFF Móvil (`GET :8092/api/v1/bff/mobile/clientes/{id}/resumen`).
6. Consultar BFF ATM (`GET :8093/api/v1/bff/atm/cuentas/{id}/saldo`).
7. Realizar transferencia con idempotencia (`POST :8087/api/v1/pagos`).
8. Ejecutar los 3 jobs Spring Batch (`POST :8085/api/v1/batch/...`).
9. Verificar archivos CSV en `batch-service/output/`.

---

## 🎯 Resultado

La solución implementa una arquitectura moderna de microservicios desplegada en **AWS EC2** que integra:

- **Seguridad** mediante OAuth 2.0 y JWT con un `auth-server` dedicado.
- **Resiliencia** mediante Resilience4j con Circuit Breaker y Retry.
- **Arquitectura orientada a eventos** con Apache Kafka.
- **Descubrimiento de servicios** con Eureka Server.
- **Configuración centralizada** con Config Server.
- **Migración de procesos batch** con Spring Batch.
- **Patrón BFF** para tres canales diferenciados.
- **Despliegue contenerizado** con Docker y Docker Compose sobre AWS EC2.

---

## EFT Semana 9: procesos Spring Batch

Se agregó `batch-service` como módulo separado para migrar los tres procesos legacy requeridos. Cada job lee su CSV con Spring Batch, procesa los registros en chunks transaccionales, reintenta ante errores de E/S o fallos transitorios y contabiliza registros inválidos mediante un `SkipListener`.

| Proceso | Endpoint | Parámetro | Archivo generado |
|---|---|---|---|
| Reporte diario de transacciones | `POST /api/v1/batch/daily-transactions` | `date=2024-06-30` | `daily-transactions-<runId>.csv` |
| Cálculo mensual de intereses | `POST /api/v1/batch/monthly-interest` | `month=2024-06` | `monthly-interest-<runId>.csv` |
| Estados de cuenta anuales | `POST /api/v1/batch/annual-statements` | `year=2024` | `annual-statements-<runId>.csv` |

Los endpoints requieren JWT con scope `write`. En Docker los CSV legacy se montan desde `bank-api/src/main/resources/data/legacy`; los informes quedan en `batch-service/output` y el repositorio de ejecuciones de Spring Batch se persiste en el volumen `batch-state`.

---

## 👤 Autor

- Cristian Olivares

---

## 📚 Referencias

- [Spring Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [Spring Cloud Netflix Eureka](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/)
- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Resilience4j — Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/)
- [Spring Security — OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Batch](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Docker Compose](https://docs.docker.com/compose/)