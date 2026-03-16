# Resiliencia y Fallbacks — Debt Manager Microservices

## ¿Por qué resiliencia?

En un sistema de microservicios, los servicios pueden fallar de forma independiente. Sin mecanismos de resiliencia, el fallo de un servicio se **propaga en cascada** y derriba todo el sistema. Este sistema implementa tres estrategias defensivas.

---

## Estrategia 1: Circuit Breaker (payment-service → debt-service)

Es el patrón de resiliencia más importante del sistema. Protege `payment-service` cuando `debt-service` no está disponible.

### Estados del Circuit Breaker

```
                    ┌─────────────────────────────────────┐
                    │                                     │
         Éxitos     ▼          ≥50% fallos en 5 calls     │
         normales  CERRADO ──────────────────────────► ABIERTO
                    ▲                                     │
                    │          Espera 10 segundos         │
                    │                                     ▼
                    │          2 calls de prueba       SEMI-ABIERTO
                    │          exitosas                   │
                    └─────────────────────────────────────┘
```

| Estado | Comportamiento |
|--------|---------------|
| **CERRADO** | Funcionamiento normal, todas las llamadas pasan |
| **ABIERTO** | Todas las llamadas van directo al fallback (sin intentar) |
| **SEMI-ABIERTO** | Deja pasar 2 llamadas de prueba para verificar recuperación |

### Configuración real (payment-service)

```properties
# Ventana deslizante: evalúa las últimas 5 llamadas
resilience4j.circuitbreaker.instances.debtClient.slidingWindowSize=5

# Umbral de fallo: si ≥50% fallan → abre el circuito
resilience4j.circuitbreaker.instances.debtClient.failureRateThreshold=50

# Tiempo en estado ABIERTO antes de pasar a SEMI-ABIERTO
resilience4j.circuitbreaker.instances.debtClient.waitDurationInOpenState=10s

# Llamadas de prueba en SEMI-ABIERTO
resilience4j.circuitbreaker.instances.debtClient.permittedNumberOfCallsInHalfOpenState=2
```

### Comportamiento del fallback

Cuando `debt-service` no está disponible:

```
payment-service recibe:  POST /api/v1/payments { debtId, monto }
        │
        ▼
  Guarda el pago en SQLite ✅  (el pago SIEMPRE se registra)
        │
        ▼
  Intenta: POST /api/v1/debts/{debtId}/apply-payment
        │
        ├─ Circuit CERRADO → debt-service actualiza saldo ✅
        │
        └─ Circuit ABIERTO → Fallback activado:
              "Pago registrado. El balance se actualizará
               cuando debt-service esté disponible."
              Devuelve 201 con el pago registrado ✅
```

**El pago nunca se pierde.** El usuario recibe confirmación. La deuda se sincronizará después.

### Retry incluido

Antes de abrir el circuito, el sistema reintenta automáticamente:

```properties
resilience4j.retry.instances.debtClient.maxAttempts=3
resilience4j.retry.instances.debtClient.waitDuration=500ms
```

Secuencia real ante un fallo:
```
Intento 1 → Falla → Espera 500ms
Intento 2 → Falla → Espera 500ms
Intento 3 → Falla → Activa fallback
```

---

## Estrategia 2: Circuit Breaker en ai-risk-service (OpenFeign)

`ai-risk-service` usa OpenFeign con circuit breaker para llamar a `debt-service` y `payment-service`.

### Configuración real (ai-risk-service)

```properties
# Activa CB en todos los clientes Feign
spring.cloud.openfeign.circuitbreaker.enabled=true

# Mismos parámetros que payment-service
resilience4j.circuitbreaker.instances.paymentClient.slidingWindowSize=5
resilience4j.circuitbreaker.instances.paymentClient.failureRateThreshold=50
resilience4j.circuitbreaker.instances.paymentClient.waitDurationInOpenState=10s
resilience4j.circuitbreaker.instances.paymentClient.permittedNumberOfCallsInHalfOpenState=2
```

### Comportamiento del fallback

Si `debt-service` o `payment-service` no responden:

