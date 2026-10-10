# Exp3_S8_Cristian_Olivares - Desarrollo Backend III

## Desarrollando microservicios y resiliencia en la nube con Spring Cloud

Proyecto correspondiente a la **Experiencia 3 - Semana 8** de la asignatura **Desarrollo Backend III (PBY2203)**.

---

## 📌 Objetivo del proyecto

El objetivo del proyecto es consolidar una arquitectura basada en microservicios fortaleciendo su **resiliencia**, incorporando **seguridad con OAuth 2.0 y JWT**, y habilitando un despliegue completo en un entorno **Cloud mediante Docker y Docker Compose**.

La solución permite registrar transacciones mediante `bank-api`, generar eventos asociados a cada operación, publicarlos en Apache Kafka y procesarlos de forma asíncrona. Adicionalmente, todos los microservicios se encuentran protegidos mediante un `auth-server` que emite tokens JWT, los cuales son validados por cada API.

---

## 🏗️ Arquitectura de la solución

El proyecto utiliza una arquitectura de microservicios compuesta por:

- **Config Server**: centraliza la configuración de los microservicios.
- **Eureka Server**: permite el registro y descubrimiento de servicios.
- **auth-server**: servidor de autorización OAuth 2.0 encargado de emitir tokens JWT.
- **bank-api**: administra transacciones y su flujo de eventos Kafka.
- **cuentas-api**: administra apertura, mantenimiento, consulta y cierre de cuentas bancarias, además de los datos legacy de cartolas.
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

La seguridad del sistema está basada en el protocolo **OAuth 2.0** con tokens **JWT**, implementado mediante **Spring Security**.

### Componentes de seguridad

- **auth-server**: actúa como **Authorization Server**. Se encarga de autenticar a los clientes y emitir los tokens JWT.
- **bank-api, cuentas-api, intereses-api**: actúan como **Resource Servers**. Validan los tokens JWT antes de permitir el acceso a sus endpoints.

### Configuración del cliente OAuth 2.0

    Client ID:     bank-client
    Client Secret: secret
    Grant Types:   client_credentials, authorization_code, refresh_token
    Scopes:        openid, profile, read, write, web.read, mobile.read, atm.read, atm.write

Los endpoints GET de las APIs requieren `SCOPE_read` y los endpoints POST
requieren `SCOPE_write`. Los BFF usan scopes diferenciados: `web.read`,
`mobile.read`, `atm.read` y `atm.write`. Así, por ejemplo, un token con
`scope=mobile.read` puede usar el BFF móvil, pero no el BFF web ni las
transferencias del cajero. Para obtener un token del canal móvil se envía
`grant_type=client_credentials` y `scope=mobile.read` al endpoint
`/oauth2/token`; para el cajero se usa `scope=atm.read atm.write`.

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
       bank-api / cuentas-api / intereses-api
           |
           | 4. Valida JWT
           v
       200 OK / 401 Unauthorized / 403 Forbidden

---

## 🔄 Arquitectura orientada a eventos

Para el procesamiento asíncrono de transacciones se implementó el siguiente flujo:

    Cliente / Postman
           |
           | POST /api/v1/transacciones
           v
       BankController
           |
           v
        BankService
          /     \
         v       v
        H2   TransaccionProducer
                  |
                  | TransaccionCreadaEvent
                  v
            Apache Kafka
                  |
                  | Topic:
                  | transacciones-creadas
                  v
          TransaccionConsumer

Cuando se registra una nueva transacción:

1. `BankController` recibe la solicitud.
2. `BankService` procesa y almacena la transacción en H2.
3. Se genera un `TransaccionCreadaEvent`.
4. `TransaccionProducer` publica el evento en Apache Kafka.
5. El evento se envía al tópico `transacciones-creadas`.
6. `TransaccionConsumer` recibe y procesa el evento de manera asíncrona.

