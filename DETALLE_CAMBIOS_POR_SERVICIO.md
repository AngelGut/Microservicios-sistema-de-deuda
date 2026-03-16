# 🔧 DETALLE TÉCNICO: Cambios por Servicio

---

## 1️⃣ PAYMENT-SERVICE

### Archivo modificado
**Path**: `Services/payment-service/src/main/resources/application.properties`

### Cambio específico (Línea 24)

#### ANTES:
```properties
# ── JWT ────────────────────────────────────────────────────────
# La clave secreta debe coincidir con la que usa auth-service.
# En producción: externalizar con variables de entorno.
app.jwt.secret=${JWT_SECRET:216/26Bhnb8aGxAunrkrMqgKAVdiakCb12AqGFdA1ugBwBOCPhevH6HBjzgJKPKwxhwkYOLzZPkNc71LA/LJCA==}
```

#### DESPUÉS:
```properties
# ── JWT ────────────────────────────────────────────────────────
# La clave secreta debe coincidir con la que usa auth-service.
# En producción: externalizar con variables de entorno.
app.jwt.secret=${JWT_SECRET}
```

### Análisis técnico del cambio

**¿Qué cambió?**
- Removido el default hardcodeado: `216/26Bhnb8aGxAun...`
- Ahora REQUIERE que la variable `JWT_SECRET` esté en Railway

**¿Cómo funciona payment-service internamente?**

```
application.properties (ANTES):
  app.jwt.secret=${JWT_SECRET:216/26Bhnb8aG...}
         ↓
Spring resuelve: Si existe JWT_SECRET → úsala, si no → usa default
         ↓
JwtAuthFilter.java (línea ~10):
  public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    ...
  }
         ↓
Cada petición con Authorization header:
  1. Extrae token del header "Authorization: Bearer <token>"
  2. Parsea con: Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)
  3. Verifica firma: HMAC-SHA256(header.payload) == token.signature
  4. Si firma NO coincide (porque secret es diferente) → JwtException → 401
```

**El problema (ANTES)**:
```
auth-service genera token:
  secret = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh... (desde JWT_SECRET en Railway)
  token = HMAC-SHA256(data, secret)

payment-service intenta verificar:
  secret = 216/26Bhnb8aGxAunrkrMqgKAVdiakCb12AqGFdA1ug... (default hardcodeado)
  calculado = HMAC-SHA256(data, secret)

  ¿token.signature == calculado? NO → 401 INVALID SIGNATURE
```

**La solución (DESPUÉS)**:
```
payment-service configuration:
  app.jwt.secret=${JWT_SECRET}  ← Lee de Railway

En Railway (configurado):
  JWT_SECRET = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh...

payment-service al iniciar:
  secret = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh... (desde Railway)

Cuando verifica token:
  calculado = HMAC-SHA256(data, secret)
  ¿token.signature == calculado? SÍ ✓ → 200 OK
```

### Archivos Java que NO requirieron cambios

