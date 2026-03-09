package com.example.airiskservice.controller;

import com.example.airiskservice.dto.response.RiskResponse;
import com.example.airiskservice.service.RiskService;
import com.example.common.api.ApiResponse;
import com.example.common.trace.TraceIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/risk")
@Tag(name = "Risk", description = "Análisis de riesgo crediticio de clientes")
@SecurityRequirement(name = "bearerAuth")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Consultar riesgo de un cliente")
    public ResponseEntity<ApiResponse<RiskResponse>> getRiskByClient(
            @Parameter(description = "ID del cliente") @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok(
                riskService.getRiskByClient(clientId), TraceIdUtil.getTraceId()));
    }

    @GetMapping("/high")
    @Operation(summary = "Listar clientes de alto riesgo")
    public ResponseEntity<ApiResponse<List<RiskResponse>>> getHighRiskClients() {
        return ResponseEntity.ok(ApiResponse.ok(
                riskService.getHighRiskClients(), TraceIdUtil.getTraceId()));
    }

    @PostMapping("/recalculate/{clientId}")
    @Operation(summary = "Recalcular riesgo de un cliente")
    public ResponseEntity<ApiResponse<RiskResponse>> recalculate(
            @Parameter(description = "ID del cliente") @PathVariable Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok(
                riskService.recalculate(clientId), TraceIdUtil.getTraceId()));
    }
}
