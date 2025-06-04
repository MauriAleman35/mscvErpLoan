package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Solicitude;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Solicitude entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SolicitudeRepository extends JpaRepository<Solicitude, Long> {
    // Métodos para filtrar solicitudes
    List<Solicitude> findByStatus(String status);

    List<Solicitude> findByBorrowerId(Long borrowerId);

    // Métodos para contar
    Integer countByStatus(String status);

    Integer countByBorrowerId(Long borrowerId);

    // Métodos para filtros por monto
    List<Solicitude> findByLoanAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    List<Solicitude> findByLoanAmountGreaterThanEqual(BigDecimal minAmount);

    List<Solicitude> findByLoanAmountLessThanEqual(BigDecimal maxAmount);

    // Búsquedas combinadas
    List<Solicitude> findByStatusAndBorrowerId(String status, Long borrowerId);
}
