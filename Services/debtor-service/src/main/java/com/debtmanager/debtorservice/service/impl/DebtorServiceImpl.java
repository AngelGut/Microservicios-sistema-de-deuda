package com.debtmanager.debtorservice.service.impl;

import com.debtmanager.debtorservice.dto.DebtorRequest;
import com.debtmanager.debtorservice.entity.Debtor;
import com.debtmanager.debtorservice.repository.DebtorRepository;
import com.debtmanager.debtorservice.service.DebtorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtorServiceImpl implements DebtorService {

    private final DebtorRepository repository;

    public DebtorServiceImpl(DebtorRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Debtor> findAll() {
        return repository.findAll();
    }

    @Override
    public Debtor findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Debtor not found: " + id));
    }

    @Override
    public Debtor create(DebtorRequest request) {
        Debtor debtor = Debtor.builder()
                .name(request.name())
                .document(request.document())
                .email(request.email())
                .type(request.type())
                .phone(request.phone())
                .build();
        return repository.save(debtor);
    }

    @Override
    public Debtor update(Long id, DebtorRequest request) {
        Debtor debtor = findById(id);
        if (request.name() != null)
            debtor.setName(request.name());
        if (request.document() != null)
            debtor.setDocument(request.document());
        if (request.email() != null)
            debtor.setEmail(request.email());
        if (request.type() != null)
            debtor.setType(request.type());
        if (request.phone() != null)
            debtor.setPhone(request.phone());
        return repository.save(debtor);
    }
}