```
ai-risk-service intenta calcular riesgo de cliente
        │
        ├─ debt-service disponible  → Obtiene historial de deudas ✅
        │
        └─ debt-service no disponible → Fallback:
              Devuelve lista vacía de deudas
              Calcula riesgo como GOOD_CLIENT (sin datos = sin mora)
              Responde igual de forma estructurada ✅
```

---

## Estrategia 3: Fallback de Groq AI (ai-risk-service)

El análisis con inteligencia artificial es **opcional**. Si la API de Groq no está disponible (sin créditos, timeout, error de red), el sistema sigue funcionando con solo las reglas de negocio.

### Lógica de decisión

```
ai-risk-service calcula riesgo:

PASO 1 — Reglas de negocio (siempre se ejecuta):
  Días de mora acumulados:
    0 días       → GOOD_CLIENT  🟢
    1 - 29 días  → LOW_RISK     🟡
    ≥ 30 días    → HIGH_RISK    🔴

PASO 2 — Análisis IA Groq (opcional):
  ┌─────────────────────────────────────────────────┐
  │ Groq disponible:                                │
  │   → Envía historial al LLM                     │
  │   → Recibe: aiRiskLevel, aiScore, recomendaciones│
  │   → Respuesta incluye AMBOS análisis            │
  ├─────────────────────────────────────────────────┤
  │ Groq no disponible (timeout / error):           │
  │   → aiRiskLevel = null                          │
  │   → aiScore = null                              │
  │   → recommendations = null                     │
  │   → Respuesta incluye solo análisis por reglas  │
  └─────────────────────────────────────────────────┘
```

### Respuesta con IA disponible

```json
{
  "clientId": "uuid-deudor",
  "riskLevel": "HIGH_RISK",
  "riskScore": 78.5,
  "totalDaysLate": 45,
  "latePaymentCount": 3,
  "paymentCount": 8,
  "aiRiskLevel": "HIGH_RISK",
  "aiScore": 82.0,
  "aiRecommendations": [
    "Contactar al cliente para plan de pagos",
    "Considerar garantías adicionales"
  ]
}
```

### Respuesta sin IA (fallback)

```json
{
  "clientId": "uuid-deudor",
  "riskLevel": "HIGH_RISK",
  "riskScore": 78.5,
  "totalDaysLate": 45,
  "latePaymentCount": 3,
  "paymentCount": 8,
  "aiRiskLevel": null,
  "aiScore": null,
  "aiRecommendations": null
}
```

El sistema **nunca falla por culpa de Groq**. Solo cambia el nivel de detalle de la respuesta.

---

## Resumen de los 3 puntos de resiliencia

| Punto | Patrón | ¿Qué pasa si falla? |
|-------|--------|---------------------|
| payment-service → debt-service | Circuit Breaker + Retry | Pago se guarda, balance se actualiza después |
| ai-risk-service → debt/payment | OpenFeign Circuit Breaker | Cálculo de riesgo con datos vacíos (GOOD_CLIENT) |
| ai-risk-service → Groq API | Try/Catch + Fallback null | Respuesta solo con reglas de negocio, sin IA |

---

## Monitoreo del Circuit Breaker

El estado del circuit breaker es visible en tiempo real via Actuator:

```
GET /actuator/health
GET /actuator/circuitbreakers

Respuesta ejemplo:
{
  "debtClient": {
    "state": "CLOSED",
    "failureRate": "0.0%",
    "bufferedCalls": 3,
    "failedCalls": 0
  }
}
```

Disponible en:
- `payment-service/actuator/circuitbreakers`
- `ai-risk-service/actuator/circuitbreakers`

---

## Retención de Datos por Servicio

### Datos de negocio (permanentes)

| Servicio | Almacén | Retención | Nota |
|----------|---------|-----------|------|
| debt-service | SQLite | **Indefinida** | No hay política de limpieza |
| payment-service | SQLite | **Indefinida** | Historial completo de pagos |
| auth-service | SQLite | **Indefinida** | Usuarios activos e inactivos (soft delete) |
| notification-service | SQLite | **Indefinida** | Logs de auditoría de correos enviados |
| debtor-service | SQLite | **Indefinida** | Datos de deudores persistentes |

### ¿Los fallbacks actúan como caché para el usuario?