**JwtAuthFilter.java** (`Services/payment-service/src/main/java/com/example/paymentservice/security/JwtAuthFilter.java`):
- Ya estaba implementado correctamente
- Lee `${app.jwt.secret}` automáticamente desde properties
- No necesita cambios de código
- Solo necesitaba que el secret fuera el CORRECTO

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        // Spring inyecta el valor de app.jwt.secret
        // Ahora es: FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh...
        // ANTES era: 216/26Bhnb8aGxAunrkrMqgKAVdiakCb12AqGFdA1ug...
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            // AQUÍ es donde se verifica la firma
            // Si el secret es diferente, Jwts.parser() lanza JwtException
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)  // Usa el secret correcto
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Si llegamos aquí, el token es válido ✓
            String sub = claims.getSubject();
            String role = claims.get("role", String.class);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    sub, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // Token expirado
            sendAuthError(response, "Token expirado");
        } catch (JwtException e) {
            // Token inválido (firma incorrecta, etc.)
            sendAuthError(response, "Token inválido");
        }
    }

    private void sendAuthError(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of("AUTH_401", message,
            TraceIdUtil.getTraceId());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
```

---

## 2️⃣ USER-SERVICE

### Archivo modificado
**Path**: `Services/user-service/src/main/resources/application.properties`

### Cambio específico (Línea 28)

#### ANTES:
```properties
# JWT (misma clave que el auth-service)
jwt.secret=QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVoxMjM0NTY3ODkwQUJDREVGR0hJSktMTU5PUA==
jwt.expiration-ms=3600000
```

#### DESPUÉS:
```properties
# JWT (misma clave que el auth-service)
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=3600000
```

### Análisis técnico del cambio

**¿Qué cambió?**
- Removido el valor hardcodeado: `QUJDREVGR0hJSktMTU5PU...`
- Ahora REQUIERE que la variable `JWT_SECRET` esté en Railway

**¿Cómo funciona user-service internamente?**

```
application.properties (AHORA):
  jwt.secret=${JWT_SECRET}
     ↓
Spring resuelve: JWT_SECRET desde variables de entorno
     ↓
JwtService.java (línea ~10):
  @Component
  public class JwtService {
    @Value("${jwt.secret}")
    private String secret;  // Ahora contiene: FdQcQwFpqKWUkIIDyYEJJ...

    private Key getSigningKey() {
      byte[] keyBytes = Base64.getDecoder().decode(secret);
      return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
      try {
        extractClaims(token);
        return true;
      } catch (Exception e) {
        return false;
      }
    }
  }
     ↓
JwtFilter.java (que llama JwtService):
  String token = authHeader.substring(7);
  if (!jwtService.isTokenValid(token)) {
    response.setStatus(401);
    return;  // ← 401 UNAUTHORIZED
  }
```

**El problema (ANTES)**:
```
auth-service genera token:
  secret = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh... (desde JWT_SECRET)
  token = HMAC-SHA256(data, secret)

user-service intenta validar:
  secret = QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVoxMjM0N... (hardcodeado)

  Llama: Jwts.parser().verifyWith(getSigningKey()).build()
         .parseSignedClaims(token)

  Intenta verificar firma con secret diferente → JwtException → false
  → JwtFilter devuelve 401
```

**La solución (DESPUÉS)**:
```
user-service configuration:
  jwt.secret=${JWT_SECRET}  ← Lee de Railway

En Railway (configurado):
  JWT_SECRET = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh...

JwtService al iniciar:
  secret = FdQcQwFpqKWUkIIDyYEJJDOfDb13RpjEzdG13G1sWvmDh... (desde Railway)

JwtFilter cuando verifica:
  Llama: jwtService.isTokenValid(token)
  JwtService parsea con secret CORRECTO → Jwts.parser OK ✓
  → JwtFilter devuelve 200 OK
```

### Archivos Java que NO requirieron cambios

**JwtService.java** (`Services/user-service/src/main/java/com/example/userservice/security/JwtService.java`):
- Ya estaba implementado correctamente
- Lee `${jwt.secret}` automáticamente desde properties
- No necesita cambios de código

```java
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;  // Ahora es: FdQcQwFpqKWUkIIDyYEJJ... (desde Railway)

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);  // Si secret es correcto → OK
            return true;
        } catch (Exception e) {  // Si secret es diferente → Exception → false
            return false;
        }
    }
}
```

**JwtFilter.java** (`Services/user-service/src/main/java/com/example/userservice/security/JwtFilter.java`):
- Ya estaba implementado correctamente
- Llama a JwtService.isTokenValid(token)
- No necesita cambios de código

```java
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token requerido\"}");
            return;
        }

        String token = authHeader.substring(7);

        // AQUÍ es donde valida el token con el secret correcto
        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token inválido\"}");
            return;  // ← 401 (ANTES de este fix)
        }

        try {
            filterChain.doFilter(request, response);  // ← 200 OK (DESPUÉS de este fix)
        } finally {
            TraceIdUtil.clear();
        }
    }
}
```

---

## 3️⃣ AUTH-SERVICE

### ✅ SIN CAMBIOS

**No se modificó nada en auth-service**. Ya estaba correcto desde el inicio.

### Configuración existente

**Archivo**: `Services/auth-service/src/main/resources/application.properties`

```properties
# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=3600000
```

**Análisis**:
- ✅ Lee `${JWT_SECRET}` desde Railway (correcto)
- ✅ No tiene default hardcodeado (correcto)
- ✅ Ya genera tokens correctamente

### Cómo funciona auth-service (generación de tokens)

**JwtService.java** (`Services/auth-service/src/main/java/com/debtmanager/authservice/security/JwtService.java`):

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;  // FdQcQwFpqKWUkIIDyYEJJ... (desde Railway)

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;  // 3600000 (1 hora)

    public String generateToken(String userId, String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", email);
        extraClaims.put("role", role);
        return buildToken(extraClaims, userId);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)           // userId
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSignInKey())    // AQUÍ usa el secret para FIRMAR
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

**AuthServiceImpl.java** (que llama JwtService):

```java
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Valida credenciales
        User user = userRepository.findByEmailAndEnabledTrue(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas.");
        }

        // 2. Genera token con el secret correcto
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole());

        // 3. Devuelve el token firmado
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getJwtExpirationMs(),
                user.getRole(),
                user.getEmail());
    }
}
```

**Flujo completo de generación (en auth-service)**:

```
POST /api/v1/auth/login
  ↓
