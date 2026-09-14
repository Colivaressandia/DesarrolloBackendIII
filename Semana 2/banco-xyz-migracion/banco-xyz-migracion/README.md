Sistema de Migracion Batch - Banco XYZ
1. Descripcion General del Proyecto
Modernizacion del sistema legacy del Banco XYZ mediante Spring Boot 3 y Spring Batch 5. La solucion procesa, valida y migra datos historicos distribuidos en archivos CSV hacia un modelo relacional en MySQL, garantizando integridad referencial, alta concurrencia y resiliencia ante errores de entrada.

2. Procesos Batch Implementados
Proceso 1: Reporte de Transacciones Diarias (transaccionesJob)

Lee transacciones.csv.

Detecta anomalias de negocio (montos superiores a $10,000) y clasifica el estado en SOSPECHOSA o NORMAL.

Persiste los registros en la tabla reporte_transacciones.

Proceso 2: Calculo de Intereses Mensuales (interesesJob)

Lee intereses.csv.

Valida coherencia de datos (edad minima laboral y saldos no negativos) y calcula la rentabilidad o costo financiero mensual segun el producto (ahorro, prestamo, hipoteca).

Persiste los registros en la tabla intereses_calculados.

Proceso 3: Generacion de Estados de Cuenta Anuales (cuentasAnualesJob)

Lee cuentas_anuales.csv.

Asigna niveles de clasificacion para auditoria (OPERACION MAYOR, CARGO RELEVANTE, OPERACION ESTANDAR).

Persiste los registros en la tabla estados_cuenta_anual.

3. Arquitectura y Optimizaciones (Semana 2)
Escalamiento Multihilo:

Implementacion de ThreadPoolTaskExecutor configurado con 3 hilos paralelos (setCorePoolSize(3), setMaxPoolSize(3)).

Procesamiento por lotes en chunks de tamano 5 (.chunk(5, transactionManager)).

Thread-Safety en Lectura:

Envoltura de FlatFileItemReader con SynchronizedItemStreamReader para asegurar la sincronizacion de lectura y evitar condiciones de carrera entre hilos concurrentes.

Tolerancia a Fallos y Reintentos:

CustomSkipPolicy: Politica personalizada que tolera hasta 5 registros con errores de casteo o parseo (FlatFileParseException, NumberFormatException) sin abortar el proceso.

CustomRetryPolicy: Manejo de hasta 3 reintentos ante excepciones transitorias de conexion o bloqueos en base de datos.

4. Estructura del Proyecto
Plaintext
banco-xyz-migracion/
├── src/main/java/cl/duoc/bancoxyz/
│   ├── BancoXyzApplication.java
│   ├── config/
│   │   ├── BatchConfig.java
│   │   ├── TransaccionesJobConfig.java
│   │   ├── InteresesJobConfig.java
│   │   └── CuentasAnualesJobConfig.java
│   ├── dto/
│   │   ├── TransaccionDTO.java
│   │   ├── InteresDTO.java
│   │   └── CuentaAnualDTO.java
│   ├── model/
│   │   ├── Transaccion.java
│   │   ├── InteresCuenta.java
│   │   └── CuentaAnual.java
│   ├── policy/
│   │   ├── CustomSkipPolicy.java
│   │   └── CustomRetryPolicy.java
│   ├── processor/
│   │   ├── TransaccionProcessor.java
│   │   ├── InteresProcessor.java
│   │   └── CuentaAnualProcessor.java
│   └── repository/
│       ├── TransaccionRepository.java
│       ├── InteresCuentaRepository.java
│       └── CuentaAnualRepository.java
├── src/main/resources/
│   ├── data/
│   │   ├── transacciones.csv
│   │   ├── intereses.csv
│   │   └── cuentas_anuales.csv
│   └── application.properties
└── pom.xml
5. Requisitos de Entorno
Java Development Kit (JDK) 17 o superior.

Apache Maven 3.9+.

MySQL Server 8.0+.

6. Configuracion y Base de Datos
Crea la base de datos previa ejecucion:

SQL
CREATE DATABASE IF NOT EXISTS banco_xyz_batch;
Ajusta tus credenciales en src/main/resources/application.properties o mediante variables de entorno:

Properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/banco_xyz_batch?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.batch.jdbc.initialize-schema=always
7. Instrucciones de Compilacion y Ejecucion
Situarse en la raiz del proyecto Maven:

PowerShell
cd banco-xyz-migracion
Compilar y ejecutar la aplicacion:

PowerShell
mvn clean spring-boot:run
Verificar los datos persistidos en MySQL:

SQL
USE banco_xyz_batch;
SELECT * FROM reporte_transacciones;
SELECT * FROM intereses_calculados;
SELECT * FROM estados_cuenta_anual;