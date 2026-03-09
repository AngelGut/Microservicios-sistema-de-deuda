# Análisis funcional: FX (tasa de cambio) y Riesgo (clientes con mora)

## 1) ¿Dónde está la zona de tasa de cambio (FX) en la UI?

Sí, está ubicada en el dashboard principal:

- Vista: `Services/web-ui/src/main/resources/templates/dashboard/index.html`
- Sección: bloque **"TASA FX"** (`USD / DOP`) que pinta `fxRate` y muestra estado "vía fx-service" o "no disponible".

Además, el dato se carga desde:

- `DashboardService` → `fxClient.getUsdToDop(token)`
- `FxClient` consume `GET /api/v1/fx/rate?from=USD&to=DOP` vía gateway

Y en gateway existe ruta:

- `/api/v1/fx/**` → `fx-service`.

### Estado actual FX

- **Funcional a nivel de integración web-ui** (tiene fallback a `N/D` si falla).
- **Bien ubicada en dashboard** para la zona de tasa de cambio.

## 2) Riesgo de clientes (alto/bajo) y mora por días: estado actual

### Lo que sí existe

En `ai-risk-service`:

- Endpoint por cliente: `GET /risk/{clientId}`
- Endpoint alto riesgo: `GET /risk/high`
- Regla de clasificación por días de mora acumulados:
  - `GOOD_CLIENT` si 0 días
  - `LOW_RISK` si < 30
  - `HIGH_RISK` si >= 30

También existe `AiRiskClient` en web-ui.

### Lo que falta para que se vea en la UI como piden

1. **El `AiRiskClient` de web-ui no se usa en controladores/vistas**
   - No se está inyectando en `DebtorController` ni en `DashboardService`.
   - Por eso no aparecen etiquetas visuales de riesgo hoy.

2. **Contrato de endpoint desalineado**
   - web-ui llama `/api/v1/ai/risk/debtor/{debtorId}`
   - ai-risk-service expone `/risk/{clientId}`
   - Debe unificarse ruta/shape de respuesta.

3. **Gateway de ai-risk está apuntando a host incorrecto**
   - Ruta `/api/v1/risk/**` está yendo a `http://alert-alignment:8080`.
   - Debe apuntar a `ai-risk-service`.

4. **Dependencia de datos para mora**
   - ai-risk-service espera historial en `payment-service` vía `/payments/by-client/{clientId}`.
   - payment-service actual no expone ese endpoint (solo `/payments`, `/payments/{id}`, `/payments/by-debt/{debtId}`).
   - Sin ese endpoint, el cálculo de riesgo real no tendrá datos completos.

## 3) Conclusión

- **FX:** está bien ubicado e integrado en dashboard.
- **Riesgo con etiquetas (alto/bajo) por días de atraso:** aún **no está conectado de extremo a extremo**.

## 4) Plan mínimo recomendado (sin romper estructura)

1. Corregir gateway para ai-risk (`/api/v1/risk/**` → `ai-risk-service`).
2. Unificar contrato de web-ui con ai-risk:
   - O cambiar `AiRiskClient` a `/api/v1/risk/{clientId}`
   - O crear alias en gateway/controlador para mantener `/api/v1/ai/risk/debtor/{id}`.
3. Implementar en payment-service `GET /payments/by-client/{clientId}` con `dueDate` + `paymentDate`.
4. En web-ui:
   - Enriquecer lista de deudores o dashboard con riesgo por cliente.
   - Mostrar badges visuales: `GOOD_CLIENT` (verde), `LOW_RISK` (ámbar), `HIGH_RISK` (rojo).
   - Opcional: tarjeta "Clientes en alto riesgo" usando `/risk/high`.
