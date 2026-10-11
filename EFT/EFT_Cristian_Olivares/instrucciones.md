# Instrucciones de ejecución y pruebas

## 1. Requisitos

- Docker y Docker Compose v2 (en Windows: Docker Desktop; en Linux/EC2: `docker.io` + `docker-compose-v2`).
- Postman (o una herramienta HTTP equivalente).
- Java 17 y Maven Wrapper solo si se desea ejecutar un módulo fuera de Docker.

## 2. Levantar la solución local

Desde la carpeta raíz del proyecto:

```bash
docker compose up --build -d
docker compose ps
```

Para evitar que el entorno local sature la máquina al iniciar varios servicios
Spring simultáneamente, Compose limita cada JVM a dos procesadores. Este ajuste
solo aplica al desarrollo local y no al entorno de producción.

Espera a que `eureka-server` y `config-server` estén saludables. Comprueba el
registro de servicios en `http://localhost:8761`. Los puertos publicados y el
estado de cada contenedor se pueden consultar con `docker compose ps`.

Para apagar el entorno:

```bash
docker compose down
```

El volumen `batch-state` conserva el repositorio de ejecuciones Spring Batch.
Los archivos generados por Batch quedan en `batch-service/output/`.

## 3. Obtener tokens OAuth 2.0 por canal

En Postman crea una solicitud `POST` a `http://localhost:9000/oauth2/token`
(en EC2: `http://<IP_PUBLICA>:9000/oauth2/token`), selecciona Basic Auth
(`bank-client` / `secret`) y usa `application/x-www-form-urlencoded`:

```text
grant_type=client_credentials
scope=web.read
```

Repite la solicitud usando `scope=mobile.read` para el BFF móvil y
`scope=atm.read atm.write` para el BFF de cajeros. Para consumir directamente
las APIs legacy existentes o Batch, solicita `scope=read write`.

Las credenciales `bank-client` / `secret` son solo para desarrollo y
demostración; no deben publicarse ni usarse en un entorno real.

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

```bash
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

El `batch-service` está publicado en el puerto fijo **8085**. Envía los
siguientes `POST` con un token `write` a `http://localhost:8085` (o
`http://<IP_PUBLICA>:8085` en EC2):

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

Verifica los archivos generados:

```bash
ls -l batch-service/output/
head batch-service/output/annual-statements-*.csv
```

## 9. Pruebas automatizadas

Ejecuta los tests de cada módulo desde su carpeta.

**En Windows (PowerShell):**

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

**En Linux/macOS (EC2):**

```bash
cd batch-service
../bank-api/mvnw -f pom.xml test

cd ../clientes-api
../bank-api/mvnw -f pom.xml test

cd ../cuentas-api
./mvnw test

cd ../pagos-api
../bank-api/mvnw -f pom.xml test
```

Los BFF se validan con `mvnw test` desde cada una de sus carpetas
(`bff-web`, `bff-mobile` y `bff-atm`).

## 10. Despliegue en AWS EC2

El despliegue en la nube se realizó sobre una instancia **EC2 t3.large**
(Ubuntu 24.04). Los pasos completos están en `despliegue.md`. Resumen:

1. Crear la instancia EC2 y configurar el Security Group con las reglas de
   entrada necesarias (8081, 8083-8087, 8091-8093, 8761, 8888, 9000, 9092).
2. Instalar Docker, Docker Compose v2 y Git.
3. Clonar el repositorio y ejecutar:
   ```bash
   docker compose up -d --build
   docker compose ps
   ```
4. Verificar el dashboard de Eureka en `http://<IP_PUBLICA>:8761` y ejecutar
   las pruebas de las secciones 3 a 8 reemplazando `localhost` por
   `<IP_PUBLICA>`.

**Nota sobre el puerto 8085:** En `docker-compose.yaml` el `batch-service`
debe declarar el mapeo explícito `"8085:8085"`. Si se deja solo `"8085"`,
Docker asigna un puerto aleatorio y las peticiones externas fallan con
`ECONNREFUSED`.

## 11. Evidencias recomendadas

Guarda capturas de:

- Tokens por canal (`web.read`, `mobile.read`, `atm.read atm.write`, `read write`).
- Rechazo `401`/`403` al usar un token en el canal equivocado.
- Apertura y cierre de cuentas.
- Creación de pago con idempotencia y repetición de la misma clave.
- Evento recibido desde Kafka en los logs de `pagos-api`.
- Ejecución exitosa de cada job de Spring Batch y CSV producido.
- Dashboard de Eureka con todos los servicios en estado `UP`.
- `docker compose ps` con todos los contenedores `running`/`healthy`.

Estas capturas complementan, pero no reemplazan, el video MP4 de 5 a 7 minutos
que grabarás con webcam.