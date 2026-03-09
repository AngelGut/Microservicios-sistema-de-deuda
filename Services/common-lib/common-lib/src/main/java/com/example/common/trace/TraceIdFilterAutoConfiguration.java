package com.example.common.trace;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * TraceIdFilterAutoConfiguration
 * ------------------------------
 * Auto-config de Spring Boot 3.
 *
 * Registra automáticamente el TraceIdFilter en cualquier microservicio
 * que incluya esta librería.
 */
@AutoConfiguration
public class TraceIdFilterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {

    FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new TraceIdFilter());

    // Aplicar a todas las rutas
    registration.addUrlPatterns("/*");

    // Ejecutar antes que otros filtros
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

    registration.setName("traceIdFilter");

    return registration;
  }
}
