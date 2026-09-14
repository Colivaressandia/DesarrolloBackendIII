# Sistema de Migración Batch - Banco XYZ

Sistema de migración y procesamiento por lotes para la modernización del ecosistema financiero del Banco XYZ, desarrollado sobre **Spring Boot 3** y **Spring Batch**. El proyecto implementa un modelo de procesamiento resiliente, escalable mediante arquitectura multi-hilo, parametrización externalizada, políticas personalizadas de tolerancia a fallos y agregación contable para fines de auditoría.

---

## 1. Arquitectura y Estructura del Proyecto

El proyecto sigue una arquitectura modular en capas bajo el paquete base `cl.duoc.bancoxyz`:

- `config/`: Infraestructura batch, pool de hilos y configuración de los 3 Jobs.
- `dto/`: Objetos de transferencia de datos para el mapeo inicial de archivos CSV.
- `listener/`: `StepMetricsListener` para auditoría detallada de métricas de ejecución.
- `model/`: Entidades JPA mapeadas a las tablas relacionales en MySQL (`Transaccion`, `InteresCuenta`, `CuentaAnual`, `ResumenCuentaAnual`).
- `policy/`: Políticas personalizadas `CustomSkipPolicy` y `CustomRetryPolicy`.
- `processor/`: Lógica de validación, reglas de negocio y detección de anomalías.
- `repository/`: Repositorios Spring Data JPA para persistencia transaccional.
- `tasklet/`: `CuentasAnualesAggregationTasklet` para el Step 2 de balance consolidado.

---

## 2. Descripción de los Procesos Batch

### Proceso 1: Reporte de Transacciones Diarias (`transaccionesJob`)
- **Entrada:** `data/transacciones.csv`
- **Lógica:** Validación de montos y detección de inconsistencias lógicas. Asigna estados: `VALIDA`, `ANOMALIA_MONTO_NEGATIVO` o `ANOMALIA_MONTO_CERO`.
- **Persistencia:** Tabla `reporte_transacciones`.
- **Escalamiento:** Procesamiento en paralelo con 3 hilos y chunks de 5 elementos.

### Proceso 2: Cálculo de Intereses Mensuales (`interesesJob`)
- **Entrada:** `data/intereses.csv`
- **Lógica:** Cálculo dinámico de tasa y rendimiento según el tipo de producto:
  - Ahorro: Tasa 0.05 (5%)
  - Préstamo: Tasa 0.08 (8%)
  - Hipoteca / Otros: Tasa 0.035 (3.5%)
- **Persistencia:** Tabla `intereses_calculados` (registra saldo inicial, interés y saldo final).
- **Escalamiento:** Pool concurrente de 3 hilos y chunks de 5 elementos.

### Proceso 3: Generación de Estados de Cuenta Anuales (`cuentasAnualesJob`)
- **Entrada:** `data/cuentas_anuales.csv`
- **Step 1 (`cuentasAnualesStep`):** Lectura concurrente, clasificación contable (`INGRESO`, `EGRESO`, `MONTO_NULO`) y persistencia en `estados_cuenta_anual`.
- **Step 2 (`cuentasAnualesResumenStep`):** Ejecución de `CuentasAnualesAggregationTasklet`. Agrupa los movimientos por `cuenta_id`, calcula el total de ingresos, egresos, balance neto y categoriza el estado para auditoría en `SUPERAVIT` o `DEFICIT`, persistiendo en la tabla `resumen_anual_cuentas`.

---

## 3. Escalamiento, Rendimiento y Comparación de Parámetros

Para optimizar el rendimiento sin comprometer la integridad transaccional ni saturar la base de datos, se configuró un modelo de **Multi-threading** respaldado por `SynchronizedItemStreamReader` para asegurar operaciones *thread-safe*.

### Comparativa de Configuraciones (Benchmark)

| Configuración | N° Hilos | Chunk Size | Tiempo de Ejecución Total | Evaluación Técnica |
| :--- | :---: | :---: | :---: | :--- |
| **Monohilo Secuencial** | 1 | 1 | 1,450 ms | Alto overhead transaccional por exceso de commits individuales y nula concurrencia. |
| **Concurrente Óptima Seleccionada** | **3** | **5** | **817 ms** | **Configuración óptima:** Reducción del 43% en tiempo, uso eficiente de CPU y conexiones JDBC balanceadas. |
| **Concurrente Sobredimensionada** | 10 | 20 | 890 ms | Pérdida de eficiencia por *context switching* excesivo y contención de bloqueos a nivel de base de datos. |

---

## 4. Tolerancia a Fallos, Resiliencia y Reejecución

- **Omisiones Controladas (`CustomSkipPolicy`):** Gestiona excepciones de parseo numérico (`NumberFormatException`), formato CSV (`FlatFileParseException`) y argumentos inválidos (`IllegalArgumentException`) hasta un límite de 5 omisiones por Step.
- **Reintentos Automáticos (`CustomRetryPolicy`):** Aplica hasta 3 reintentos ante fallos transitorios de infraestructura (`TransientDataAccessException`, `SQLException`, `SocketTimeoutException`).
- **Estrategia de Reejecución:** Integración de `RunIdIncrementer` formal en cada Job para garantizar secuencias trazables e incrementales en los metadatos de Spring Batch.
- **Auditoría de Métricas (`StepMetricsListener`):** Reporte en log de registros leídos, escritos, omitidos (skips), commits y rollbacks al cierre de cada Step.

---

## 5. Configuración Externalizada (`application.properties`)

```properties
# Base de Datos MySQL
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/banco_xyz_batch?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:root1234}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Spring Batch
spring.batch.jdbc.initialize-schema=always
spring.batch.job.enabled=false

# Parámetros de Tuning y Resiliencia
batch.tuning.chunk-size=5
batch.tuning.core-pool-size=3
batch.tuning.max-pool-size=5
batch.tuning.queue-capacity=20
batch.tuning.thread-prefix=banco-thread-
batch.tuning.max-skip-count=5
batch.tuning.max-retry-attempts=3