> **Nota:** la arquitectura de eventos con Kafka se implementa únicamente en `bank-api`. Los microservicios `cuentas-api` e `intereses-api` mantienen su comportamiento síncrono.

---

## 📨 Evento implementado

El evento utilizado para representar una nueva transacción es:

    TransaccionCreadaEvent

Contiene los siguientes datos:

- Identificador de la transacción.
- Fecha.
- Monto.
- Tipo de transacción.

El tópico utilizado en Kafka es:

    transacciones-creadas

El grupo utilizado por el Consumer es:

    bank-api-group

El tópico se declara con **3 particiones** y el listener usa **3 consumidores
concurrentes** que pertenecen al mismo grupo. Así Kafka puede distribuir las
particiones entre los consumidores; el máximo de consumidores activos del grupo
queda limitado por el número de particiones. El identificador de partición y el
hilo consumidor se incluyen en cada log del evento para hacer visible esa
distribución.

La configuración está en `app.kafka` dentro del archivo de configuración de
`bank-api` (y también en el Config Server). Si el tópico ya existía con menos
particiones, la declaración de Spring no las reduce ni las amplía: aumente las
particiones manualmente antes de validar:

```powershell
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --alter --topic transacciones-creadas --partitions 3
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --describe --topic transacciones-creadas
```

Si `bank-api` ya estaba conectado al tópico cuando se aumentaron las
particiones, reinícielo para que el grupo vuelva a asignarlas:

```powershell
docker compose restart bank-api
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group bank-api-group
```

Después de registrar varias transacciones, revise el identificador de partición
y el hilo en los logs del consumidor:

```powershell
docker compose logs bank-api 2>&1 | Select-String "EVENTO RECIBIDO DESDE KAFKA"
```

Una transacción se asigna a una partición según su clave (el ID de la
transacción); por eso conviene producir varios eventos con diferentes IDs.
Esta configuración demuestra concurrencia dentro de una instancia de
`bank-api`; no representa por sí sola escalamiento horizontal de contenedores.

---

## 🛡️ Tolerancia a fallos

La solución utiliza **Resilience4j** para fortalecer la resiliencia de los microservicios.

Se implementaron los patrones:

### Circuit Breaker

Permite controlar fallos durante la ejecución de las operaciones y utilizar métodos fallback cuando una operación no puede completarse correctamente.

### Retry

Permite realizar reintentos automáticos ante determinados errores antes de considerar que una operación ha fallado definitivamente.

En `bank-api`, estos mecanismos se encuentran aplicados sobre las operaciones administradas por `BankService`.

El fallback de consulta de una transacción devuelve un objeto identificable con
ID `-1` y tipo `FALLBACK`, en lugar de un `null`, para que la recuperación sea
visible en la respuesta. Una prueba de interrupción debe registrar tanto el
estado del Circuit Breaker como la respuesta fallback observada por el cliente.

### Verificar Circuit Breaker y fallback

Con un token que incluya el scope `read`, realiza varias solicitudes a
`GET /api/v1/transacciones/99999` para provocar un error controlado en la
consulta. La API responde con el objeto fallback (`id: -1` y `tipo` con el
prefijo `FALLBACK`). Consulta `GET /actuator/circuitbreakers` con el mismo
token para observar el estado del circuito. Con la configuración actual,
`minimum-number-of-calls` es 5 y el umbral de fallos es 50%; después de
alcanzar el umbral, el estado esperado es `OPEN`, con solicitudes posteriores
contabilizadas como `notPermittedCalls`. Al cumplirse `wait-duration`, el
circuito transita automáticamente a `HALF_OPEN` para permitir llamadas de
prueba.

Esta prueba fuerza un error de consulta dentro de `bank-api`; no equivale a
detener una dependencia externa ni demuestra el patrón `Retry`, que debe
probarse por separado con una excepción incluida en `retry-exceptions`.

### Verificar Retry

