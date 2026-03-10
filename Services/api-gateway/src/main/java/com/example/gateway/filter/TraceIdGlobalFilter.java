package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceIdGlobalFilter.class);
    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // Reutilizar el X-Trace-Id entrante si ya existe,
        // si no, generar uno nuevo de 8 caracteres
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        log.debug("[Gateway] TraceId: {} → {}", TRACE_HEADER, traceId);

        final String finalTraceId = traceId;

        // Mutar el request para propagar el X-Trace-Id
        // hacia el microservicio destino (fx-service, auth-service, etc.)
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TRACE_HEADER, finalTraceId)
                .build();

        // Construir el exchange mutado con el request modificado
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .response(exchange.getResponse())
                .build();

        // Ejecutar la cadena de filtros y, al terminar,
        // añadir el X-Trace-Id también en el response
        // Solo si el response aún no fue enviado (evita UnsupportedOperationException)
        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            if (!exchange.getResponse().isCommitted()) {
                exchange.getResponse().getHeaders().add(TRACE_HEADER, finalTraceId);
            }
        }));
    }

    // Este filtro se ejecuta primero que cualquier otro (máxima prioridad)
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
