# Guía de despliegue en AWS — EC2 + Docker Compose

## 1. Alcance y arquitectura elegida

Las instrucciones de la EFT piden preparar y desplegar los microservicios en un
entorno de nube (AWS) usando Docker. Para este proyecto se despliega **toda la
solución en una instancia Amazon EC2** (Ubuntu 24.04) ejecutando el mismo
`docker-compose.yaml` que se usa en local:

```text
+-------------------------------------------------------------+
|               EC2 t3.large (Ubuntu 24.04)                   |
|            Docker Compose - Red: banco-network              |
|                                                             |
|  eureka-server  config-server  auth-server  kafka           |
|  bank-api       cuentas-api    intereses-api  clientes-api  |
|  pagos-api      batch-service                               |
|  bff-web        bff-mobile     bff-atm                      |
+-------------------------------------------------------------+
```

Se eligió EC2 en lugar de ECS Fargate porque el entorno de AWS disponible limita
los permisos IAM necesarios para ECS, ECR, MSK y RDS, y porque Docker Compose ya
orquesta los 13 contenedores (12 aplicaciones Spring + Kafka) con sus
dependencias y healthchecks. La arquitectura de producción recomendada se
describe en la sección 8.

## 2. Puertos publicados por servicio

El `docker-compose.yaml` publica los siguientes puertos en la instancia EC2:

- **bank-api:** 8081 (interno) a 8081 (EC2)
- **cuentas-api:** 8082 (interno) a 8084 (EC2)
- **intereses-api:** 8083 (interno) a 8083 (EC2)
- **batch-service:** 8085 (interno) a 8085 (EC2)
- **clientes-api:** 8086 (interno) a 8086 (EC2)
- **pagos-api:** 8087 (interno) a 8087 (EC2)
- **bff-web:** 8091 (interno) a 8091 (EC2)
- **bff-mobile:** 8092 (interno) a 8092 (EC2)
- **bff-atm:** 8093 (interno) a 8093 (EC2)
- **auth-server:** 9000 (interno) a 9000 (EC2)
- **eureka-server:** 8761 (interno) a 8761 (EC2)
- **config-server:** 8888 (interno) a 8888 (EC2)
- **kafka:** 9092 (interno) a 9092 (EC2)

> **Nota sobre batch-service:** Es importante que el mapeo en
> `docker-compose.yaml` sea explícito (`"8085:8085"`) y no solo `"8085"`. Si se
> deja solo el puerto interno, Docker asigna un puerto aleatorio en el host (por
> ejemplo 32769) y las peticiones externas fallan con `ECONNREFUSED`. Este
> ajuste ya está aplicado en el repositorio.

## 3. Crear la instancia EC2 (consola AWS)

1. Consola AWS, región **us-east-1**, sección **EC2**, botón **Launch instance**.
2. Configuración:
   - **Name**: `bancoxyz-eft`
   - **AMI**: Ubuntu Server 24.04 LTS
   - **Instance type**: `t3.large` (2 vCPU, 8 GB RAM) como mínimo práctico
     para la demostración; toda la solución es intensiva en memoria.
   - **Key pair**: *Proceed without a key pair* (se usará EC2 Instance Connect
     desde el navegador).
   - **Storage**: 30 GiB gp3.
3. **Network settings, Create security group** con estas reglas de entrada:

   - **SSH:** puerto 22, origen Anywhere (0.0.0.0/0) para EC2 Instance Connect.
   - **Custom TCP:** puerto 8081, origen My IP, para bank-api.
   - **Custom TCP:** puertos 8083-8087, origen My IP, para cuentas, intereses, batch, clientes y pagos.
   - **Custom TCP:** puertos 8091-8093, origen My IP, para BFF web, móvil y ATM.
   - **Custom TCP:** puerto 8761, origen My IP, para el Dashboard de Eureka.
   - **Custom TCP:** puerto 8888, origen My IP, para Config Server.
   - **Custom TCP:** puerto 9000, origen My IP, para auth-server (tokens OAuth2).
   - **Custom TCP:** puerto 9092, origen My IP, para Kafka (opcional, solo para depuración).

   Los puertos de aplicación solo se abren a la IP del evaluador/desarrollador,
   no a Internet completo. Si la IP cambia, se edita la regla.
4. **Launch instance**. Esperar a que esté `Running` y `2/2 checks passed`.
5. Seleccionar la instancia, **Connect**, **EC2 Instance Connect**,
   **Connect**. Se abre una terminal en el navegador.

## 4. Preparar el servidor

Dentro de la terminal de la instancia:

```bash
# Docker, Docker Compose v2 y Git
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2 git
sudo usermod -aG docker $USER
newgrp docker

# Swap de 2 GB como margen de seguridad de memoria
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Verificar
docker --version
docker compose version
free -h
```

## 5. Descargar, construir y levantar el sistema

```bash
git clone <URL_DEL_REPOSITORIO_GITHUB>
cd <CARPETA_DEL_REPOSITORIO>
mkdir -p batch-service/output

# Construir imágenes (limitar paralelismo para no agotar memoria)
docker compose --parallel 1 build

# Levantar toda la solución
docker compose up -d
docker compose ps
```

La construcción de las imágenes toma varios minutos (Maven descarga
dependencias). Después de `up -d`, Eureka y Config Server tardan cerca de 1 a
2 minutos en quedar saludables; el resto de los servicios arranca cuando sus
dependencias están listas.

Obtener la IP pública de la instancia:

```bash
curl -s http://checkip.amazonaws.com
```

En adelante se usa `<IP_PUBLICA>`.

## 6. Verificación del despliegue

