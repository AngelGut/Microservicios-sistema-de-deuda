package com.example.debtservice.service;

import com.example.debtservice.dto.ApplyPaymentRequest;
import com.example.debtservice.dto.CreateDebtRequest;
import com.example.debtservice.entity.Debt;

import java.util.List;

public interface IDebtService {

    Debt createDebt(CreateDebtRequest request);

    List<Debt> getDebtsByDebtorId(String debtorId);

    List<Debt> getDebtsByCurrency(String currency);

    List<Debt> getAllDebts();

    Debt getDebtById(String id);

    Debt applyPayment(String id, ApplyPaymentRequest request);
}
