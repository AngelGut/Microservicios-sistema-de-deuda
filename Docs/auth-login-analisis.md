# Decisión arquitectónica: gestión de usuarios centralizada en `auth-service`

## Decisión

`user-service` fue **eliminado** del sistema.

Toda la gestión de usuarios del sistema (credenciales, roles, estado) queda centralizada en `auth-service`.

---

## Contexto previo

El análisis original evaluaba si era conveniente mover el login a `user-service`. La conclusión fue que mantener dos servicios gestionando usuarios en paralelo generaba duplicación de fuentes de verdad y confusión de responsabilidades.

La decisión final fue la opuesta: **eliminar `user-service` y absorber su responsabilidad en `auth-service`**.

---

## Arquitectura actual

`auth-service` es el único servicio que:

- Autentifica usuarios (`POST /api/v1/auth/login`)
- Valida tokens (`POST /api/v1/auth/validate`)
- Gestiona usuarios del sistema (`/api/v1/auth/users/**`)

### Endpoints de gestión de usuarios (solo ADMIN)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/auth/users` | Listar usuarios |
| POST | `/api/v1/auth/users` | Crear usuario |
| GET | `/api/v1/auth/users/{id}` | Obtener usuario |
| PUT | `/api/v1/auth/users/{id}` | Actualizar (password, role, enabled) |
| DELETE | `/api/v1/auth/users/{id}` | Desactivar usuario (soft delete) |

### Endpoints públicos

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Login, devuelve JWT |
| POST | `/api/v1/auth/validate` | Validar token |

---

## Gateway

No se requirieron cambios en `api-gateway`. La ruta `/api/v1/auth/**` ya apuntaba a `auth-service`.

---

## Modelo de datos (tabla `users` en auth-service)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Identificador único |
| email | TEXT | Email único |
| password | TEXT | Hash bcrypt |
| role | TEXT | ADMIN / USER |
| enabled | BOOLEAN | Soft delete flag |
| createdAt | TIMESTAMP | Fecha de creación |

---

## Admin por defecto

Al arrancar `auth-service`, `DataInitializer` crea el admin si no existe:

- Email: `admin@tejada.com`
- Password: `Admin2026!`
- Role: `ADMIN`