Respuesta corta: **depende del servicio**. El sistema tiene dos comportamientos distintos:

#### Fallbacks que devuelven datos vacíos (NO son caché)

Cuando `debt-service` o `payment-service` están caídos:

```
Circuit Breaker ABIERTO → Fallback devuelve lista vacía []

El usuario ve: "No hay datos disponibles"
El sistema NO guarda ni sirve datos anteriores en este caso.
No hay caché de respaldo para deudas ni pagos.
```

Esto aplica a:
- `payment-service` → `debt-service` (circuit breaker)
- `ai-risk-service` → `debt-service` (Feign fallback)
- `ai-risk-service` → `payment-service` (Feign fallback)

#### El único servicio con caché real: ai-risk-service

El nivel de riesgo tiene **dos capas** que sí permiten servir datos aunque otros servicios estén caídos:

```
Capa 1 — Caché en memoria (Spring Simple Cache)
  @Cacheable(value = "clientRisk", key = "#clientId")
  → Sin TTL configurado: vive mientras el servicio esté corriendo
  → Si el riesgo se calculó antes de que fallara algo, se sirve desde memoria
  → Se pierde al reiniciar el servicio

Capa 2 — SQLite (client_risk table)
  → Persiste entre reinicios del servicio
  → Siempre hay un último valor calculado guardado
  → Se actualiza con el job nocturno (2:00 AM) o manualmente
```

**Comportamiento cuando debt-service o payment-service están caídos:**

```
Usuario consulta riesgo del cliente X
        │
        ▼
¿Está en caché memoria?
  Sí → Devuelve el dato cacheado (sin llamar a nadie) ✅
  No → ¿Está en SQLite?
         Sí → Devuelve el último cálculo guardado ✅
         No → Intenta calcular → fallback vacío → GOOD_CLIENT por defecto
```

**El dato más viejo que puede servir:** el calculado en el último job de las 2:00 AM (máximo 24 horas de antigüedad).

### Scheduler de riesgo — cada cuánto recalcula

```
Cron: "0 0 2 * * *"  →  Todos los días a las 2:00 AM

Acción: recalculateAll()
  → Toma todos los clientes en client_risk
  → Recalcula reglas de negocio + llama Groq para cada uno
  → Hace UPSERT en SQLite con los nuevos valores
  → El dato en SQLite siempre refleja el estado de la última ejecución
```

### Scheduler de recordatorios — notification-service

```
Cron: "0 0 8 * * *"  →  Todos los días a las 8:00 AM

Acción: sendPaymentReminders()
  → Busca deudas ACTIVAS cuyo vencimiento sea exactamente en 3 días
  → Por cada deuda, verifica en notification_logs si ya se envió
    un recordatorio exitoso HOY (desde inicio del día)
  → Si ya se envió → lo omite (anti-duplicados)
  → Si no → envía el correo y guarda el log
```

**Anti-duplicado:** el log de notificaciones actúa como memoria del scheduler. Aunque el servicio se reinicie y el caché en memoria se pierda, el log en SQLite garantiza que no se envíen correos duplicados en el mismo día.

### JWT — retención del token

```
Duración del token:  1 hora (3,600,000 ms)
Almacenamiento:      Solo en el cliente (sesión HTTP de web-ui)
Servidor:            Stateless — no guarda tokens emitidos

Consecuencia: no hay forma de invalidar un token antes de que expire.
Si un usuario hace logout, el token sigue siendo técnicamente válido
por el tiempo restante de la hora, pero se elimina de la sesión del browser.
```

### Resumen visual

```
JWT                    ──── 1 hora ────►|  expira
Caché riesgo (memoria) ─────────────────── sin TTL, hasta reinicio
SQLite riesgo          ─────────────────── recalcula cada 24h (2:00 AM)
Logs notificación      ─────────────────── indefinido (auditoría)
Pagos / Deudas         ─────────────────── indefinido
Deudores               ─────────────────── indefinido (SQLite)

Fallbacks deudas/pagos → NO tienen caché, devuelven lista vacía []
Fallback riesgo        → SÍ tiene caché (memoria + SQLite, máx. 24h)
```
