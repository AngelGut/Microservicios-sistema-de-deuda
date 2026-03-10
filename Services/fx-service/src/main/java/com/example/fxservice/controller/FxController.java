package com.example.fxservice.controller;

import com.example.fxservice.dto.ConversionResponse;
import com.example.fxservice.service.FxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fx")
@Tag(name = "FX Service", description = "Conversión de divisas USD a DOP")
public class FxController {

    private final FxService fxService;

    public FxController(FxService fxService) {
        this.fxService = fxService;
    }

    @GetMapping("/convert")
    @Operation(summary = "Convertir monto de una divisa a otra",
               description = "Ejemplo: /api/v1/fx/convert?from=USD&to=DOP&amount=100")
    public ResponseEntity<ConversionResponse> convert(
            @RequestParam(defaultValue = "USD") String from,
            @RequestParam(defaultValue = "DOP") String to,
            @RequestParam double amount) {

        ConversionResponse response = fxService.convert(from, to, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rate")
    @Operation(summary = "Obtener tasa de cambio actual USD a DOP")
    public ResponseEntity<ConversionResponse> getRate() {
        // Convierte 1 USD para obtener la tasa actual
        ConversionResponse response = fxService.convert("USD", "DOP", 1);
        return ResponseEntity.ok(response);
    }
}
