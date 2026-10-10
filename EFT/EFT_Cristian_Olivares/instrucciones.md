# Instrucciones de ejecución y pruebas

## 1. Requisitos

- Docker Desktop en ejecución y Docker Compose v2.
- Postman (o una herramienta HTTP equivalente).
- Java 17 y Maven Wrapper solo si se desea ejecutar un módulo fuera de Docker.

## 2. Levantar la solución local

Desde la carpeta raíz del proyecto:

```powershell
docker compose up --build -d
docker compose ps
```

Para evitar que el entorno local sature la máquina al iniciar varios servicios
Spring simultáneamente, Compose limita cada JVM a dos procesadores. Este ajuste
solo aplica al desarrollo local y no a las tareas ECS.

Espera a que `eureka-server` y `config-server` estén saludables. Comprueba el
registro de servicios en `http://localhost:8761`. Los puertos publicados y el
estado de cada contenedor se pueden consultar con `docker compose ps`.

Para apagar el entorno:

```powershell
docker compose down
```

El volumen `batch-state` conserva el repositorio de ejecuciones Spring Batch.
Los archivos generados por Batch quedan en `batch-service/output/`.

## 3. Obtener tokens OAuth 2.0 por canal

En Postman crea una solicitud `POST` a `http://localhost:9000/oauth2/token`,
selecciona Basic Auth (`bank-client` / `secret`) y usa
`application/x-www-form-urlencoded`:

```text
grant_type=client_credentials
scope=web.read
```

Repite la solicitud usando `scope=mobile.read` para el BFF móvil y
`scope=atm.read atm.write` para el BFF de cajeros. Para consumir directamente
las APIs legacy existentes o Batch, solicita `scope=read write`. No publiques
los secretos de desarrollo de este entorno local.

Comprueba que las autorizaciones por canal sean distintas:

- El token `web.read` puede acceder a `bff-web`, pero no a `bff-mobile`.
- El token `mobile.read` puede acceder a `bff-mobile`, pero no a `bff-web`.
- El token `atm.read atm.write` puede consultar saldo y enviar transferencias
  por `bff-atm`.
- Una solicitud protegida sin token debe responder `401`; una solicitud con
  scope de otro canal debe responder `403`.

## 4. Probar gestión de clientes

Usa un token con scope `write`:

```http
POST http://localhost:8086/api/v1/clientes
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "nombres": "Cristian",
  "apellidos": "Olivares",
  "rut": "12345678-9",
  "correo": "cristian@example.cl",
  "telefono": "+56912345678"
}
```

La respuesta debe ser `201 Created`. Para consultar el perfil, usa
`GET http://localhost:8086/api/v1/clientes/{id}` con un token `read`,
`web.read` o `mobile.read`. Repetir el RUT o correo devuelve `409 Conflict`.

## 5. Probar gestión de cuentas

En estas instrucciones, `cuentas-api` se publica como puerto local `8084`
(dentro de Docker escucha en `8082`).

```http
POST http://localhost:8084/api/v1/cuentas-bancarias
Authorization: Bearer <token con scope write>
Content-Type: application/json
```

```json
{
  "clienteId": 1,
  "tipo": "AHORRO",
  "saldoInicial": 50000.00
}
```

La API también permite consultar `GET /api/v1/cuentas-bancarias`,
`GET /api/v1/cuentas-bancarias/{id}`, mantener datos mediante `PUT
/api/v1/cuentas-bancarias/{id}` y cerrar mediante `PATCH
/api/v1/cuentas-bancarias/{id}/cierre`.

## 6. Probar transferencias, pagos, outbox y Kafka

```http
POST http://localhost:8087/api/v1/pagos
Authorization: Bearer <token con scope write o atm.write>
Content-Type: application/json
```

```json
{
  "cuentaOrigen": 1,
  "cuentaDestino": 2,
  "monto": 1250.00,
  "moneda": "CLP",
  "claveIdempotencia": "eft-demo-0001"
}
```

