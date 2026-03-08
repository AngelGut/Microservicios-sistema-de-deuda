package com.debtmanager.debtorservice.service;

import com.debtmanager.debtorservice.dto.DebtorRequest;
import com.debtmanager.debtorservice.entity.Debtor;

import java.util.List;

public interface DebtorService {
    List<Debtor> findAll();

    Debtor findById(Long id);

    Debtor create(DebtorRequest request);

    Debtor update(Long id, DebtorRequest request);
}
