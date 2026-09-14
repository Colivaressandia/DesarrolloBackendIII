# Banco XYZ - Sistema Backend-for-Frontend (BFF)

Proyecto desarrollado para la asignatura Desarrollo Backend III (PBY2203). Implementa una arquitectura basada en el patron Backend for Frontend (BFF) para desacoplar, optimizar y asegurar el consumo de tres tipos de clientes diferenciados: Navegador Web, Aplicacion Movil y Cajeros Automaticos (ATM), integrando y normalizando informacion proveniente del Core Legacy.

## 1. Analisis de la Estrategia de Implementacion BFF

Para dar cumplimiento a los requerimientos del Banco XYZ, se implemento una estrategia Modular Monolitica con Desacoplamiento por Canales (Single-Deploy Multi-BFF):
* Web BFF: Respuestas completas y estructuradas con la posicion global y detalle historico.
* Mobile BFF: Respuestas ligeras y optimizadas limitadas al saldo y ultimos 5 movimientos.
* ATM BFF: Operaciones transaccionales criticas de retiro y consulta de saldo.
* Core Legacy: Centralizacion del acceso a MySQL y parseo de archivos CSV.
* Seguridad RBAC: Aislamiento estricto de rutas por roles con Spring Security.

## 2. Requisitos de Ejecucion

* Java Development Kit (JDK): Version 17 o superior.
* Apache Maven: 3.8+ (o wrapper ./mvnw incluido).
* Motor de Base de Datos: MySQL Server 8.0+.

## 3. Matriz de Seguridad y Roles

| Canal | Usuario | Contrasena | Rol Asignado | Rutas |
| :--- | :--- | :--- | :--- | :--- |
| Web | web_user | web123 | ROLE_WEB | /api/v1/bff/web/** |
| Mobile | mobile_user | mobile123 | ROLE_MOBILE | /api/v1/bff/mobile/** |
| ATM | atm_user | atm123 | ROLE_ATM | /api/v1/bff/atm/** |

### 4.1. Canal ATM - Consulta de Saldo
curl.exe -u "atm_user:atm123" http://localhost:8080/api/v1/bff/atm/cuentas/137/saldo

### 4.2. Canal ATM - Retiro de Efectivo
$cred = New-Object System.Management.Automation.PSCredential ("atm_user", (ConvertTo-SecureString "atm123" -AsPlainText -Force)); $body = '{"cuentaId":137,"monto":2000,"terminalId":"ATM-VALPO-01"}'; Invoke-RestMethod -Uri "http://localhost:8080/api/v1/bff/atm/retiro" -Method Post -Body $body -ContentType "application/json" -Credential $cred | ConvertTo-Json -Depth 5

### 4.3. Canal Mobile - Resumen de Cuenta
curl.exe -u "mobile_user:mobile123" http://localhost:8080/api/v1/bff/mobile/cuentas/106/resumen
### 4.4. Canal Web - Posicion Global
curl.exe -u "web_user:web123" "http://localhost:8080/api/v1/bff/web/posicion-global?cliente=Bob%20Johnson"

