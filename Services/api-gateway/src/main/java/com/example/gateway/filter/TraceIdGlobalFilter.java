package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * TraceIdGlobalFilter
 * -------------------
 * Filtro global que garantiza que cada request tenga un X-Trace-Id.
 *
 * Comportamiento (idéntico al TraceIdFilter de common-lib pero para WebFlux):
 *   - Si el cliente envía X-Trace-Id → se reutiliza
 *   - Si NO viene → se genera uno nuevo
 *   - Se propaga al microservicio destino en el header del request
 *   - Se devuelve al cliente en el header del response
 *
 * Principio SRP: este filtro tiene una única responsabilidad — trazabilidad.
 * Principio OCP: extensible sin modificar — se puede agregar lógica sin tocar esto.
 */
@Slf4j
@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    /** Nombre del header de trazabilidad — igual que en common-lib (TraceIdUtil.TRACE_HEADER) */
    public static final String TRACE_HEADER = "X-Trace-Id";

    /**
     * Intercepta cada request que pasa por el gateway.
     * Añade o propaga el X-Trace-Id antes de enviarlo al servicio destino.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // Leemos el traceId entrante — si no existe, generamos uno nuevo
        String incomingTraceId = request.getHeaders().getFirst(TRACE_HEADER);
        String traceId = (incomingTraceId != null && !incomingTraceId.isBlank())
                ? incomingTraceId.trim()
                : UUID.randomUUID().toString().substring(0, 8);

        log.debug("[Gateway] TraceId: {} → {}", TRACE_HEADER, traceId);

        // Mutamos el request para añadir X-Trace-Id al microservicio destino
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_HEADER, traceId)
                .build();

        // Propagamos también en el response al cliente
        final String finalTraceId = traceId;
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() ->
                        exchange.getResponse().getHeaders().set(TRACE_HEADER, finalTraceId)
                ));
    }

    /**
     * Prioridad máxima — se ejecuta antes que cualquier otro filtro del gateway.
     * Así el traceId está disponible en toda la cadena.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
