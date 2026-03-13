package com.example.fxservice.service;

import com.example.fxservice.dto.ConversionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Servicio de conversión de divisas.
 * Usa open.er-api.com (gratuito, sin API key).
 */
@Service
public class FxService {

    private final WebClient webClient;

    public FxService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public ConversionResponse convert(String from, String to, double amount) {
        // open.er-api.com — gratuito, sin API key
        Map response = webClient.get()
                .uri("https://open.er-api.com/v6/latest/{from}", from)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !"success".equals(response.get("result"))) {
            throw new RuntimeException("Error al obtener la tasa de cambio");
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        if (rates == null || !rates.containsKey(to)) {
            throw new RuntimeException("Moneda destino no encontrada: " + to);
        }

        double rate = ((Number) rates.get(to)).doubleValue();
        double converted = rate * amount;

        return ConversionResponse.builder()
                .from(from)
                .to(to)
                .amount(amount)
                .converted(converted)
                .rate(rate)
                .build();
    }
}
