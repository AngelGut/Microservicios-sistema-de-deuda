package com.example.debtservice.repository;

import com.example.debtservice.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, String> {

    List<Debt> findByDebtorId(String debtorId);

    List<Debt> findByCurrency(String currency);
}
