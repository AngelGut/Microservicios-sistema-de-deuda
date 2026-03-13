package com.example.paymentservice.repository;

import com.example.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio de acceso a datos para {@link Payment}.
 *
 * <p>Principio ISP: expone solo las operaciones relevantes para el dominio
 * de pagos, sin métodos genéricos innecesarios.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Devuelve todos los pagos asociados a una deuda específica,
     * ordenados del más reciente al más antiguo.
     */
    List<Payment> findByDebtIdOrderByPaymentDateDesc(String debtId);

    /**
     * Calcula el total pagado para una deuda dada.
     * Retorna 0 si no hay pagos.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.debtId = :debtId")
    BigDecimal sumAmountByDebtId(@Param("debtId") String debtId);
}
