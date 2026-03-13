package com.debtmanager.debtorservice.controller;

import com.debtmanager.debtorservice.dto.DebtorRequest;
import com.debtmanager.debtorservice.entity.Debtor;
import com.debtmanager.debtorservice.service.DebtorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/debtors")
@RequiredArgsConstructor
public class DebtorController {

    private final DebtorService service;

    /** GET /api/v1/debtors — Lista todos los deudores */
    @GetMapping
    public ResponseEntity<List<Debtor>> getAllDebtors() {
        return ResponseEntity.ok(service.findAll());
    }

    /** GET /api/v1/debtors/{id} — Obtiene un deudor por ID */
    @GetMapping("/{id}")
    public ResponseEntity<Debtor> getDebtorById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /** POST /api/v1/debtors — Crea un nuevo deudor */
    @PostMapping
    public ResponseEntity<Debtor> createDebtor(@Valid @RequestBody DebtorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /** PUT /api/v1/debtors/{id} — Actualiza un deudor */
    @PutMapping("/{id}")
    public ResponseEntity<Debtor> updateDebtor(@PathVariable Long id,
            @Valid @RequestBody DebtorRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }
}
