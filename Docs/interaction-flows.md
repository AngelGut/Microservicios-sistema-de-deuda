# Flujos de Interacción — Debt Manager Microservices

Cada diagrama muestra el flujo de punta a punta para los casos de uso principales del sistema.

---

## Flujo 1: Login de Usuario

El usuario ingresa credenciales. El sistema valida contra la base de datos de `auth-service` y devuelve un JWT.

```mermaid
sequenceDiagram
    actor Usuario
    participant WebUI as web-ui
    participant Gateway as api-gateway
    participant Auth as auth-service
    participant DB as SQLite (auth.db)

    Usuario->>WebUI: POST /auth/login (email, password)
    WebUI->>Gateway: POST /api/v1/auth/login
    Gateway->>Auth: POST /api/v1/auth/login

    Auth->>DB: SELECT * FROM users WHERE email=? AND enabled=true
    DB-->>Auth: Usuario encontrado

    Auth->>Auth: BCrypt.matches(password, hash)

    alt Credenciales válidas
        Auth->>Auth: JwtService.generateToken(userId, email, role)
        Auth-->>Gateway: 200 { token, tokenType, expiresIn, role, email }
        Gateway-->>WebUI: 200 { token, ... }
        WebUI->>WebUI: Guardar token en sesión HTTP
        WebUI-->>Usuario: Redirigir a /dashboard
    else Credenciales inválidas
        Auth-->>Gateway: 401 AUTH_401
        Gateway-->>WebUI: 401
        WebUI-->>Usuario: "Credenciales inválidas"
    end
```

---

## Flujo 2: Crear Deudor y Registrar Deuda

El administrador registra un nuevo deudor y le asigna una deuda con fecha de vencimiento.

```mermaid
sequenceDiagram
    actor Admin
    participant WebUI as web-ui
    participant Gateway as api-gateway
    participant Debtor as debtor-service
    participant Debt as debt-service
    participant DB_D as H2 (debtordb)
    participant DB_Debt as SQLite (debt.db)

    Note over Admin,DB_Debt: Paso 1 — Crear Deudor

    Admin->>WebUI: POST /debtors (nombre, email, teléfono)
    WebUI->>Gateway: POST /api/v1/debtors\n[Authorization: Bearer JWT]
    Gateway->>Debtor: POST /api/v1/debtors

    Debtor->>Debtor: Validar campos
    Debtor->>DB_D: INSERT INTO debtors
    DB_D-->>Debtor: debtorId generado (UUID)
    Debtor-->>Gateway: 201 { success, data: { id, nombre, ... } }
    Gateway-->>WebUI: 201
    WebUI-->>Admin: Redirigir a detalle del deudor

    Note over Admin,DB_Debt: Paso 2 — Crear Deuda

    Admin->>WebUI: POST /debts (debtorId, monto, moneda, fechaVencimiento)
    WebUI->>Gateway: POST /api/v1/debts\n[Authorization: Bearer JWT]
    Gateway->>Debt: POST /api/v1/debts

    Debt->>Debt: Validar monto, fecha, moneda
    Debt->>DB_Debt: INSERT INTO debts (status=PENDIENTE, balancePending=monto)
    DB_Debt-->>Debt: debtId generado (UUID)
    Debt-->>Gateway: 201 { success, data: { id, monto, status, ... } }
    Gateway-->>WebUI: 201
    WebUI-->>Admin: Deuda creada y vinculada al deudor
```

---

## Flujo 3: Registrar Pago (flujo más complejo)

El pago actualiza el saldo de la deuda y dispara una notificación por correo. Incluye circuit breaker.

```mermaid
sequenceDiagram
    actor Admin
    participant WebUI as web-ui
    participant Gateway as api-gateway
    participant Payment as payment-service
    participant Debt as debt-service
    participant Notif as notification-service
    participant Debtor as debtor-service
    participant SMTP as SendGrid/SMTP
    participant DB_P as SQLite (payment.db)
    participant DB_Debt as SQLite (debt.db)

    Admin->>WebUI: POST /payments (debtId, monto, fecha, nota)
    WebUI->>Gateway: POST /api/v1/payments\n[Authorization: Bearer JWT]
    Gateway->>Payment: POST /api/v1/payments

    Payment->>DB_P: INSERT INTO payments
    DB_P-->>Payment: paymentId

    Note over Payment,Debt: Circuit Breaker (Resilience4j)

    Payment->>Debt: POST /api/v1/debts/{debtId}/apply-payment\n{ monto }

    alt Debt-service disponible
        Debt->>DB_Debt: UPDATE debts SET balance_pending -= monto\nSET status = PAGADO si balance = 0
        DB_Debt-->>Debt: OK
        Debt-->>Payment: 200 { deuda actualizada }
    else Debt-service caído (Circuit Breaker OPEN)
        Payment-->>Payment: Fallback: pago registrado,\ndeuda se actualizará después
    end

    Note over Payment,SMTP: Notificación asíncrona

    Payment->>Notif: POST /api/v1/notifications/payment-confirmation\n{ debtId, debtorId, monto }

    Notif->>Debtor: GET /api/v1/debtors/{debtorId}
    Debtor-->>Notif: { nombre, email }

    Notif->>Debt: GET /api/v1/debts/{debtId}
    Debt-->>Notif: { monto original, balance pendiente }

    Notif->>SMTP: Enviar email de confirmación a deudor
    SMTP-->>Notif: Email enviado
    Notif->>Notif: INSERT INTO notification_logs

    Notif-->>Payment: 200 OK
    Payment-->>Gateway: 201 { success, data: { paymentId, ... } }
    Gateway-->>WebUI: 201
    WebUI-->>Admin: Pago registrado ✓
```

