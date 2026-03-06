# Estándares del Sistema – Sistema de Gestión de Deudas con Microservicios

---

# 1. Propósito

Este documento define los estándares técnicos compartidos para el sistema de microservicios.

Su objetivo es asegurar que todos los servicios sigan las mismas convenciones respecto a:

- seguridad JWT
- estructura de respuestas de API
- estructura de errores de API
- trazabilidad de requests mediante TraceId

Todos los servicios deben seguir estos estándares.

---

# 2. Estándar de Seguridad JWT

## 2.1 Header de Autenticación

Todos los endpoints protegidos deben recibir el token JWT usando el siguiente header HTTP:

```
Authorization: Bearer <token>
```

Ejemplo:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 2.2 Claims mínimos del JWT

Cada token emitido debe contener al menos los siguientes claims.

| Claim            | Descripción                         |
| ---------------- | ----------------------------------- |
| sub              | identificador único del usuario     |
| role             | rol asignado al usuario             |
| email o username | identidad del usuario en el sistema |
| exp              | timestamp de expiración del token   |

Ejemplo de payload:

```json
{
  "sub": "123",
  "role": "ADMIN",
  "email": "admin@system.com",
  "exp": 1760000000
}
```

---

## 2.3 Expiración del Token

Tiempo estándar de expiración para los access tokens:

```
30 minutos
```

---

## 2.4 Fallo de Autenticación

Ocurre cuando:

- el token no existe
- el token es inválido
- el token está expirado
- la firma del token es inválida

Respuesta HTTP:

```
401 Unauthorized
```

Ejemplo de respuesta:

```json
{
  "success": false,
  "timestamp": "2026-03-05T20:00:00Z",
  "traceId": "4ec7b46d",
  "error": {
    "code": "AUTH_401",
    "message": "Authentication failed",
    "details": {}
  }
}
```

---

## 2.5 Fallo de Autorización

Ocurre cuando:

- el token es válido
- pero el usuario no tiene permiso para acceder al recurso

Respuesta HTTP:

```
403 Forbidden
```

Ejemplo de respuesta:

```json
{
  "success": false,
  "timestamp": "2026-03-05T20:00:00Z",
  "traceId": "4ec7b46d",
  "error": {
    "code": "AUTH_403",
    "message": "Access denied",
    "details": {}
  }
}
```

---

# 3. Estándar de Respuestas de API

Todas las respuestas exitosas deben usar la estructura definida en `ApiResponse`.

Ejemplo:

```json
{
  "success": true,
  "timestamp": "2026-03-05T20:00:00Z",
  "traceId": "4ec7b46d",
  "data": {
    "id": 1,
    "name": "Example"
  }
}
```

Campos:

| Campo     | Descripción                        |
| --------- | ---------------------------------- |
| success   | indica si la solicitud fue exitosa |
| timestamp | fecha y hora en UTC                |
| traceId   | identificador de la request        |
| data      | información devuelta por la API    |

---

# 4. Estándar de Errores de API

Todas las respuestas de error deben seguir la estructura definida por `ApiErrorResponse`.

Ejemplo:

```json
{
  "success": false,
  "timestamp": "2026-03-05T20:00:00Z",
  "traceId": "4ec7b46d",
  "error": {
    "code": "VAL_400",
    "message": "Validation failed",
    "details": {
      "field": "email"
    }
  }
}
```

Campos:

| Campo         | Descripción                     |
| ------------- | ------------------------------- |
| success       | siempre false                   |
| timestamp     | fecha y hora en UTC             |
| traceId       | identificador de la request     |
| error.code    | código del error                |
| error.message | descripción del error           |
| error.details | detalles adicionales opcionales |

---

## 4.1 Códigos de Error Estándar

| Código   | Significado                |
| -------- | -------------------------- |
| AUTH_401 | fallo de autenticación     |
| AUTH_403 | acceso denegado            |
| VAL_400  | error de validación        |
| RES_404  | recurso no encontrado      |
| INT_500  | error interno del servidor |

---

# 5. Estándar de TraceId

## 5.1 Propósito

TraceId permite rastrear una solicitud a través de múltiples microservicios.

Es esencial para depuración y registro distribuido de logs.

---

## 5.2 Header Estándar

El sistema utiliza el siguiente header para trazabilidad:

```
X-Trace-Id
```

Ejemplo:

```
X-Trace-Id: 4ec7b46d-4a1d
```

---

## 5.3 Reglas de TraceId

- Si el cliente envía `X-Trace-Id`, el servicio debe reutilizarlo.
- Si el header no existe, el servicio debe generar uno nuevo.
- El mismo TraceId debe propagarse entre microservicios.
- El TraceId debe aparecer en:
  - logs
  - respuestas exitosas
  - respuestas de error

---

# 6. Estándar de Librería Compartida (`common-lib`)

El módulo `common-lib` es una librería compartida utilizada por todos los microservicios.

Proporciona infraestructura común y contratos compartidos.

La librería incluye:

```
ApiResponse
ApiErrorResponse
ApiException
GlobalExceptionHandler
TraceIdUtil
TraceIdFilter
TraceIdFilterAutoConfiguration
```

Propósito de la librería:

- asegurar consistencia en respuestas
- asegurar consistencia en errores
- centralizar manejo de excepciones
- proveer trazabilidad de requests

La generación de tokens JWT pertenece a `auth-service`, no a esta librería.

---

# 7. Reglas de Integración para Todos los Servicios

Todos los microservicios deben cumplir las siguientes reglas:

1. Usar el header JWT

```
Authorization: Bearer <token>
```

2. Respetar los claims mínimos del JWT

- sub
- role
- email o username
- exp

3. Devolver respuestas exitosas usando `ApiResponse`.

4. Devolver errores usando `ApiErrorResponse`.

5. Incluir `traceId` en todas las respuestas.

6. Reutilizar o generar `X-Trace-Id` para cada request.

7. Incluir la dependencia compartida:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>common-lib</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

# 8. Inicio Rápido para Desarrolladores

Antes de comenzar un microservicio:

1. Instalar la librería compartida

```
mvn clean install
```

2. Agregar la dependencia `common-lib` al proyecto.

3. Usar las clases compartidas para respuestas y errores.

4. Asegurar que todas las respuestas incluyan `traceId`.