La prueba automatizada `BankServiceRetryTest` simula dos fallos transitorios
de tipo `IOException` en la consulta al repositorio de transacciones y permite
que la tercera llamada se complete. Comprueba que la operación se intentó
exactamente tres veces y terminó correctamente, acorde con `max-attempts: 3`
y con `IOException` incluida en `retry-exceptions`. La prueba conserva el
tiempo de espera de 2 segundos configurado para `bank-api`.

Ejecuta la prueba desde la carpeta `bank-api`:

```powershell
.\mvnw.cmd -Dtest=BankServiceRetryTest test
```

La prueba reemplaza el repositorio por un mock y no detiene un servicio real;
demuestra el comportamiento de reintento ante fallos transitorios de E/S.
El orden de aspectos coloca Retry dentro de Circuit Breaker para que el
Circuit Breaker registre el resultado final de los intentos y no convierta el
primer fallo transitorio en una respuesta fallback antes de reintentar.

---

## 🐳 Contenerización y despliegue en la nube

Todos los componentes del sistema se encuentran contenerizados con **Docker** y orquestados con **Docker Compose**, permitiendo levantar toda la arquitectura con un solo comando.

### Contenedores definidos

| Contenedor | Imagen | Puerto |
|---|---|---|
| banco-kafka | apache/kafka:4.3.1 | 9092 |
| eureka-server | Dockerfile propio | 8761 |
| config-server | Dockerfile propio | 8888 |
| auth-server | Dockerfile propio | 9000 |
| bank-api | Dockerfile propio | 8081 |
| cuentas-api | Dockerfile propio | 8084 (host) → 8082 (contenedor) |
| intereses-api | Dockerfile propio | 8083 |
| batch-service | Dockerfile propio | Puerto publicado dinámicamente → 8085 |
| clientes-api | Dockerfile propio | 8086 |
| pagos-api | Dockerfile propio | 8087 |
| bff-web | Dockerfile propio | 8091 |
| bff-mobile | Dockerfile propio | 8092 |
| bff-atm | Dockerfile propio | 8093 |

### Red interna

Todos los contenedores se comunican a través de la red interna `banco-network`, definida en `docker-compose.yaml`. Esto permite que los microservicios se encuentren entre sí usando sus nombres de servicio (por ejemplo `http://config-server:8888`) en lugar de `localhost`.

Eureka Server y Config Server incluyen healthchecks. Los demás servicios esperan
a que esos servidores estén saludables antes de iniciar; `bank-api` y
`pagos-api` también esperan a que Kafka haya iniciado.

---

## ⚙️ Tecnologías utilizadas

- Java 17
- Spring Boot 4.1.0
- Spring Cloud 2025.1.3
- Spring Cloud Config
- Netflix Eureka
- Spring Security
- Spring OAuth2 Authorization Server
- Spring OAuth2 Resource Server
- Spring Data JPA
- Apache Kafka
- Spring Kafka
- Resilience4j
- H2 Database
- Maven
- Docker
- Docker Compose
- Postman

---

## 📂 Estructura general

    Exp3_S8_Cristian_Olivares/
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
    |-- README.md

### auth-server

Contiene el servidor de autorización OAuth 2.0 que emite los tokens JWT.

    auth-server/
        src/main/java/com/bancoxyz/authserver/
            AuthServerApplication.java
            SecurityConfig.java
        src/main/resources/
            application.yml
        Dockerfile
        pom.xml

### bank-api

Contiene la implementación principal de la arquitectura orientada a eventos y la seguridad OAuth 2.0.

    bank-api/
        src/main/java/com/bancoxyz/banco_microservicio/
            controller/
                BankController.java
            service/
                BankService.java
            event/
                TransaccionCreadaEvent.java
            kafka/
                TransaccionProducer.java
                TransaccionConsumer.java
            config/
                KafkaConfig.java
                SecurityConfig.java
        src/main/resources/
            application.yml
        Dockerfile
        pom.xml