---

## Flujo 4: Cálculo de Riesgo del Cliente (IA)

Combina reglas de negocio con análisis de lenguaje natural via Groq (LLM externo). Tiene fallback si Groq no está disponible.

```mermaid
sequenceDiagram
    actor Admin
    participant WebUI as web-ui
    participant Gateway as api-gateway
    participant Risk as ai-risk-service
    participant Debt as debt-service
    participant Payment as payment-service
    participant Groq as Groq AI API
    participant DB_R as SQLite (ai-risk.db)

    Admin->>WebUI: GET /debtors/{id} (vista de deudor)
    WebUI->>Gateway: GET /api/v1/risk/{clientId}
    Gateway->>Risk: GET /risk/{clientId}

    Note over Risk,DB_R: Paso 1 — Verificar caché

    Risk->>DB_R: SELECT * FROM client_risk WHERE client_id=?
    alt Riesgo en caché (calculado recientemente)
        DB_R-->>Risk: Registro existente
        Risk-->>Gateway: 200 { riskLevel, score, recomendaciones }
    else Sin caché o expirado
        DB_R-->>Risk: No encontrado

        Note over Risk,Payment: Paso 2 — Recopilar historial

        Risk->>Debt: GET /api/v1/debts?debtorId={clientId}
        Debt-->>Risk: [ { id, monto, dueDate, status }, ... ]

        loop Por cada deuda
            Risk->>Payment: GET /api/v1/payments?debtId={debtId}
            Payment-->>Risk: [ { monto, paymentDate, note }, ... ]
        end

        Note over Risk: Paso 3 — Reglas de negocio

        Risk->>Risk: Calcular días de mora acumulados\n0 días → GOOD_CLIENT\n1-29 días → LOW_RISK\n≥30 días → HIGH_RISK

        Note over Risk,Groq: Paso 4 — Análisis IA (opcional)

        Risk->>Groq: POST /openai/v1/chat/completions\n{ historial de pagos, días mora }

        alt Groq disponible
            Groq-->>Risk: { riskLevel, aiScore, recomendaciones[] }
            Risk->>DB_R: UPSERT client_risk (nivel reglas + nivel IA)
            Risk-->>Gateway: 200 { riskLevel, aiRiskLevel, aiScore, recommendations }
        else Groq no disponible (fallback)
            Risk->>DB_R: UPSERT client_risk (solo nivel por reglas)
            Risk-->>Gateway: 200 { riskLevel, aiRiskLevel: null }
        end

        Gateway-->>WebUI: 200
        WebUI-->>Admin: Badge de riesgo: 🟢 GOOD / 🟡 LOW / 🔴 HIGH
    end
```

---

## Flujo 5: Recordatorio Automático por Vencimiento

El `notification-service` tiene un scheduler que corre periódicamente y detecta deudas próximas a vencer.

```mermaid
sequenceDiagram
    participant Scheduler as notification-service\n(Scheduled Task)
    participant Debt as debt-service
    participant Debtor as debtor-service
    participant SMTP as SendGrid/SMTP
    participant DB_N as SQLite (notification.db)

    Note over Scheduler: Cron job (configurable, ej. diario)

    Scheduler->>Debt: GET /api/v1/debts?status=PENDIENTE
    Debt-->>Scheduler: [ deudas pendientes con dueDate ]

    loop Por cada deuda próxima a vencer (N días)
        Scheduler->>Debtor: GET /api/v1/debtors/{debtorId}
        Debtor-->>Scheduler: { nombre, email }

        Scheduler->>SMTP: Enviar recordatorio al deudor\n"Tu deuda vence en N días"
        SMTP-->>Scheduler: Email enviado

        Scheduler->>DB_N: INSERT INTO notification_logs\n(tipo=REMINDER, debtId, estado)
    end
```

---

## Resumen de Dependencias entre Servicios

```
web-ui ──────────────► auth-service
       ──────────────► debtor-service
       ──────────────► debt-service
       ──────────────► payment-service
       ──────────────► fx-service
       ──────────────► ai-risk-service

payment-service ─────► debt-service          [Resilience4j Circuit Breaker]
                ─────► notification-service

notification-service ► debtor-service
                     ► debt-service

ai-risk-service ─────► debt-service          [OpenFeign + Circuit Breaker]
                ─────► payment-service        [OpenFeign + Circuit Breaker]
                ─────► Groq API (externo)     [Fallback si no disponible]

fx-service ──────────► ExchangeRate API (externo)
```
