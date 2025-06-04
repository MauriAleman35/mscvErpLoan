package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Loan;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Loan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Override
    List<Loan> findAll();

    // Métodos para filtrar préstamos
    //<Loan> findByCurrentStatus(String currentStatus);

    List<Loan> findByLoanAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // Métodos para contar préstamos
    Integer countByCurrentStatus(String currentStatus);

    // Métodos adicionales para filtros por fechas
    List<Loan> findByStartDateBetween(Instant startDate, Instant endDate);

    List<Loan> findByEndDateBetween(Instant startDate, Instant endDate);

    // Métodos para buscar por oferta
    List<Loan> findByOfferId(Long offerId);

    // Métodos para búsqueda combinada
    List<Loan> findByCurrentStatusAndLoanAmountGreaterThan(String status, BigDecimal minAmount);
}
