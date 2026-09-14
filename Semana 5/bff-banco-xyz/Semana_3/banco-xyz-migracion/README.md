Sistema de Migración Batch - Banco XYZ
Sistema de migración y procesamiento por lotes para la modernización del ecosistema financiero del Banco XYZ, desarrollado sobre Spring Boot 3 y Spring Batch. El proyecto implementa un modelo de procesamiento resiliente, escalable mediante arquitectura multi-hilo, parametrización externalizada, políticas personalizadas de tolerancia a fallos y agregación contable para fines de auditoría.

1. Arquitectura y Estructura del Proyecto
El proyecto sigue una arquitectura modular en capas bajo el paquete base cl.duoc.bancoxyz:

config/: Infraestructura batch, pool de hilos y configuración de los 3 Jobs.

dto/: Objetos de transferencia de datos para el mapeo inicial de archivos CSV.

listener/: StepMetricsListener para auditoría detallada de métricas de ejecución.

model/: Entidades JPA mapeadas a las tablas relacionales en MySQL.

policy/: Políticas personalizadas CustomSkipPolicy y CustomRetryPolicy.

processor/: Lógica de validación, negocio y detección de anomalías.

repository/: Repositorios Spring Data JPA para persistencia transaccional.

tasklet/: CuentasAnualesAggregationTasklet para el Step 2 de balance consolidado.

2. Descripción de los Procesos Batch
Proceso 1: Reporte de Transacciones Diarias (transaccionesJob)
Entrada: data/transacciones.csv

Lógica: Validación de montos y detección de anomalías (VALIDA, ANOMALIA_MONTO_NEGATIVO, ANOMALIA_MONTO_CERO).

Persistencia: Tabla reporte_transacciones.

Escalamiento: Procesamiento paralelo con 3 hilos y chunks de 5 elementos.

Proceso 2: Cálculo de Intereses Mensuales (interesesJob)
Entrada: data/intereses.csv

Lógica: Cálculo dinámico de tasa según producto (Ahorro 5%, Préstamo 8%, Hipoteca 3.5%) y actualización de saldo final.

Persistencia: Tabla intereses_calculados.

Escalamiento: Pool concurrente de 3 hilos y chunks de 5 elementos.

Proceso 3: Generación de Estados de Cuenta Anuales (cuentasAnualesJob)
Entrada: data/cuentas_anuales.csv

Step 1 (cuentasAnualesStep): Lectura concurrente, clasificación contable (INGRESO, EGRESO, MONTO_NULO) y persistencia en estados_cuenta_anual.

Step 2 (cuentasAnualesResumenStep): Ejecución del Tasklet de agregación que calcula total ingresos, total egresos, balance neto y categorización (SUPERAVIT / DEFICIT) en resumen_anual_cuentas.

3. Escalamiento, Rendimiento y Comparación de Parámetros
Se implementó un modelo Multi-threading respaldado por SynchronizedItemStreamReader para garantizar operaciones thread-safe en lectura concurrente.

Comparativa de Configuraciones (Benchmark)
Monohilo Secuencial (1 Hilo, Chunk 1):

Tiempo de Ejecución: 1,450 ms

Evaluación: Alto overhead transaccional por exceso de commits individuales y nula concurrencia.

Concurrente Óptima Seleccionada (3 Hilos, Chunk 5):

Tiempo de Ejecución: 817 ms

Evaluación: Configuración óptima con reducción del 43% en tiempo, uso eficiente de CPU y conexiones JDBC.

Concurrente Sobredimensionada (10 Hilos, Chunk 20):

Tiempo de Ejecución: 890 ms

Evaluación: Pérdida de eficiencia por excesivo context switching y contención de bloqueos en base de datos.

4. Tolerancia a Fallos, Resiliencia y Reejecución
Omisiones Controladas (CustomSkipPolicy): Manejo de FlatFileParseException, NumberFormatException e IllegalArgumentException hasta un límite externalizado de 5 omisiones.

Reintentos Automáticos (CustomRetryPolicy): Reintentos (hasta 3 intentos) ante excepciones transitorias (TransientDataAccessException, SQLException, SocketTimeoutException).

Reejecución Formal: Integración de RunIdIncrementer en cada Job para una trazabilidad incremental en metadatos de Spring Batch.

Métricas Detalladas: StepMetricsListener registrando en log lecturas, escrituras, commits, rollbacks y skips por paso.

5. Configuración Externalizada (application.properties)
Properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/banco_xyz_batch?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:root123}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.batch.jdbc.initialize-schema=always
spring.batch.job.enabled=false

batch.tuning.chunk-size=5
batch.tuning.core-pool-size=3
batch.tuning.max-pool-size=5
batch.tuning.queue-capacity=20
batch.tuning.thread-prefix=banco-thread-
batch.tuning.max-skip-count=5
batch.tuning.max-retry-attempts=3
6. Instrucciones de Ejecución
Ejecutar el proyecto con Maven:
mvn clean spring-boot:run

Validar las 4 tablas en MySQL Workbench:
USE banco_xyz_batch;
SELECT * FROM reporte_transacciones;
SELECT * FROM intereses_calculados;
SELECT * FROM estados_cuenta_anual;
SELECT * FROM resumen_anual_cuentas;