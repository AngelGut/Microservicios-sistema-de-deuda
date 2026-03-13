package com.example.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceIdFilter
 * -------------
 * Filtro que:
 * 1) Lee X-Request-Id del request si viene (ej: desde gateway)
 * 2) Si no viene, genera uno
 * 3) Lo guarda en MDC para logs
 * 4) Lo devuelve en el response header
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String incoming = request.getHeader(TraceIdUtil.TRACE_HEADER);

            String traceId = (incoming == null || incoming.isBlank())
                    ? TraceIdUtil.generate()
                    : incoming.trim();

            TraceIdUtil.setTraceId(traceId);

            response.setHeader(TraceIdUtil.TRACE_HEADER, traceId);

            filterChain.doFilter(request, response);

        } finally {
            TraceIdUtil.clear();
        }
    }
}
