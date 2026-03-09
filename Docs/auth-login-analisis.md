# Análisis de solución propuesta: login en `user-service`

## Veredicto corto

La solución **sí conviene como parche rápido** para destrabar autenticación con usuarios registrados en `user-service`, pero **no conviene como diseño final** si se quiere arquitectura limpia de microservicios.

## Hallazgos en el estado actual del repo

1. `auth-service` ya expone `POST /api/v1/auth/login` y `POST /api/v1/auth/validate`, con su propia tabla de usuarios.
2. `auth-service` inicializa un admin por defecto (`admin@debtmanager.com`) al arrancar.
3. En `web-ui`, `AuthClient` hace login contra `/api/v1/auth/login` y registro contra `/api/v1/auth/register`.
4. `auth-service` no tiene endpoint `/register`.
5. En `api-gateway` no existe ruta hacia `user-service`; solo hay ruta `/api/v1/auth/**` hacia `auth-service`.
6. En `user-service`, el filtro JWT se aplica a `/api/*` y rechaza requests sin token (401), lo cual puede bloquear login/registro si no se excluyen rutas públicas.

## Qué está bien de su propuesta

- Mover login al `user-service` resuelve el desalineamiento entre “dónde se crean usuarios” y “dónde se validan credenciales”.
- `findByEmail` + `passwordEncoder.matches` + emisión JWT en login es patrón válido.
- Mantener `auth-service` solo para validación puede funcionar si todos los servicios comparten secreto y formato de claims.

## Riesgos / cosas a corregir antes de considerarlo estable

1. **Confusión de flujo en el texto**: Registro y Login están invertidos.
   - Correcto debería ser:
     - Registro → `POST /api/users`
     - Login → `POST /api/users/login`
2. **Gateway**: si `web-ui` pasa por gateway, deben enrutar `/api/users/**` o una ruta versionada equivalente hacia `user-service`.
3. **Filtro JWT en user-service**: deben dejar públicas al menos:
   - `POST /api/users` (registro)
   - `POST /api/users/login` (login)
4. **Contrato de token**: si `auth-service` valida tokens emitidos por `user-service`, ambos deben compartir:
   - mismo `jwt.secret`
   - mismo algoritmo
   - expectativas de claims (`role` vs `roles`, `sub`, expiración)
5. **Manejo de errores**: usar excepciones controladas (4xx) en vez de `RuntimeException` genérica para credenciales inválidas / usuario inactivo.

## Recomendación práctica

- **Sí implementar su cambio** para avanzar ahora, pero con estos mínimos:
  1. Exponer login en `user-service`.
  2. Ajustar `JwtFilter` para whitelist de login/registro.
  3. Ajustar `api-gateway` y `web-ui` al nuevo endpoint.
  4. Corregir flujo documentado (registro/login).
  5. Acordar contrato de JWT único entre `user-service` y `auth-service`.

- **A mediano plazo** definir una arquitectura única:
  - Opción A: `auth-service` autentica y emite tokens (y consulta user-service/DB compartida).
  - Opción B: `user-service` autentica y emite, `auth-service` solo introspección/validación.
  - Evitar que ambos gestionen usuarios de forma paralela para no duplicar fuentes de verdad.
