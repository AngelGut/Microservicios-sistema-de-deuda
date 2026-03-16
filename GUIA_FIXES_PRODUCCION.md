# 🚀 GUÍA COMPLETA: Fixes de Producción - Sistema de Deudas (Railway)

## 📋 Resumen Ejecutivo

**Problema Principal**: El sistema estaba en producción (Railway) con 3 bugs críticos que impedían el funcionamiento correcto:

1. **web-ui no responde** — puerto hardcodeado
2. **payment-service rechaza todos los tokens (401)** — JWT secret diferente
3. **user-service rechaza todos los tokens (401)** — JWT secret diferente

**Solución**: Centralizar el JWT_SECRET en todas las instancias y usar variables de entorno de Railway.

---

## 🔴 PROBLEMA 1: web-ui no responde en Railway

### Root Cause

```properties
# ANTES (INCORRECTO)
server.port=8090  # Puerto hardcodeado
```

Railway inyecta el puerto dinámicamente via variable `${PORT}`. Si el servidor no escucha en ese puerto, Railway **no puede enrutar el tráfico** → servicio caído.

### Fix

**Archivo**: `Services/web-ui/src/main/resources/application.properties`

```properties
# DESPUÉS (CORRECTO)
server.port=${PORT:8090}  # Lee ${PORT} de Railway, default 8090 para local
```

**Impacto**: 1 línea
**Por qué funciona**:

- En Railway: `${PORT}` se reemplaza con el puerto dinámico asignado (ej: 51234)
- En local: usa default 8090
- Spring resuelve `${PORT}` desde variables de entorno antes de iniciar

---

## 🔴 PROBLEMA 2: payment-service rechaza tokens con 401

### Root Cause

```properties
# ANTES (INCORRECTO)
app.jwt.secret=${JWT_SECRET:216/26Bhnb8aGxAunrkrMqgKAVdiakCb12AqGFdA1ugBwBOCPhevH6HBjzgJKPKwxhwkYOLzZPkNc71LA/LJCA==}
```

**Problema**:

- `auth-service` firma tokens con una clave (desde `JWT_SECRET`)
- `payment-service` verifica tokens con **otra clave diferente** (el default hardcodeado)
- JwtAuthFilter de payment-service falla al parsear: `Invalid signature`
- Resultado: 401 Unauthorized en TODOS los endpoints protegidos

### Fix

**Archivo**: `Services/payment-service/src/main/resources/application.properties`

```properties
# ANTES
app.jwt.secret=${JWT_SECRET:216/26Bhnb8aGxAunrkrMqgKAVdiakCb12AqGFdA1ugBwBOCPhevH6HBjzgJKPKwxhwkYOLzZPkNc71LA/LJCA==}

# DESPUÉS
app.jwt.secret=${JWT_SECRET}
```

**Cambios de código**: NINGUNO. El `JwtAuthFilter.java` ya lee `${app.jwt.secret}` correctamente.

**Por qué funciona**:

- Ahora payment-service intenta leer `JWT_SECRET` desde Railway
- Si la variable está configurada en Railway con el MISMO valor que auth-service → tokens se verifican correctamente
- El servicio NO tiene default hardcodeado → fuerza al usuario a configurar la variable en Railway

**En Railway**:

```
JWT_SECRET = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDhBIJWQiC47IgR5p1YPzYXIRm5Ha04wJzA9XUhwLrhg==
```

---

## 🔴 PROBLEMA 3: user-service rechaza tokens con 401

### Root Cause

```properties
# ANTES (INCORRECTO)
jwt.secret=QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVoxMjM0NTY3ODkwQUJDREVGR0hJSktMTU5PUA==
```

Mismo problema que payment-service:

- Valor hardcodeado diferente al de auth-service
- JwtService falla al validar tokens
- 401 en endpoints protegidos

### Fix

**Archivo**: `Services/user-service/src/main/resources/application.properties`

```properties
# ANTES
jwt.secret=QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVoxMjM0NTY3ODkwQUJDREVGR0hJSktMTU5PUA==

# DESPUÉS
jwt.secret=${JWT_SECRET}
```

**Cambios de código**: NINGUNO. El `JwtService.java` ya lee `${jwt.secret}` correctamente.

**Por qué funciona**: Mismo mecanismo que payment-service.

---

## ✅ ARQUITECTURA DE SEGURIDAD JWT DESPUÉS DE LOS FIXES

```
┌──────────────────────────────────────────────────────────────┐
│                          RAILWAY                              │
│  Configurar variable de entorno (global o por servicio):      │
│  JWT_SECRET = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1s...    │
└──────────────────────────────────────────────────────────────┘
                              ▼
            ┌─────────────────────────────────────┐
            │        auth-service (8081)          │
            │ Lee: jwt.secret=${JWT_SECRET}       │
            │ • Genera tokens con JWT_SECRET      │
            │ • Claims: sub, role, email, exp     │
            │ Endpoints:                          │
            │   POST /api/v1/auth/login           │
            │   POST /api/v1/auth/validate        │
            └─────────────────────────────────────┘
                        ▲       ▲       ▲
                    FIRMA   FIRMA   FIRMA
                        │       │       │
        ┌───────────────┴───────┴───────┴──────────────┐
        │   Tokens signed con JWT_SECRET               │
        └───────────────┬───────┬───────┬──────────────┘
                        │       │       │
            VERIFICA   VERIFICA   VERIFICA
                        │       │       │
        ┌───────────────▼────┬──▼───────▼──────────┐
        │ payment-service    │ user-service        │
        │ Lee: app.jwt.secret│ Lee: jwt.secret     │
        │     = ${JWT_SECRET}│      = ${JWT_SECRET}│
        │ JwtAuthFilter      │ JwtService          │
        │ verifica tokens ✓  │ verifica tokens ✓   │
        └────────────────────┴─────────────────────┘
```

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### Para auth-service (sin cambios)

