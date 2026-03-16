# Seguridad JWT — Debt Manager Microservices

## ¿Qué es JWT en este sistema?

JWT (JSON Web Token) es el mecanismo de autenticación del sistema. El usuario se autentica **una sola vez** en `auth-service` y recibe un token firmado. Con ese token puede acceder a cualquier servicio sin volver a ingresar credenciales.

---

## Ciclo de Vida del Token

```
┌─────────────┐     1. POST /login          ┌──────────────┐
│   Usuario   │ ──────────────────────────► │ auth-service │
│             │                             │              │
│             │ ◄────────────────────────── │  Firma JWT   │
│             │     2. Devuelve token        │  con SECRET  │
└─────────────┘                             └──────────────┘
       │
       │  3. Guarda token en sesión HTTP (web-ui)
       │
       ▼
┌─────────────┐  Authorization: Bearer TOKEN  ┌────────────────┐
│   web-ui    │ ─────────────────────────────► │  api-gateway   │
└─────────────┘                               └───────┬────────┘
                                                      │
                                           4. Propaga el header
                                                      │
                              ┌───────────────────────▼──────────────┐
                              │  Cada microservicio recibe el token   │
                              │  y lo valida localmente con el mismo  │
                              │  JWT_SECRET (sin llamar a nadie)      │
                              └──────────────────────────────────────┘
```

**Punto clave:** La validación es **local** en cada servicio. No hay llamadas adicionales a `auth-service` por cada request. El token es autocontenido.

---

## Estructura del Token (Claims)

```json
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "uuid-del-usuario",
  "email": "admin@tejada.com",
  "role": "ADMIN",
  "iat": 1742000000,
  "exp": 1742003600
}

Firma:
  HMAC-SHA256(base64(header) + "." + base64(payload), JWT_SECRET)
```

| Claim | Valor | Uso |
|-------|-------|-----|
| `sub` | UUID del usuario | Identificador único |
| `email` | Email del usuario | Identidad legible |
| `role` | `ADMIN` o `USER` | Control de acceso |
| `iat` | Timestamp de emisión | Auditoría |
| `exp` | Timestamp de expiración | Validez del token |

**Expiración estándar: 1 hora (3,600,000 ms)**

---

## Generación del Token — auth-service

```
POST /api/v1/auth/login
Body: { "email": "admin@tejada.com", "password": "Admin2026!" }

auth-service:
  1. Busca usuario en SQLite por email (solo usuarios enabled=true)
  2. BCrypt.matches(passwordIngresada, hashGuardado)
  3. Si coincide → JwtService.generateToken(userId, email, role)
  4. Firma con HMAC-SHA256 usando JWT_SECRET de Railway
  5. Devuelve:
     {
       "token": "eyJhbGciOiJIUzI1NiJ9...",
       "tokenType": "Bearer",
       "expiresIn": 3600000,
       "role": "ADMIN",
       "email": "admin@tejada.com"
     }
```

---

## Validación del Token — cada microservicio

Cada servicio que recibe un request protegido ejecuta este proceso internamente:

```
Request entrante:
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
                                │
                    JwtAuthFilter / JwtFilter
                                │
            ┌───────────────────▼────────────────────┐
            │  1. Extrae token del header             │
            │  2. Jwts.parser()                       │
            │       .verifyWith(JWT_SECRET)           │
            │       .parseSignedClaims(token)         │
            │  3. Verifica firma HMAC-SHA256          │
            │  4. Verifica que no esté expirado       │
            │  5. Extrae claims: sub, role            │
            │  6. Pone en SecurityContext             │
            └───────────────────┬────────────────────┘
                                │
              ┌─────────────────┴──────────────────┐
              │ OK                                  │ FALLO
              ▼                                     ▼
      Procesa el request                     401 Unauthorized
      (200, 201, etc.)                  { "code": "AUTH_401" }
```

---

## Por qué todos los servicios deben usar el MISMO secret

JWT usa criptografía simétrica (HMAC-SHA256):

```
auth-service FIRMA con:      JWT_SECRET = "FdQcQwFp..."
payment-service VERIFICA con: JWT_SECRET = "FdQcQwFp..."  ✅ Coincide → OK

Si fueran diferentes:
auth-service FIRMA con:       JWT_SECRET = "FdQcQwFp..."
payment-service VERIFICA con: JWT_SECRET = "QUJDREVG..."  ❌ No coincide → 401
```

En Railway, **todos los servicios comparten la misma variable** `JWT_SECRET`.

---

## Servicios que validan JWT

| Servicio | Propiedad | Filter |
|----------|-----------|--------|
| payment-service | `app.jwt.secret` | `JwtAuthFilter` |
| debt-service | `app.jwt.secret` | `JwtAuthFilter` |
| debtor-service | `app.jwt.secret` | `JwtAuthFilter` |
| auth-service | `jwt.secret` | `JwtAuthFilter` |
| ai-risk-service | `app.jwt.secret` | `JwtAuthFilter` |
| notification-service | `app.jwt.secret` | `JwtAuthFilter` |
| fx-service | `app.jwt.secret` | `JwtAuthFilter` |

---

## Endpoints públicos (sin token)

| Servicio | Endpoint | Razón |
|----------|----------|-------|
| auth-service | `POST /api/v1/auth/login` | Es donde se obtiene el token |
| auth-service | `POST /api/v1/auth/validate` | Validación interna entre servicios |
| Todos | `GET /actuator/health` | Monitoreo Railway |

---

## Errores de autenticación

```json
// Token ausente o mal formado → 401
{
  "success": false,
  "traceId": "4ec7b46d",
  "error": { "code": "AUTH_401", "message": "Authentication failed" }
}

// Token válido pero rol insuficiente → 403
{
  "success": false,
  "traceId": "4ec7b46d",
  "error": { "code": "AUTH_403", "message": "Access denied" }
}
```
