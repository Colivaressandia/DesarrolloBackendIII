# Banco XYZ - Sistema Backend-for-Frontend (BFF)

Proyecto desarrollado para la asignatura **Desarrollo Backend III (PBY2203)**.

El sistema implementa una arquitectura basada en el patrón **Backend for Frontend (BFF)**, cuyo objetivo es desacoplar, optimizar y asegurar el consumo de servicios para tres tipos de clientes diferenciados:

- Navegador Web
- Aplicación Móvil
- Cajeros Automáticos (ATM)

La solución integra y normaliza información proveniente de un **Core Legacy**, adaptando las respuestas y operaciones según las necesidades específicas de cada canal.

---

## 1. Análisis de la Estrategia de Implementación BFF

Para dar cumplimiento a los requerimientos del **Banco XYZ**, se implementó la estrategia de **Endpoints Personalizados y Canales Independientes**, bajo un esquema de **monolito modular escalable**.

Cada canal cuenta con endpoints específicos, diseñados para entregar únicamente la información y funcionalidades necesarias para su respectivo cliente.

### Web BFF

Orientado al consumo desde navegadores web.

Proporciona respuestas completas y estructuradas que incluyen:

- Posición global del cliente.
- Ingresos.
- Egresos.
- Detalle histórico de movimientos.

### Mobile BFF

Diseñado para optimizar el consumo de recursos y ancho de banda en dispositivos móviles.

Las respuestas se limitan a:

- Saldo disponible.
- Titular de la cuenta.
- Tipo de cuenta.
- Últimos 5 movimientos.

Esta estrategia permite reducir el volumen de información transferida y mejorar la eficiencia del consumo desde dispositivos móviles.

### ATM BFF

Orientado a las operaciones realizadas mediante cajeros automáticos.

Implementa operaciones transaccionales críticas como:

- Consulta de saldo.
- Retiro de efectivo.
- Validación de montos y múltiplos.
- Generación de códigos de autorización.

### Seguridad RBAC

El sistema implementa un modelo de control de acceso basado en roles (**RBAC - Role-Based Access Control**) utilizando **Spring Security**.

Cada canal posee credenciales y un rol independiente, permitiendo aislar estrictamente el acceso a sus respectivas rutas.

La autenticación utilizada corresponde a **HTTP Basic**, diferenciada según el canal de consumo.

---

## 2. Requisitos de Ejecución

Para ejecutar el proyecto se requiere contar con los siguientes componentes:

| Componente | Versión |
| :--- | :--- |
| Java Development Kit (JDK) | 17 o superior |
| Apache Maven | 3.8+ |
| MySQL Server | 8.0+ |

También es posible utilizar el **Maven Wrapper** incluido en el proyecto:

```bash
./mvnw

En Windows:

.\mvnw.cmd
3. Matriz de Seguridad y Roles

Cada canal posee un usuario, contraseña y rol específico para controlar el acceso a sus endpoints.

Canal	Usuario	Contraseña	Rol Asignado	Rutas Protegidas
Web	web_user	web123	ROLE_WEB	/api/v1/bff/web/**
Mobile	mobile_user	mobile123	ROLE_MOBILE	/api/v1/bff/mobile/**
ATM	atm_user	atm123	ROLE_ATM	/api/v1/bff/atm/**

La separación de roles permite evitar que un usuario de un canal pueda acceder directamente a los endpoints correspondientes a otro canal.

4. Evidencias de Ejecución y Pruebas de Endpoints

A continuación se presentan ejemplos de consumo de los principales endpoints implementados.

4.1. Canal ATM - Consulta de Saldo

Consulta el saldo de la cuenta utilizando las credenciales correspondientes al canal ATM.

curl.exe -u "atm_user:atm123" http://localhost:8080/api/v1/bff/atm/cuentas/137/saldo
4.2. Canal ATM - Retiro de Efectivo

Para realizar un retiro desde el canal ATM se envía una solicitud POST con los datos de la cuenta, monto y terminal.

Ejemplo utilizando PowerShell:

$cred = New-Object System.Management.Automation.PSCredential ("atm_user", (ConvertTo-SecureString "atm123" -AsPlainText -Force))

$body = '{"cuentaId":137,"monto":2000,"terminalId":"ATM-VALPO-01"}'

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/bff/atm/retiro" `
    -Method Post `
    -Body $body `
    -ContentType "application/json" `
    -Credential $cred | ConvertTo-Json -Depth 5
4.3. Canal Mobile - Resumen de Cuenta

Obtiene un resumen optimizado de la cuenta para el canal móvil.

curl.exe -u "mobile_user:mobile123" http://localhost:8080/api/v1/bff/mobile/cuentas/106/resumen
4.4. Canal Web - Posición Global

Obtiene la posición global de un cliente mediante el canal Web.

curl.exe -u "web_user:web123" "http://localhost:8080/api/v1/bff/web/posicion-global?cliente=Bob%20Johnson"
5. Estructura de Canales

La arquitectura implementada separa los endpoints según el tipo de cliente:

/api/v1/bff/
│
├── web/
│   └── posici​​on-global
│
├── mobile/
│   └── cuentas/{id}/resumen
│
└── atm/
    ├── cuentas/{id}/saldo
    └── retiro

Esta separación permite que cada canal tenga una interfaz adaptada a sus propias necesidades, evitando exponer directamente al cliente las estructuras internas del Core Legacy.

6. Arquitectura General

La comunicación del sistema puede representarse de la siguiente manera:

                    ┌──────────────────┐
                    │   Cliente Web    │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │     Web BFF      │
                    └────────┬─────────┘
                             │
                             │
┌──────────────────┐         ▼
│ Cliente Mobile   │──►┌───────────────┐
└──────────────────┘   │               │
                       │   Core Legacy │
┌──────────────────┐   │               │
│    Cliente ATM   │──►│               │
└──────────────────┘   └───────────────┘
                       ▲
                       │
                 ┌─────┴─────┐
                 │ ATM BFF    │
                 │ Mobile BFF │
                 └───────────┘

Los BFF actúan como una capa intermedia entre los diferentes clientes y el Core Legacy, permitiendo adaptar y controlar la información entregada a cada consumidor.

7. Tecnologías Utilizadas
Java 17+
Spring Boot
Spring Security
Spring Web
Maven
MySQL
HTTP Basic Authentication
RBAC (Role-Based Access Control)
REST API
Backend for Frontend (BFF)
8. Objetivo de la Solución

La implementación del patrón Backend for Frontend permite que el Banco XYZ pueda ofrecer una experiencia diferenciada para cada canal de atención, manteniendo al mismo tiempo un mayor control sobre la seguridad y el consumo de información.

La solución permite:

Desacoplar los clientes del Core Legacy.
Personalizar las respuestas según cada canal.
Reducir el tráfico innecesario, especialmente en dispositivos móviles.
Separar las responsabilidades mediante endpoints específicos.
Controlar el acceso mediante roles.
Centralizar las reglas de seguridad.
Facilitar una futura evolución de los diferentes canales.
9. Conclusión

La arquitectura propuesta permite integrar un sistema bancario Legacy con diferentes canales de consumo mediante una capa Backend for Frontend (BFF).

La separación entre Web BFF, Mobile BFF y ATM BFF permite adaptar las funcionalidades y respuestas a las necesidades particulares de cada cliente, mientras que Spring Security y RBAC proporcionan un mecanismo de control de acceso diferenciado.

De esta manera, la solución cumple con los requerimientos funcionales y de seguridad definidos para el Banco XYZ, manteniendo una arquitectura modular y preparada para futuras extensiones.