`pagos-api` propaga el bearer token a `cuentas-api`, que debita y acredita los
saldos en una transacción local protegida con bloqueos y clave idempotente. La
transferencia debe ser en CLP. Solo después de confirmarse, `pagos-api` persiste
el pago y su evento outbox en una misma transacción local. No es una transacción
distribuida XA: si hay un fallo técnico tras mover los fondos, reintenta con la
misma clave para que `cuentas-api` responda idempotentemente sin repetir el
débito. Los errores de negocio (por ejemplo, saldo insuficiente) se devuelven
sin reintento; los fallos de conectividad usan Retry y Circuit Breaker.

El publicador programado reintenta la salida del outbox hacia
`pagos-procesados`; el consumidor registra el evento y cambia el pago a
`PROCESADO`. Consulta el estado con `GET
http://localhost:8087/api/v1/pagos/{id}`. Repetir la misma clave con los mismos
datos devuelve el pago existente; reutilizarla con otros datos devuelve
`409 Conflict`.

Verifica productor y consumidor:

```powershell
docker compose logs -f pagos-api
docker compose logs -f kafka
```

## 7. Probar los BFF

Todos los BFF propagan el bearer token a los microservicios internos mediante
descubrimiento Eureka.

- **Web** — token `web.read`:
  `GET http://localhost:8091/api/v1/bff/web/clientes/{id}/dashboard`.
  Combina el perfil completo y las cuentas del cliente.
- **Móvil** — token `mobile.read`:
  `GET http://localhost:8092/api/v1/bff/mobile/clientes/{id}/resumen`.
  Entrega un payload reducido sin RUT ni correo.
- **Cajeros** — token `atm.read`:
  `GET http://localhost:8093/api/v1/bff/atm/cuentas/{id}/saldo`.
- **Transferencia desde cajero** — token `atm.write`:
  `POST http://localhost:8093/api/v1/bff/atm/transferencias`, usando el cuerpo
  del ejemplo de pagos anterior.

## 8. Ejecutar los jobs Spring Batch

En Docker, consulta el puerto asignado al contenedor:

```powershell
docker compose port batch-service 8085
```

Usa ese puerto local para enviar los siguientes `POST` con un token `write`:

```text
/api/v1/batch/daily-transactions?date=2024-06-30
/api/v1/batch/monthly-interest?month=2024-06
/api/v1/batch/annual-statements?year=2024
```

La respuesta informa el identificador de ejecución y la ruta del CSV producido.
Los CSV aparecen en `batch-service/output/`. Para reiniciar una ejecución
fallida, vuelve a enviar los mismos parámetros con el mismo `runId`. Para
ejecutar un nuevo informe del mismo período, usa un `runId` nuevo u omítelo.

Las tasas mensuales por defecto son `0.005` para ahorro y `0.020` para
préstamo/hipoteca. Son parámetros demostrativos: el archivo legacy no contiene
tasas de interés y deben reemplazarse por los valores aprobados para el caso.

## 9. Pruebas automatizadas

Ejecuta los tests del módulo desde su carpeta:

```powershell
Set-Location .\batch-service
..\bank-api\mvnw.cmd -f pom.xml test

Set-Location ..\clientes-api
..\bank-api\mvnw.cmd -f pom.xml test

Set-Location ..\cuentas-api
.\mvnw.cmd test

Set-Location ..\pagos-api
..\bank-api\mvnw.cmd -f pom.xml test
```

Los BFF se validan con `..\bank-api\mvnw.cmd -f pom.xml test` desde cada una de
sus carpetas (`bff-web`, `bff-mobile` y `bff-atm`).

## 10. Evidencias recomendadas

Guarda capturas de: tokens por canal; rechazo `401`/`403`; apertura y cierre de
cuentas; creación/idempotencia de pagos; evento recibido desde Kafka; ejecución
exitosa de cada job y CSV producido; Eureka con los servicios `UP`; y
`docker compose ps`. Estas capturas complementan, pero no reemplazan, el video
MP4 de 5 a 7 minutos que grabarás con webcam.