**1. Eureka**
Abrir `http://<IP_PUBLICA>:8761` y comprobar que aparecen registrados
`BANK-API`, `CUENTAS-API`, `CLIENTES-API`, `PAGOS-API`, `INTERESES-API`,
`BATCH-SERVICE`, `BFF-WEB`, `BFF-MOBILE`, `BFF-ATM` y `AUTH-SERVER` en estado
**UP**.

**2. Contenedores**
Ejecutar `docker compose ps` y verificar que todos los servicios aparezcan como
`running` o `healthy`.

**3. Token OAuth2**
Probar la generación de token desde Postman o curl:

```bash
curl -s -u bank-client:secret -X POST http://<IP_PUBLICA>:9000/oauth2/token \
  -d "grant_type=client_credentials" -d "scope=read write"
```

**4. Pruebas funcionales**
Seguir `instrucciones.md` reemplazando `localhost` por `<IP_PUBLICA>`. Los
puertos en la nube son: auth 9000, clientes 8086, cuentas 8084, pagos 8087,
batch 8085, BFF web 8091, BFF móvil 8092 y BFF ATM 8093.

**5. Kafka**
Ejecutar `docker compose logs --tail=50 pagos-api` y
`docker compose logs --tail=50 bank-api` para ver el evento producido y
consumido.

**6. Batch**
Después de ejecutar los jobs, `ls batch-service/output` lista los CSV generados.

## 7. Escalabilidad horizontal (trabajo futuro)

Los BFF no guardan estado, por lo que son candidatos naturales a escalar
horizontalmente. Sin embargo, en el `docker-compose.yaml` actual los tres BFF
tienen puertos fijos (8091, 8092 y 8093), lo que impide levantar más de un
contenedor por canal sin colisión de puertos.

Para habilitar la escalabilidad horizontal de los BFF, el siguiente paso sería
modificar el `docker-compose.yaml` para publicar rangos de puertos:

- **BFF Web:** puerto interno 8091, rango publicado 8191 a 8195.
- **BFF Móvil:** puerto interno 8092, rango publicado 8201 a 8205.
- **BFF ATM:** puerto interno 8093, rango publicado 8211 a 8215.

De esta forma, un comando como:

```bash
docker compose up -d --scale bff-web=2 --no-recreate
```

levantaría dos réplicas del BFF Web, atendiendo en los puertos 8191 y 8192. El
balanceo se delegaría a un Application Load Balancer (sección 8).

**Limitación adicional**: las APIs de datos (`clientes-api`, `cuentas-api`,
`pagos-api`) usan H2 en memoria, por lo que cada réplica tendría su propia copia
de los datos. Para escalarlas se requiere una base de datos compartida como
Amazon RDS for PostgreSQL (sección 8).

## 8. Arquitectura recomendada para producción (trabajo futuro)

El proyecto ya incluye el driver de PostgreSQL y usa configuración externa, de
modo que el paso siguiente es migrar a servicios administrados de AWS:

- **Amazon ECR** para las imágenes y **ECS Fargate** para ejecutar cada servicio
  con Service Auto Scaling por CPU/memoria.
- **Application Load Balancer** con HTTPS (certificado ACM) delante de los tres
  BFF; el resto de los servicios en subredes privadas.
- **Amazon RDS for PostgreSQL** en lugar de H2, lo que permite escalar también
  `clientes-api`, `cuentas-api` y `pagos-api`.
- **Amazon MSK** en lugar del contenedor Kafka.
- **AWS Secrets Manager** para credenciales OAuth y claves de firma JWT, en
  reemplazo de `{noop}secret` y de las claves de desarrollo.
- **CloudWatch** para logs, métricas y alarmas.

## 9. Seguridad y costos

- No se guardan claves de AWS ni secretos en el repositorio.
- El `client_secret` `secret` y las claves JWT del auth-server son solo para
  desarrollo/demostración y deben reemplazarse antes de un uso real.
- Los puertos de aplicación están restringidos por Security Group a una IP
  específica; el tráfico es HTTP, por lo que en producción se debe añadir HTTPS
  en un balanceador.
- Al terminar la evaluación, **detener o terminar la instancia** para evitar
  costos. Si se detiene y se vuelve a iniciar, la IP pública cambia y los
  contenedores se levantan con `docker compose up -d`.

## 10. Lecciones aprendidas: troubleshooting del puerto 8085

Durante el despliegue inicial del `batch-service` se detectó un error
`ECONNREFUSED` al llamar al endpoint `POST /api/v1/batch/annual-statements`
desde Postman. El diagnóstico fue el siguiente:

1. `docker ps` mostró que el contenedor estaba mapeado a un puerto aleatorio
   (`0.0.0.0:32769->8085/tcp`) en lugar del 8085 esperado.
2. La causa era que el `docker-compose.yaml` declaraba `ports: - "8085"` sin el
   mapeo explícito host:contenedor.
3. La solución fue cambiar la declaración a `ports: - "8085:8085"`, detener y
   remover el contenedor, y volver a levantarlo con `docker compose up -d
   batch-service`.
4. Se verificó con `docker ps` que el mapeo quedara como
   `0.0.0.0:8085->8085/tcp` y se repitió la prueba en Postman con resultado
   `200 OK` y `status: COMPLETED`.

**Conclusión**: en Docker Compose, cuando se quiere exponer un puerto específico
del contenedor en el mismo puerto del host, siempre se debe declarar en el
formato `"HOST:CONTENEDOR"`. Dejar solo el número hace que Docker asigne un
puerto efímero, lo cual es válido para escalabilidad pero rompe las pruebas
directas contra el puerto conocido.