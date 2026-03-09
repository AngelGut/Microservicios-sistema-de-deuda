# Railway Deploy Notes (CD)

Este proyecto se despliega en Railway desde GitHub.
Railway hace **Auto Deploy** cuando hacemos push a `main` (esto cubre el CD del CI/CD).

## Enfoque (Monorepo)
- Un repositorio con todos los servicios en `services/*`
- En Railway se crean **múltiples Services**, cada uno apuntando al mismo repo pero con un **Root Directory** distinto.

Ejemplo Root Directory por servicio:
- services/api-gateway
- services/auth-service
- services/debtor-service
- services/debt-service
- services/payment-service
- services/fx-service
- services/notification-service
- services/ai-risk-service
- services/user-service
- services/web-ui

## Variables de entorno (por servicio)
Cada servicio puede necesitar:
- PORT (Railway lo inyecta)
- DATABASE_URL (Railway Postgres o external)
- JWT_SECRET
- MAIL_API_KEY / OWNER_EMAIL (notification)
- GROQ_API_KEY (ai-risk)
- FX_BASE_URL (fx-service)
- AUTH_SERVICE_URL / DEBTOR_SERVICE_URL / DEBT_SERVICE_URL / PAYMENT_SERVICE_URL / FX_SERVICE_URL / NOTIFICATION_SERVICE_URL / AI_RISK_SERVICE_URL / USER_SERVICE_URL / WEB_UI_URL (api-gateway)

## Comandos
Railway detecta Maven por defecto. Si hay que forzar:
- Build: ./mvnw -DskipTests package
- Start: ./mvnw spring-boot:run

## Salud del sistema
Cada servicio expone:
- /actuator/health
y documentación:
- /swagger-ui/index.html

## Nota para la demo
Mostrar:
- Auto deploy con push a GitHub
- /actuator/health por servicio
- Swagger por servicio
