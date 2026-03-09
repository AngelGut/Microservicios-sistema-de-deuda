package com.example.fxservice.service;

import com.example.fxservice.dto.ConversionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class FxService {

    private final WebClient webClient;

    @Value("${fx.api.key}")
    private String apiKey;

    @Value("${fx.api.url}")
    private String apiUrl;

    public FxService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public ConversionResponse convert(String from, String to, double amount) {
        // Llamar a la API externa de exchangerate.host
        Map response = webClient.get()
                .uri(apiUrl + "/convert?from={from}&to={to}&amount={amount}&access_key={key}",
                        from, to, amount, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !(Boolean) response.get("success")) {
            throw new RuntimeException("Error al obtener la tasa de cambio");
        }

        // La API retorna el resultado convertido y la tasa en "info.quote"
        Map info = (Map) response.get("info");
        double rate = ((Number) info.get("quote")).doubleValue();
        double converted = ((Number) response.get("result")).doubleValue();

        return ConversionResponse.builder()
                .from(from)
                .to(to)
                .amount(amount)
                .converted(converted)
                .rate(rate)
                .build();
    }
}