### clientes-api, pagos-api y batch-service

`clientes-api` mantiene perfiles de clientes y aplica unicidad de RUT/correo.
`pagos-api` coordina transferencias CLP con `cuentas-api`, propaga la
autorización del usuario y emplea claves idempotentes para evitar débitos
duplicados ante reintentos. Tras confirmar el movimiento, registra el pago y su
evento outbox en una transacción local para reintentar la publicación a Kafka.
Los errores HTTP de negocio no se reintentan; Retry y Circuit Breaker cubren
indisponibilidad técnica del servicio de cuentas. `batch-service` contiene los
tres jobs Spring Batch; ver la sección EFT Semana 9 para parámetros y salidas.

### BFF por canal

`bff-web`, `bff-mobile` y `bff-atm` son aplicaciones independientes. Web
agrega el perfil completo del cliente y sus cuentas; Móvil entrega solo campos
esenciales; Cajeros ofrece consulta de saldo y envío de transferencias. Cada
BFF valida su scope OAuth específico y propaga el token a los servicios
descubiertos por Eureka.

### config-server

Contiene la configuración centralizada de los microservicios.

    config-server/
        src/main/resources/
            application.yml
            config-repo/
                bank-api.yml
                cuentas-api.yml
                intereses-api.yml
        Dockerfile
        pom.xml

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
| batch-service | 8085 (puerto host dinámico en Compose) |
| clientes-api | 8086 |
| pagos-api | 8087 |
| bff-web | 8091 |
| bff-mobile | 8092 |
| bff-atm | 8093 |
| Apache Kafka | 9092 |

---

## 🚀 Ejecución del proyecto

### Requisitos previos

- Docker Desktop instalado y en ejecución.
- Docker Compose v2 o superior.
- Postman para las pruebas.

### Levantar toda la arquitectura

Desde la carpeta raíz del proyecto ejecutar:

    docker compose up --build

Docker construirá las imágenes de los microservicios y BFF (la primera vez
puede tardar varios minutos) y levantará los servicios según las condiciones de
inicio declaradas en `docker-compose.yaml`.

### Verificar que los servicios estén corriendo

    docker ps

Deben aparecer los contenedores de infraestructura, microservicios y canales:

    banco-kafka
    eureka-server
    config-server
    auth-server
    bank-api
    cuentas-api
    intereses-api
    batch-service
    clientes-api
    pagos-api
    bff-web
    bff-mobile
    bff-atm

### Verificar el registro en Eureka

Abrir en el navegador:

    http://localhost:8761

Deberían aparecer los servicios backend y BFF en estado `UP`:

- AUTH-SERVER
- BANK-API
- CUENTAS-API
- INTERESES-API
- CLIENTES-API
- PAGOS-API
- BATCH-SERVICE
- BFF-WEB
- BFF-MOBILE
- BFF-ATM
- CONFIG-SERVER

### Detener la arquitectura

    docker-compose down

---

## 🧪 Pruebas de la solución

### 1. Obtener un token JWT

Solicitud en Postman:

    POST http://localhost:9000/oauth2/token

Configuración:

    Authorization: OAuth 2.0
    Grant Type: Client Credentials
    Access Token URL: http://localhost:9000/oauth2/token
    Client ID: bank-client
    Client Secret: secret
    Scope: read

Respuesta esperada: un token JWT (cadena que comienza con `eyJ...`).

### 2. Probar la seguridad de bank-api

**Sin token (debe fallar):**

    GET http://localhost:8081/api/v1/transacciones

Resultado esperado:

    401 Unauthorized

**Con token (debe funcionar):**

    GET http://localhost:8081/api/v1/transacciones
    Authorization: Bearer <JWT>

Resultado esperado:

    200 OK

### 3. Crear una transacción

    POST http://localhost:8081/api/v1/transacciones
    Authorization: Bearer <JWT>
    Content-Type: application/json