AuthServiceImpl.login()
  ↓
JwtService.generateToken()
  ↓
secretKey = FdQcQwFpqKWUkIIDyYEJJ... (desde Railway JWT_SECRET)
  ↓
Jwts.builder()
  .claims({"email": "admin@tejada.com", "role": "ADMIN"})
  .subject("user-id-uuid")
  .signWith(HMAC-SHA256 using secretKey)
  .compact()
  ↓
Token firmado: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWI...
  ↓
Devuelve LoginResponse con token + metadata
```

---

## 📊 TABLA COMPARATIVA: ANTES vs DESPUÉS

| Aspecto | ANTES (Broken) | DESPUÉS (Fixed) |
|--------|---|---|
| **payment-service secret** | `216/26Bhnb8aGxAun...` (hardcodeado) | `${JWT_SECRET}` (de Railway) |
| **user-service secret** | `QUJDREVGR0hJSktM...` (hardcodeado) | `${JWT_SECRET}` (de Railway) |
| **auth-service secret** | `${JWT_SECRET}` ✓ | `${JWT_SECRET}` ✓ |
| **JWT verificación en payment-service** | ❌ Falla (secret diferente) | ✅ OK (secret igual) |
| **JWT verificación en user-service** | ❌ Falla (secret diferente) | ✅ OK (secret igual) |
| **Endpoints /payments** | 401 Unauthorized | 200 OK |
| **Endpoints /users** | 401 Unauthorized | 200 OK |
| **Seguridad** | ⚠️ Hardcodeado en git | ✅ Variables de entorno |

---

## 🔄 FLUJO COMPLETO SINCRONIZADO (DESPUÉS DE FIXES)

```
1. Usuario hace login:
   POST http://api-gateway:8080/api/v1/auth/login
   Body: { "email": "admin@tejada.com", "password": "Admin2026!" }

2. API Gateway enruta a auth-service (8081):
   POST http://auth-service:8081/api/v1/auth/login

3. auth-service genera token:
   - Lee secret: FdQcQwFpqKWUkIIDyYEJJ... (desde JWT_SECRET en Railway)
   - Firma token con HMAC-SHA256
   - Devuelve: { "token": "eyJhbGciOiJIUzI1N...", ... }

4. Usuario usa token en payment-service:
   GET http://api-gateway:8080/api/v1/payments
   Header: Authorization: Bearer eyJhbGciOiJIUzI1N...

5. API Gateway enruta a payment-service (8084):
   - JwtAuthFilter intercepta petición
   - Lee secret: FdQcQwFpqKWUkIIDyYEJJ... (desde JWT_SECRET en Railway)
   - Verifica firma: HMAC-SHA256(header.payload) == token.signature ✓
   - Claims extraídos: { sub: "user-id", role: "ADMIN", email: "..." }
   - Petición autorizada → 200 OK

6. Usuario usa token en user-service:
   GET http://api-gateway:8080/api/v1/users
   Header: Authorization: Bearer eyJhbGciOiJIUzI1N...

7. API Gateway enruta a user-service (8087):
   - JwtFilter intercepta petición
   - Llama: jwtService.isTokenValid(token)
   - JwtService lee secret: FdQcQwFpqKWUkIIDyYEJJ... (desde JWT_SECRET en Railway)
   - Parsea y verifica: Jwts.parser().verifyWith(secret).parseSignedClaims(token) ✓
   - Petición autorizada → 200 OK
```

---

## ✨ RESUMEN DE CAMBIOS

| Servicio | Archivo | Cambio | Linea | Justificación |
|----------|---------|--------|-------|---|
| **payment-service** | application.properties | Remover default hardcodeado | 24 | Usar secret de Railway |
| **user-service** | application.properties | Remover valor hardcodeado | 28 | Usar secret de Railway |
| **auth-service** | - | ✅ SIN CAMBIOS | - | Ya estaba correcto |

**Archivos Java**: NINGUNO requirió cambios (ya estaban implementados correctamente)

**Configuración Railway**: Agregar variable `JWT_SECRET` a los 3 servicios
