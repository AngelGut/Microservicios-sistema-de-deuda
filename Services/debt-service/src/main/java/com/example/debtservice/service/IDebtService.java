package com.example.debtservice.service;

import com.example.debtservice.dto.ApplyPaymentRequest;
import com.example.debtservice.dto.CreateDebtRequest;
import com.example.debtservice.entity.Debt;

import java.util.List;

/**
 * Interfaz que define los contratos del servicio de deudas.
 */
public interface IDebtService {

    /** Crea una nueva deuda */
    Debt createDebt(CreateDebtRequest request);

    /** Obtiene todas las deudas de un deudor */
    List<Debt> getDebtsByDebtorId(String debtorId);

    /** Obtiene una deuda por su ID */
    Debt getDebtById(String id);

    /** Aplica un pago a una deuda */
    Debt applyPayment(String id, ApplyPaymentRequest request);
}
