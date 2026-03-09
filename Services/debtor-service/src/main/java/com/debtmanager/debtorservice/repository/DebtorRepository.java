package com.debtmanager.debtorservice.repository;

import com.debtmanager.debtorservice.entity.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DebtorRepository extends JpaRepository<Debtor, Long> {
    Optional<Debtor> findByDocument(String document);
}
