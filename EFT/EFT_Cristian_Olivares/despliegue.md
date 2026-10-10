# Guía de despliegue en AWS — ECS Fargate

## Alcance y arquitectura elegida

Las instrucciones de la EFT exigen preparar un despliegue en AWS, pero no fijan
un servicio específico. Para este proyecto se propone **Amazon ECS Fargate**:
ECR almacena las imágenes; un Application Load Balancer (ALB) publica los tres
BFF; ECS Service Discovery/Eureka permite las llamadas internas; MSK reemplaza
el broker Kafka local; RDS for PostgreSQL entrega persistencia administrada;
y CloudWatch centraliza logs y métricas.

`docker compose up` es solo el entorno local. No constituye un despliegue en
AWS ni configura por sí solo HTTPS, balanceo externo, secretos o escalado.

## 1. Requisitos de la cuenta y red

Se necesita una cuenta AWS con permisos para ECR, ECS, EC2/VPC, ELB, IAM,
Secrets Manager, CloudWatch, RDS y MSK. Configura AWS CLI con un perfil de
credenciales local y una región:

```powershell
$AWS_REGION = "us-east-1"
$AWS_ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
```

No guardes claves de AWS, secretos OAuth ni contraseñas en el repositorio.

Prepara:

1. Una VPC con subredes públicas para el ALB y al menos dos subredes privadas
   para las tareas Fargate, RDS y MSK.
2. Security groups que permitan HTTPS al ALB; tráfico desde el ALB solo a los
   puertos de los BFF; y tráfico privado entre microservicios, Eureka, Config
   Server, RDS y MSK según necesidad.
3. Un certificado ACM para el nombre DNS público y un dominio apuntado al ALB.
4. Un clúster ECS con roles de ejecución de tareas y de aplicación de mínimo
   privilegio.
5. Un clúster MSK accesible desde las subredes privadas.
6. Un RDS for PostgreSQL accesible solo desde las tareas autorizadas.
7. Secretos en Secrets Manager para contraseña de RDS, credenciales OAuth y
   claves de firma JWT.

Los microservicios incluyen el driver PostgreSQL. Configura
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y
`SPRING_DATASOURCE_PASSWORD` en las tareas mediante referencias a secretos.
Los perfiles Docker Compose mantienen H2 para desarrollo. Antes de una
operación bancaria real, completa el cambio de esquema y una migración de datos
controlada; no uses H2 en memoria como persistencia de producción.

## 2. Publicar imágenes de contenedor en ECR

Crea un repositorio ECR por cada imagen desplegable:

```powershell
$services = @(
  "eureka-server", "config-server", "auth-server", "bank-api",
  "cuentas-api", "clientes-api", "intereses-api", "pagos-api",
  "batch-service", "bff-web", "bff-mobile", "bff-atm"
)

foreach ($service in $services) {
  aws ecr create-repository --repository-name $service --region $AWS_REGION
}

$ECR = "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
aws ecr get-login-password --region $AWS_REGION |
  docker login --username AWS --password-stdin $ECR

foreach ($service in $services) {
  docker build -t "$ECR/$service:eft-s9" ".\$service"
  docker push "$ECR/$service:eft-s9"
}
```

Si ECR informa que el repositorio ya existe, conserva el repositorio creado y
continúa con el login/build/push. Para entrega reproducible, usa tags
inmutables derivados del commit en vez de publicar siempre `eft-s9`.

## 3. Configurar ECS y el ALB

1. Crea un servicio ECS Fargate por componente; usa imágenes ECR y subredes
   privadas. Config Server, Eureka, auth-server, APIs y broker no deben
   publicarse directamente a Internet.
2. Configura health checks con `/actuator/health` para los servicios Spring.
   El ALB debe enviar tráfico solo a BFF saludables.
3. Crea target groups separados para `bff-web` (8091), `bff-mobile` (8092) y
   `bff-atm` (8093). Usa listener HTTPS 443 con el certificado ACM y reglas por
   host o path. Redirige HTTP 80 a HTTPS.
4. Configura variables de entorno para que las APIs y los BFF usen las URLs
   internas correctas de Config Server, Eureka y MSK. Las URLs
   `localhost`/`kafka:9092` de Compose no aplican en AWS.
5. Conserva la configuración de jobs y eventos; configura el Batch service
   para leer los CSV desde un volumen EFS montado como
   `/app/data/legacy` y guardar informes en un volumen durable o enviarlos a
   S3. El directorio local `batch-service/output` no persiste al reemplazar una
   tarea Fargate.
6. Para el acceso interno, configura Eureka con nombres de host privados y
   health checks. Para alta disponibilidad de producción, despliega y valida
   Eureka/Config Server con redundancia; la configuración de referencia actual
   del proyecto está pensada para una topología didáctica pequeña.
7. Revisa la configuración del Authorization Server: el secreto `{noop}secret`
   y las claves de desarrollo no son adecuados para producción. Inyecta
   secretos desde Secrets Manager y configura certificados/llaves de firma
   durables y rotables. No expongas el issuer interno sin una URL pública y
   estrategia de validación compatibles con los clientes.

## 4. Despliegue, verificación y escalado

Despliega primero la infraestructura compartida (RDS, MSK, Eureka y Config
Server), luego auth-server, APIs y finalmente los BFF. En ECS, actualiza cada
servicio a la nueva revisión de task definition y espera el estado `RUNNING`.

Verifica:

- Targets saludables y HTTPS del ALB.
- Registro/disponibilidad de instancias internas.
- OAuth 2.0/JWT, permisos por canal y ausencia de endpoints backend públicos.
- Conexión a PostgreSQL y MSK desde subred privada.
- Eventos de pagos consumidos y outbox sin acumulación.
- Ejecución de los tres jobs Spring Batch y persistencia de salidas.
- Logs, métricas, alarmas y trazabilidad en CloudWatch.

Habilita ECS Service Auto Scaling por CPU/memoria y métricas de solicitudes del
ALB; define mínimos y máximos por servicio. Para los BFF, escala cada canal por
separado. No escales réplicas con H2 en memoria: usa PostgreSQL administrado y
verifica que las operaciones idempotentes y el outbox funcionen con varias
tareas.

## 5. Límites de esta guía

Esta guía documenta la arquitectura y los pasos de preparación, pero no crea
recursos AWS ni realiza un despliegue. La cuenta, región, permisos, DNS,
certificado, red y presupuesto deben ser provistos y aprobados por el titular.
Tampoco reemplaza una revisión de seguridad, gestión de datos personales o
validación de reglas financieras antes de producción.
