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
- **bank-api**: administra las operaciones bancarias y el flujo de eventos de las transacciones.
- **cuentas-api**: microservicio encargado de las operaciones relacionadas con cuentas.
- **intereses-api**: microservicio encargado de las operaciones relacionadas con intereses.
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
    Scopes:        openid, profile, read, write

Los endpoints GET de las APIs requieren `SCOPE_read`. Los endpoints POST y
cualquier otro método requieren `SCOPE_write`. Para solicitar un token de
escritura mediante `client_credentials`, enviar `grant_type=client_credentials`
y `scope=read write` al endpoint `/oauth2/token`; para comprobar la restricción,
solicitar un token con `scope=read` e intentar un POST, que debe responder
`403 Forbidden`.

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
docker exec banco-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --alter --topic transacciones-creadas --partitions 3
docker exec banco-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --describe --topic transacciones-creadas
```

Si `bank-api` ya estaba conectado al tópico cuando se aumentaron las
particiones, reinícielo para que el grupo vuelva a asignarlas:

```powershell
docker compose restart bank-api
docker exec banco-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka:9092 --describe --group bank-api-group
```

Después de registrar varias transacciones, revise el identificador de partición
y el hilo en los logs del consumidor:

```powershell
docker logs bank-api 2>&1 | Select-String "EVENTO RECIBIDO DESDE KAFKA"
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

### Red interna

Todos los contenedores se comunican a través de la red interna `banco-network`, definida en `docker-compose.yaml`. Esto permite que los microservicios se encuentren entre sí usando sus nombres de servicio (por ejemplo `http://config-server:8888`) en lugar de `localhost`.

Eureka Server y Config Server incluyen healthchecks. Los demás servicios esperan
a que esos servidores estén saludables antes de iniciar; `bank-api` también
espera a que Kafka haya iniciado.

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
    |-- intereses-api/
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

Docker construirá las imágenes de los 6 microservicios (la primera vez puede tardar varios minutos) y levantará los servicios según las condiciones de salud declaradas en `docker-compose.yaml`.

### Verificar que los servicios estén corriendo

    docker ps

Deben aparecer los siguientes contenedores:

    banco-kafka
    eureka-server
    config-server
    auth-server
    bank-api
    cuentas-api
    intereses-api

### Verificar el registro en Eureka

Abrir en el navegador:

    http://localhost:8761

Deberían aparecer los 5 servicios en estado `UP`:

- AUTH-SERVER
- BANK-API
- CUENTAS-API
- INTERESES-API
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

Todo el sistema puede levantarse con un solo comando y queda listo para ser desplegado en cualquier entorno Cloud compatible con Docker.

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