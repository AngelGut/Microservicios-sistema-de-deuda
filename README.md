# Debt Manager System (Microservices)

Aplicación web responsive para gestionar deudores, deudas y pagos parciales con autenticación JWT.
Arquitectura de microservicios con Spring Boot + API Gateway + Web UI (Thymeleaf).
Despliegue en Railway con auto-deploy desde GitHub.

## Servicios
Core:
- api-gateway
- auth-service
- debtor-service
- debt-service
- payment-service
- web-ui (Thymeleaf)

Value Add (para puntos extra):
- fx-service (USD/DOP con exchangerate.host + cache)
- notification-service (email + reminders + error logs)
- ai-risk-service (Groq: FIABLE / REVISIÓN / BLOQUEADO)

## Extras técnicos (obligatorios)
- Swagger / OpenAPI en cada microservicio
- Actuator (/actuator/health)
- Flyway migrations por servicio
- Resilience4j (obligatorio: payment -> debt)

## Cómo correr local (alto nivel)
1) Copia `.env.example` a `.env` y completa valores.
2) Levanta Postgres local (o usa uno remoto).
3) Arranca servicios individualmente con `./mvnw spring-boot:run`
   o usa `./scripts/local/start-all.ps1`

## URLs útiles
- Swagger: /swagger-ui/index.html
- Health: /actuator/health

## Demo (guion rápido)
1. Login JWT
2. Crear deudor y deuda
3. Pago parcial + historial + saldo actualizado
4. Swagger UI por servicio
5. Actuator health por servicio
6. Circuit breaker (debt-service caído y payment responde controlado)
7. FX USD->DOP
8. Recordatorios + email
9. Error logs (email al dueño)
10. IA estado de deudor (o fallback)
