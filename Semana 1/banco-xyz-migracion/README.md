Sistema de Migración de Procesos Batch - Banco XYZ
1. Descripción del Proyecto
Este proyecto implementa la modernización y migración técnica de los procesos por lotes (batch) del sistema legacy de Banco XYZ, utilizando el ecosistema de Spring Boot 3, Spring Batch 5, Spring Data JPA y un motor relacional MySQL 8.0.

El sistema procesa, valida, transforma y persiste tres flujos de información a partir de archivos planos en formato CSV:

Reporte de Transacciones Diarias (transaccionesJob): Ingesta transacciones, aplica normalización de tipos y audita inconsistencias detectando anomalías (montos negativos o en cero).

Cálculo de Intereses Mensuales (interesesJob): Aplica tasas de interés financiero diferenciadas según el tipo de producto bancario (ahorro: 5%, préstamo: 8%, hipoteca: 3.5%), filtra saldos inválidos y persiste el saldo final recalculado.

Generación de Estados de Cuenta Anuales (cuentasAnualesJob): Consolida los movimientos anuales de cuentas y genera clasificaciones de auditoría contable (INGRESO, EGRESO, MONTO_NULO).

2. Arquitectura de la Solución
El diseño se basa en el patrón estándar de Spring Batch (Reader -> Processor -> Writer) estructurado por capas y ejecutado secuencialmente al iniciar la aplicación:

Plaintext
src/main/java/cl/duoc/bancoxyz/
├── BancoXyzApplication.java         # Entrada y orquestador secuencial (CommandLineRunner)
├── config/                          # Configuración de Jobs, Steps y Listeners
│   ├── BatchConfig.java
│   ├── TransaccionesJobConfig.java
│   ├── InteresesJobConfig.java
│   └── CuentasAnualesJobConfig.java
├── dto/                             # Data Transfer Objects para lectura de CSV
│   ├── TransaccionDTO.java
│   ├── InteresDTO.java
│   └── CuentaAnualDTO.java
├── model/                           # Entidades JPA mapeadas a tablas MySQL
│   ├── Transaccion.java
│   ├── InteresCuenta.java
│   └── CuentaAnual.java
├── processor/                       # Lógica de negocio, validaciones y reglas
│   ├── TransaccionProcessor.java
│   ├── InteresProcessor.java
│   └── CuentaAnualProcessor.java
└── repository/                      # Repositorios JPA para persistencia
    ├── TransaccionRepository.java
    ├── InteresCuentaRepository.java
    └── CuentaAnualRepository.java
3. Requisitos Previos
Java Development Kit (JDK): Versión 17 o superior

Motor de Base de Datos: MySQL 8.0

Gestor de Construcción: Apache Maven (incluido con Maven Wrapper mvnw)

4. Configuración de Base de Datos
El proyecto incluye auto-creación de esquema mediante createDatabaseIfNotExist=true en src/main/resources/application.properties:

Properties
spring.datasource.url=jdbc:mysql://localhost:3306/banco_xyz_batch?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.batch.jdbc.initialize-schema=always
spring.batch.job.enabled=false
5. Instrucciones de Ejecución
Clonar el repositorio:

Bash
git clone <URL_DEL_REPOSITORIO>
cd migracion-batch
Compilar y ejecutar:

En Windows PowerShell / VS Code:

PowerShell
./mvnw clean spring-boot:run
En CMD / Terminal:

DOS
mvnw clean spring-boot:run
6. Verificación de Resultados en MySQL
Para auditar la persistencia de los tres procesos en la base de datos, ejecuta las siguientes consultas en la consola de MySQL:

SQL
USE banco_xyz_batch;

-- 1. Verificar transacciones y detección de anomalías
SELECT * FROM reporte_transacciones;

-- 2. Verificar cuentas con interés calculado y saldo final
SELECT * FROM intereses_calculados;

-- 3. Verificar estados de cuenta y clasificación de auditoría
SELECT * FROM estados_cuenta_anual;
7. Manejo de Errores y Validaciones Aplicadas
Filtrado de Registros Nulos: Si un registro en intereses.csv contiene saldo menor o igual a cero, el procesador retorna null, omitiendo la fila sin interrumpir el lote.

Trazabilidad de Anomalías: En transacciones.csv, los montos menores o iguales a cero no se descartan, sino que se etiquetan explícitamente como ANOMALIA_MONTO_NEGATIVO o ANOMALIA_MONTO_CERO para su posterior auditoría.

Transaccionalidad por Chunks: Cada Step procesa lotes de 5 en 5 elementos con control transaccional garantizado por PlatformTransactionManager.