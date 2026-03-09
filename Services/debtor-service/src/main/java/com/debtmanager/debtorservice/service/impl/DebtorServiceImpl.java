package com.debtmanager.debtorservice.service.impl;

import com.debtmanager.debtorservice.dto.DebtorRequest;
import com.debtmanager.debtorservice.entity.Debtor;
import com.debtmanager.debtorservice.repository.DebtorRepository;
import com.debtmanager.debtorservice.service.DebtorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtorServiceImpl implements DebtorService {

    private final DebtorRepository repository;

    @Override
    public List<Debtor> findAll() {
        return repository.findAll();
    }

    @Override
    public Debtor findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debtor not found: " + id));
    }

    @Override
    public Debtor create(DebtorRequest request) {
        Debtor debtor = Debtor.builder()
                .name(request.name())
                .document(request.document())
                .email(request.email())
                .type(request.type())
                .build();

        return repository.save(debtor);
    }

    @Override
    public Debtor update(Long id, DebtorRequest request) {
        Debtor debtor = findById(id);
        debtor.setName(request.name());
        debtor.setDocument(request.document());
        debtor.setEmail(request.email());
        debtor.setType(request.type());

        return repository.save(debtor);
    }
}