- ✅ Ya tiene: `jwt.secret=${JWT_SECRET}`
- ✅ Ya genera tokens correctamente
- ✅ DataInitializer crea admin automático
- ✅ NO requiere cambios

### Para payment-service

- [ ] Editar: `Services/payment-service/src/main/resources/application.properties`
- [ ] Cambiar línea con `app.jwt.secret`
- [ ] Remover default hardcodeado
- [ ] Resultado: `app.jwt.secret=${JWT_SECRET}`
- [ ] Verificar: JwtAuthFilter.java no necesita cambios
- [ ] Hacer commit

### Para user-service

- [ ] Editar: `Services/user-service/src/main/resources/application.properties`
- [ ] Cambiar línea con `jwt.secret`
- [ ] Remover valor hardcodeado
- [ ] Resultado: `jwt.secret=${JWT_SECRET}`
- [ ] Verificar: JwtService.java no necesita cambios
- [ ] Hacer commit

### Para web-ui

- [ ] Editar: `Services/web-ui/src/main/resources/application.properties`
- [ ] Cambiar línea con `server.port`
- [ ] Resultado: `server.port=${PORT:8090}`
- [ ] Hacer commit

### En Railway Dashboard

- [ ] Crear variable `JWT_SECRET` = `FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDhBIJWQiC47IgR5p1YPzYXIRm5Ha04wJzA9XUhwLrhg==`
- [ ] Configurar en servicios:
  - [ ] auth-service
  - [ ] payment-service
  - [ ] user-service
  - [ ] (web-ui no necesita, solo usa ${PORT})
- [ ] Triggerear redeploy en cada servicio

### Verificación

- [ ] `web-ui`: Status = ACTIVE, responde en puerto dinámico
- [ ] `payment-service`: Status = ACTIVE, tokens validados ✓
- [ ] `user-service`: Status = ACTIVE, tokens validados ✓
- [ ] `auth-service`: Status = ACTIVE, genera tokens ✓

---

## 🔍 VERIFICACIÓN TÉCNICA

### Test: Validar que payment-service acepta tokens

```bash
# 1. Obtener token de auth-service
curl -X POST http://api-gateway:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@tejada.com",
    "password": "Admin2026!"
  }'

# Respuesta esperada:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "tokenType": "Bearer",
#   "expiresIn": 3600000,
#   "role": "ADMIN",
#   "email": "admin@tejada.com"
# }

# 2. Usar token en payment-service
curl -X GET http://api-gateway:8080/api/v1/payments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"

# Respuesta esperada: 200 OK (NO 401)
```

### Test: Validar que user-service acepta tokens

```bash
curl -X GET http://api-gateway:8080/api/v1/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"

# Respuesta esperada: 200 OK (NO 401)
```

---

## 📚 CONCEPTOS CLAVE

### ¿Por qué no podemos tener JWT secrets diferentes?

JWT usa **criptografía simétrica** (HMAC-SHA256):

```
FIRMA (auth-service):
  Token = HMAC-SHA256(header.payload, JWT_SECRET_ORIGINAL)

VERIFICACIÓN (payment-service):
  Hash = HMAC-SHA256(header.payload, JWT_SECRET_DIFERENTE)
  ¿Hash == Token.signature? NO → 401 INVALID SIGNATURE
```

Si los secrets son diferentes, la firma no coincide → token rechazado.

### ¿Por qué ${PORT} en lugar de hardcodear?

```properties
# INCORRECTO (hardcodeado)
server.port=8090
# Si Railway asigna puerto 51234, el servidor escucha en 8090
# → Railway no puede conectarse → servicio caído

# CORRECTO (dinámico)
server.port=${PORT:8090}
# Si Railway asigna puerto 51234, el servidor escucha en 51234 ✓
# Si corre localmente sin PORT, usa default 8090 ✓
```

---

## 🎯 PARA TUS COMPAÑEROS CON OTRA INTERFAZ

Si están usando otro modelo UI (no Thymeleaf), los cambios siguen siendo los MISMOS:

1. **payment-service** y **user-service**: Actualizar JWT secret exactamente igual
2. **web-ui** (o equivalente): Cambiar `server.port` igual
3. **auth-service**: SIN cambios (ya está correcto)

El problema NO está en la UI, está en la **configuración de microservicios y JWT**. La interfaz (Thymeleaf, Angular, Vue, etc.) es irrelevante para estos fixes.

---

## 📝 NOTAS IMPORTANTES

- **NO mezclar secretos**: Todos los servicios deben usar **exactamente el MISMO JWT_SECRET**
- **Variables de entorno primero**: Railway se encarga de inyectar `${JWT_SECRET}` automáticamente
- **SIN defaults hardcodeados en producción**: Fuerza la configuración correcta
- **El default `:8090` en web-ui es OK**: Porque es para desarrollo local, Railway sobrescribe con `${PORT}`

---

## ✅ Estado Final

Después de estos fixes:

```
web-ui         → ✅ Responde en puerto dinámico
payment-service → ✅ Acepta tokens válidos
user-service   → ✅ Acepta tokens válidos
auth-service   → ✅ Genera tokens correctos (sin cambios)
```

**Sistema sincronizado y funcional en Railway** 🚀

cambios 12-3-26 de nuevo
