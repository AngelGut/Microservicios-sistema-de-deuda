# Stack Tecnológico — Debt Manager Microservices

---

## Lenguaje y Runtime

### Java 17
- Versión LTS (Long Term Support) usada en todos los microservicios
- Permite usar `records` (DTOs inmutables sin boilerplate), `sealed classes`, y mejoras de texto
- Elegido por estabilidad, soporte garantizado hasta 2029, y compatibilidad con Spring Boot 3.x

---

## Framework Principal

### Spring Boot 3.x
Núcleo de todos los microservicios. Proporciona:

| Módulo | Uso en el sistema |
|--------|-------------------|
| `spring-boot-starter-web` | Controladores REST (todos los servicios excepto gateway) |
| `spring-boot-starter-data-jpa` | Acceso a base de datos con JPA/Hibernate |
| `spring-boot-starter-security` | Configuración de seguridad y filtros JWT |
| `spring-boot-starter-validation` | Validación de DTOs con anotaciones (`@NotNull`, `@Email`, etc.) |
| `spring-boot-starter-actuator` | Endpoints de salud y métricas (`/actuator/health`) |
| `spring-boot-starter-mail` | Envío de correos en notification-service |
| `spring-boot-starter-cache` | Caché en memoria para ai-risk-service |

---

## API Gateway

### Spring Cloud Gateway (WebFlux)
- Punto único de entrada del sistema (`api-gateway :8080`)
- Basado en programación **reactiva** (Project Reactor), no bloqueante
- Enruta peticiones a los microservicios según el path:
  ```
  /api/v1/auth/**     → auth-service
  /api/v1/debtors/**  → debtor-service
  /api/v1/debts/**    → debt-service
  /api/v1/payments/** → payment-service
  /api/v1/risk/**     → ai-risk-service
  /api/v1/fx/**       → fx-service
  ```
- No valida JWT (la validación la hace cada servicio individualmente)

---

## Seguridad

### Spring Security + JWT (jjwt 0.12.5)
- `auth-service` emite tokens JWT firmados con HMAC-SHA256
- Cada servicio valida el token localmente usando `JwtAuthFilter` sin llamadas adicionales
- BCrypt para hash de contraseñas (factor de costo por defecto = 10)
- Claims del token: `sub` (userId), `email`, `role`, `iat`, `exp`
- Expiración: 1 hora

---

## Comunicación entre Servicios

### Spring OpenFeign
Clientes HTTP declarativos para comunicación síncrona entre servicios:
```java
@FeignClient(name = "debt-service", url = "${app.debt-service.base-url}")
public interface DebtClient {
    @GetMapping("/api/v1/debts")
    ApiResponse<List<DebtDTO>> getDebtsByDebtor(@RequestParam String debtorId);
}
```
Usado en: `ai-risk-service`, `notification-service`, `web-ui`

### RestTemplate
Cliente HTTP clásico (bloqueante), usado en servicios más simples:
- `payment-service` → `debt-service` (con circuit breaker)
- `payment-service` → `notification-service`

---

## Resiliencia

### Resilience4j
Librería de tolerancia a fallos. En este sistema se usa:

| Patrón | Configuración | Servicio |
|--------|--------------|---------|
| Circuit Breaker | 5 calls, 50% fallo, 10s espera | payment-service, ai-risk-service |
| Retry | 3 intentos, 500ms entre cada uno | payment-service, ai-risk-service |

Estados del Circuit Breaker: **CERRADO** → **ABIERTO** → **SEMI-ABIERTO** → **CERRADO**

---

## Bases de Datos

### SQLite
- Base de datos embebida (archivo `.db` en disco), sin servidor separado
- Usada por: auth-service, debt-service, payment-service, ai-risk-service, notification-service, debtor-service
- Ventaja en Railway: no requiere provisionar un servidor de BD externo
- Dialecto JPA: `org.hibernate.community.dialect.SQLiteDialect`

### Flyway
- Gestión de migraciones de base de datos con versionado
- Cada servicio tiene sus scripts en `src/main/resources/db/migration/`
- Nomenclatura: `V1__descripcion.sql`, `V2__descripcion.sql`
- Se ejecuta automáticamente al arrancar el servicio
- Garantiza que el esquema de la BD sea consistente entre entornos

---

## Interfaz de Usuario

### Thymeleaf (web-ui)
- Motor de plantillas HTML del lado del servidor (SSR — Server Side Rendering)
- Integrado con Spring MVC: los controladores pasan datos al modelo, Thymeleaf los renderiza
- Ventaja: no requiere API separada para el frontend, el HTML llega ya construido al browser
- Cada vista consulta los microservicios vía clientes HTTP internos, no desde el browser