Body:

    {
        "fecha": "2026-10-02",
        "monto": 150000.0,
        "tipo": "DEPOSITO"
    }

Ejemplo de respuesta:

    {
        "fecha": "2026-10-02",
        "id": 1001,
        "monto": 150000.0,
        "tipo": "DEPOSITO"
    }

---

## ✅ Validación del flujo de Kafka

Después de registrar la transacción, la consola del contenedor `bank-api` permite verificar la publicación del evento:

    docker logs -f bank-api

Se evidencia:

    Evento enviado a Kafka -> tópico: transacciones-creadas

Posteriormente el Consumer procesa el evento:

    EVENTO RECIBIDO DESDE KAFKA

Esto permite comprobar el flujo completo:

    POST -> bank-api -> H2 -> Producer -> Kafka -> Consumer

---

## 🎯 Resultado

La solución implementa una arquitectura moderna de microservicios en la nube que integra:

- **Seguridad** mediante OAuth 2.0 y JWT con un `auth-server` dedicado.
- **Resiliencia** mediante Resilience4j con Circuit Breaker y Retry.
- **Arquitectura orientada a eventos** con Apache Kafka.
- **Descubrimiento de servicios** con Eureka Server.
- **Configuración centralizada** con Config Server.
- **Despliegue contenerizado** con Docker y Docker Compose.

Todo el sistema puede levantarse con un solo comando en el entorno local. La
consigna de la EFT solicita preparar un despliegue en AWS, pero no especifica
un servicio concreto; para ese objetivo se propone **Amazon ECS Fargate**, con
imágenes en ECR y balanceo mediante ALB. Compose se mantiene como entorno local
de desarrollo y validación, no como evidencia de un despliegue productivo en
AWS.

---

## EFT Semana 9: procesos Spring Batch

Se agregó `batch-service` como módulo separado para migrar los tres procesos
legacy requeridos. Cada job lee su CSV con Spring Batch, procesa los registros
en chunks transaccionales de 100, reintenta hasta tres veces ante errores de
E/S o fallos transitorios de acceso a datos y omite/contabiliza registros
inválidos mediante un `SkipListener`. Los tres jobs son independientes y
pueden ejecutarse en paralelo con parámetros y archivos de salida distintos;
cada ejecución mantiene su propio estado reiniciable en el repositorio Batch.

| Proceso | Endpoint | Parámetro | Archivo generado |
|---|---|---|---|
| Reporte diario de transacciones | `POST /api/v1/batch/daily-transactions` | `date=2024-06-30` | `daily-transactions-<runId>.csv` |
| Cálculo mensual de intereses | `POST /api/v1/batch/monthly-interest` | `month=2024-06` | `monthly-interest-<runId>.csv` |
| Estados de cuenta anuales | `POST /api/v1/batch/annual-statements` | `year=2024` | `annual-statements-<runId>.csv` |

Los endpoints requieren JWT con scope `write`. En Docker los CSV legacy se
montan desde `bank-api/src/main/resources/data/legacy`; los informes quedan en
`batch-service/output` y el repositorio de ejecuciones de Spring Batch se
persiste en el volumen `batch-state`.

Las tasas del proceso mensual se parametrizan mediante
`BATCH_SAVINGS_MONTHLY_RATE` y `BATCH_LOAN_MONTHLY_RATE` (por defecto `0.005` y
`0.020`). El dataset legacy no contiene tasas ni período por cuenta, así que
estos valores son supuestos configurables y deben validarse con la regla de
negocio que se presente en el informe EFT.

Para ejecutar localmente:

```powershell
Set-Location .\batch-service
..\bank-api\mvnw.cmd -f pom.xml spring-boot:run
```

Para probar los jobs automatizados:

```powershell
Set-Location .\batch-service
..\bank-api\mvnw.cmd -f pom.xml test
```

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
- [Docker Compose](https://docs.docker.com/compose/)
- Repositorio de datos legacy: [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data)