package com.example.airiskservice.client;

import com.example.airiskservice.dto.response.GroqRiskResponse;
import com.example.airiskservice.dto.response.PaymentHistoryDTO;
import com.example.airiskservice.model.RiskLevel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP que se comunica con la API de Groq (llama3-70b-8192).
 *
 * Principio SRP: única responsabilidad — construir el prompt,
 *               llamar a Groq y parsear la respuesta.
 * Principio DIP: el servicio depende de esta clase a través de
 *               su interfaz GroqAiAnalyzer.
 */
@Component
public class GroqClient implements GroqAiAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "llama-3.3-70b-versatile";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GroqClient(@Value("${app.groq.api-key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKey       = apiKey;
    }

    @Override
    public GroqRiskResponse analyze(Long clientId,
                                    int totalDaysLate,
                                    int latePaymentCount,
                                    int paymentCount,
                                    List<PaymentHistoryDTO> payments) {
        try {
            String prompt = buildPrompt(clientId, totalDaysLate,
                                        latePaymentCount, paymentCount, payments);
            String raw    = callGroq(prompt);
            return parseResponse(raw);
        } catch (Exception e) {
            log.error("Error llamando a Groq AI para clientId={}: {}", clientId, e.getMessage());
            return null;   // el servicio continúa con el resultado de reglas
        }
    }

    // ── Construcción del prompt ─────────────────────────────

    private String buildPrompt(Long clientId,
                                int totalDaysLate,
                                int latePaymentCount,
                                int paymentCount,
                                List<PaymentHistoryDTO> payments) {

        StringBuilder sb = new StringBuilder();
        sb.append("You are a credit risk analyst AI. Analyze the following payment history ")
          .append("and return ONLY a valid JSON object with no extra text.\n\n");

        sb.append("CLIENT ID: ").append(clientId).append("\n");
        sb.append("TOTAL PAYMENTS: ").append(paymentCount).append("\n");
        sb.append("LATE PAYMENTS: ").append(latePaymentCount).append("\n");
        sb.append("TOTAL DAYS LATE: ").append(totalDaysLate).append("\n\n");

        sb.append("PAYMENT HISTORY:\n");
        for (PaymentHistoryDTO p : payments) {
            sb.append("- Amount: ").append(p.amount())
              .append(", PaymentDate: ").append(p.paymentDate())
              .append(", DueDate: ").append(p.dueDate())
              .append("\n");
        }

        sb.append("\nRisk classification rules:\n");
        sb.append("- GOOD_CLIENT: no late payments (totalDaysLate == 0)\n");
        sb.append("- LOW_RISK: accumulated late days < 30\n");
        sb.append("- HIGH_RISK: accumulated late days >= 30\n\n");

        sb.append("Return ONLY this JSON (no markdown, no explanation):\n");
        sb.append("{\n");
        sb.append("  \"riskLevel\": \"GOOD_CLIENT\" | \"LOW_RISK\" | \"HIGH_RISK\",\n");
        sb.append("  \"aiScore\": <number 0.0 to 100.0>,\n");
        sb.append("  \"recommendations\": [\"recommendation1\", \"recommendation2\", \"recommendation3\"]\n");
        sb.append("}");

        return sb.toString();
    }

    // ── Llamada HTTP a Groq ─────────────────────────────────

    private String callGroq(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system",
                               "content", "You are a credit risk analyst. Always respond with valid JSON only."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2,
                "max_tokens",  512
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response =
                restTemplate.postForEntity(GROQ_URL, request, String.class);

        return response.getBody();
    }

    // ── Parseo de la respuesta ──────────────────────────────

    private GroqRiskResponse parseResponse(String rawBody) throws Exception {
        JsonNode root    = objectMapper.readTree(rawBody);
        String content   = root.path("choices")
                               .get(0)
                               .path("message")
                               .path("content")
                               .asText();

        log.debug("Groq raw content: {}", content);

        // Limpiar posibles bloques markdown ```json ... ```
        String json = content.replaceAll("(?s)```json\\s*", "")
                             .replaceAll("(?s)```\\s*", "")
                             .trim();

        JsonNode parsed = objectMapper.readTree(json);

        RiskLevel level = RiskLevel.valueOf(
                parsed.path("riskLevel").asText("GOOD_CLIENT"));

        double score = parsed.path("aiScore").asDouble(0.0);

        List<String> recommendations = new ArrayList<>();
        JsonNode recsNode = parsed.path("recommendations");
        if (recsNode.isArray()) {
            recsNode.forEach(n -> recommendations.add(n.asText()));
        }

        return new GroqRiskResponse(level, score, recommendations, content);
    }
}
