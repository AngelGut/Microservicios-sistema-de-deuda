# Arquitectura del Sistema — Debt Manager Microservices

## Diagrama General

```mermaid
graph TB
    Browser["🌐 Browser / Cliente HTTP"]

    subgraph Gateway["API Gateway  :8080"]
        GW["Spring Cloud Gateway\nWebFlux"]
    end

    subgraph Core["Servicios Core"]
        AUTH["auth-service :8081\nAutenticación + Usuarios"]
        DEBTOR["debtor-service :8082\nGestión de Deudores"]
        DEBT["debt-service :8083\nGestión de Deudas"]
        PAYMENT["payment-service :8084\nRegistro de Pagos"]
    end

    subgraph ValueAdd["Servicios de Valor Añadido"]
        NOTIF["notification-service :8085\nCorreos + Recordatorios"]
        RISK["ai-risk-service :8086\nAnálisis de Riesgo IA"]
        FX["fx-service :8088\nTasa de Cambio USD/DOP"]
    end

    subgraph UI["Interfaz Web"]
        WEB["web-ui :8090\nThymeleaf SSR"]
    end

    subgraph External["APIs Externas"]
        GROQ["Groq AI API\nAnálisis crediticio LLM"]
        EXRATE["ExchangeRate API\nTasas de cambio"]
        SMTP["SMTP / SendGrid\nEnvío de correos"]
    end

    subgraph DBs["Bases de Datos"]
        DB_AUTH[("SQLite\nauth.db")]
        DB_DEBT[("SQLite\ndebt.db")]
        DB_PAY[("SQLite\npayment.db")]
        DB_RISK[("SQLite\nai-risk.db")]
        DB_NOTIF[("SQLite\nnotification.db")]
        DB_DEBTOR[("SQLite\ndebtor.db")]
    end

    Browser -->|"HTTP/HTTPS"| GW
    GW -->|"/api/v1/auth/**"| AUTH
    GW -->|"/api/v1/debtors/**"| DEBTOR
    GW -->|"/api/v1/debts/**"| DEBT
    GW -->|"/api/v1/payments/**"| PAYMENT
    GW -->|"/api/v1/risk/**"| RISK
    GW -->|"/api/v1/fx/**"| FX
    GW -->|"/api/v1/notifications/**"| NOTIF
    GW -->|"/** (vistas)"| WEB

    WEB -->|"Login / Validate JWT"| AUTH
    WEB -->|"CRUD Deudores"| DEBTOR
    WEB -->|"CRUD Deudas"| DEBT
    WEB -->|"CRUD Pagos"| PAYMENT
    WEB -->|"Tasa FX"| FX
    WEB -->|"Nivel de riesgo"| RISK

    PAYMENT -->|"apply-payment\n+ Resilience4j CB"| DEBT
    PAYMENT -->|"Confirmar pago"| NOTIF

    NOTIF -->|"Datos del deudor"| DEBTOR
    NOTIF -->|"Datos de deuda"| DEBT
    NOTIF -->|"Envío de correo"| SMTP

    RISK -->|"Historial de deudas"| DEBT
    RISK -->|"Historial de pagos"| PAYMENT
    RISK -->|"Análisis LLM"| GROQ

    FX -->|"Tasas de cambio"| EXRATE

    AUTH --- DB_AUTH
    DEBT --- DB_DEBT
    PAYMENT --- DB_PAY
    RISK --- DB_RISK
    NOTIF --- DB_NOTIF
    DEBTOR --- DB_DEBTOR
```

---

## Inventario de Servicios

| Servicio | Puerto | Responsabilidad | Base de Datos | Llama a |
|----------|--------|-----------------|---------------|---------|
| **api-gateway** | 8080 | Enrutamiento, punto único de entrada | — | Todos los servicios |
| **auth-service** | 8081 | Autenticación JWT + gestión de usuarios del sistema | SQLite | — |
| **debtor-service** | 8082 | CRUD de deudores (personas con deudas) | SQLite | — |
| **debt-service** | 8083 | CRUD de deudas, KPIs del dashboard | SQLite | — |
| **payment-service** | 8084 | Registro de pagos, historial | SQLite | debt-service, notification-service |
| **notification-service** | 8085 | Emails de confirmación y recordatorios por vencimiento | SQLite | debtor-service, debt-service |
| **ai-risk-service** | 8086 | Calcula nivel de riesgo (reglas + IA Groq) | SQLite | debt-service, payment-service, Groq API |
| **fx-service** | 8088 | Conversión de moneda USD ↔ DOP | — | ExchangeRate API |
| **web-ui** | 8090 | Interfaz web Thymeleaf SSR | — | auth, debtor, debt, payment, fx, ai-risk |

---

## Patrones Técnicos

| Patrón | Donde se aplica |
|--------|----------------|
| **JWT Auth** | Todos los servicios validan `Authorization: Bearer <token>` |
| **Circuit Breaker (Resilience4j)** | payment-service → debt-service |
| **OpenFeign** | ai-risk-service, notification-service, web-ui |
| **Flyway Migrations** | Todos los servicios con SQLite/H2 |
| **TraceId propagation** | Todos los servicios via `X-Trace-Id` header |
| **Actuator Health** | `/actuator/health` en cada servicio |
| **Swagger / OpenAPI** | `/swagger-ui/index.html` en cada servicio |

---

## Estructura de Respuesta Estándar

Todas las APIs siguen el mismo contrato definido en `common-lib`:

```json
// Éxito
{
  "success": true,
  "timestamp": "2026-03-16T15:00:00Z",
  "traceId": "4ec7b46d",
  "data": { ... }
}

// Error
{
  "success": false,
  "timestamp": "2026-03-16T15:00:00Z",
  "traceId": "4ec7b46d",
  "error": {
    "code": "AUTH_401",
    "message": "Authentication failed",
    "details": {}
  }
}
```