---

## Librería Compartida

### common-lib
- Módulo Maven interno usado como dependencia por todos los microservicios
- Provee:

| Clase | Propósito |
|-------|-----------|
| `ApiResponse<T>` | Envuelve cualquier respuesta exitosa con `success`, `timestamp`, `traceId`, `data` |
| `ApiErrorResponse` | Estructura estándar de error con código, mensaje y detalles |
| `ApiException` | Excepción controlada que se transforma en `ApiErrorResponse` |
| `GlobalExceptionHandler` | `@ControllerAdvice` que captura excepciones y devuelve `ApiErrorResponse` |
| `TraceIdUtil` | Genera y almacena el `traceId` en `ThreadLocal` |
| `TraceIdFilter` | Filtro HTTP que propaga `X-Trace-Id` en cada request |

---

## Documentación de API

### SpringDoc OpenAPI (Swagger)
- Genera documentación interactiva automáticamente desde las anotaciones de los controladores
- Disponible en cada servicio en: `/swagger-ui/index.html`
- Especificación JSON/YAML en: `/api-docs`

---

## Monitoreo

### Spring Boot Actuator
- Expone endpoints de diagnóstico sin código adicional
- Habilitados en todos los servicios:

| Endpoint | Información |
|----------|-------------|
| `/actuator/health` | Estado del servicio (UP/DOWN), estado de la BD |
| `/actuator/metrics` | Métricas JVM, peticiones HTTP, tiempos de respuesta |
| `/actuator/circuitbreakers` | Estado actual del Circuit Breaker (payment, ai-risk) |

---

## Tareas Programadas

### Spring Scheduler
- `@Scheduled(cron = "...")` para ejecutar jobs en horarios definidos
- Usado en:
  - `ai-risk-service`: recalcular riesgo de todos los clientes a las **2:00 AM** diario
  - `notification-service`: enviar recordatorios de vencimiento a las **8:00 AM** diario

---

## Reducción de Boilerplate

### Lombok
- Genera automáticamente en tiempo de compilación: `getters`, `setters`, `constructores`, `builders`, `toString`, `equals`
- Anotaciones principales usadas: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@RequiredArgsConstructor`

---

## Integración con APIs Externas

### Groq AI API
- LLM externo (modelos Llama) para análisis de riesgo crediticio
- `ai-risk-service` envía el historial de pagos y recibe recomendaciones en lenguaje natural
- Requiere: `GROQ_API_KEY` en variables de entorno
- Tiene fallback: si Groq no responde, el sistema usa solo las reglas de negocio

### ExchangeRate API
- API pública para obtener tasas de cambio USD/DOP en tiempo real
- Usada por `fx-service`
- Respuesta con caché para evitar llamadas repetidas

### SMTP / SendGrid
- Envío de correos electrónicos desde `notification-service`
- Configurable vía: `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- Dos tipos de correo: confirmación de pago y recordatorio de vencimiento

---

## Construcción y Módulos

### Maven (Multi-módulo)
- El `pom.xml` raíz define el proyecto padre con todos los módulos:
  ```xml
  <modules>
    <module>Services/common-lib</module>
    <module>Services/api-gateway</module>
    <module>Services/auth-service</module>
    <!-- ... resto de servicios -->
  </modules>
  ```
- `common-lib` se compila primero y los demás servicios la consumen como dependencia local
- Cada servicio genera su propio `.jar` ejecutable

---

## Despliegue

### Railway
- Plataforma de despliegue en la nube
- Cada microservicio tiene su propio servicio en Railway con:
  - Su propio Dockerfile
  - Puerto dinámico asignado vía `${PORT}`
  - Variables de entorno configuradas en el dashboard
- Redeploy automático al hacer push a GitHub (main branch)
- Variables críticas: `JWT_SECRET`, `GROQ_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`, URLs entre servicios

---

## Resumen Visual

```
Lenguaje:     Java 17
Framework:    Spring Boot 3.x
Gateway:      Spring Cloud Gateway (WebFlux)
Seguridad:    Spring Security + JWT (jjwt 0.12.5) + BCrypt
Comunicación: OpenFeign + RestTemplate
Resiliencia:  Resilience4j (Circuit Breaker + Retry)
Base de datos: SQLite + Flyway (migraciones)
UI:           Thymeleaf (SSR)
Docs API:     SpringDoc OpenAPI (Swagger)
Monitoreo:    Spring Actuator
Scheduler:    Spring @Scheduled
Boilerplate:  Lombok
IA externa:   Groq API (LLM)
FX externa:   ExchangeRate API
Email:        SMTP / SendGrid
Build:        Maven multi-módulo
Deploy:       Railway (Docker + GitHub)
```
