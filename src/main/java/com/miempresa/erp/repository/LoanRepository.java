package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Loan;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Loan entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Override
    List<Loan> findAll();

    @Query("SELECT l FROM Loan l WHERE l.offer.solicitude.borrower.id = :userId AND l.currentStatus = 'al_dia'")
    List<Loan> findActiveLoansByBorrowerId(@Param("userId") Long userId);

    // Métodos para filtrar préstamos
    //<Loan> findByCurrentStatus(String currentStatus);
    // Para encontrar préstamos donde el usuario es el prestamista (partner)
    @Query("SELECT l FROM Loan l WHERE l.offer.partnerId = :partnerId AND l.currentStatus = 'al_dia'")
    List<Loan> findActiveLoansByPartnerId(@Param("partnerId") long partnerId);

    List<Loan> findByLoanAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    @Query("SELECT l FROM Loan l WHERE l.offer.solicitude.borrower.id = :userId AND l.currentStatus = 'al_dia'")
    List<Loan> findActiveLoansByUserId(@Param("userId") Long userId);

    // Métodos para contar préstamos
    Integer countByCurrentStatus(String currentStatus);

    // Métodos adicionales para filtros por fechas
    List<Loan> findByStartDateBetween(Instant startDate, Instant endDate);

    List<Loan> findByEndDateBetween(Instant startDate, Instant endDate);

    // Métodos para buscar por oferta
    List<Loan> findByOfferId(Long offerId);

    // Métodos para búsqueda combinada
    List<Loan> findByCurrentStatusAndLoanAmountGreaterThan(String status, BigDecimal minAmount);

    // Método para encontrar por estado
    List<Loan> findByCurrentStatus(String status);

    @Query(
        value = "SELECT l.* FROM loan l " +
        "JOIN offer o ON l.id_offer = o.id " +
        "WHERE o.partner_id = :partnerId " +
        "ORDER BY l.start_date DESC",
        nativeQuery = true
    )
    List<Loan> findLoansByPartnerId(@Param("partnerId") Long partnerId);

    // Consulta mejorada usando la relación directa
    @Query("SELECT l FROM Loan l JOIN l.offer o WHERE o.partner.id = :partnerId ORDER BY l.startDate DESC")
    List<Loan> findByPartnerIdOrderByStartDateDesc(@Param("partnerId") Long partnerId);

    // Con filtro de estado
    @Query("SELECT l FROM Loan l JOIN l.offer o WHERE o.partner.id = :partnerId AND l.currentStatus = :status ORDER BY l.startDate DESC")
    List<Loan> findByPartnerIdAndStatusOrderByStartDateDesc(@Param("partnerId") Long partnerId, @Param("status") String status);
}
