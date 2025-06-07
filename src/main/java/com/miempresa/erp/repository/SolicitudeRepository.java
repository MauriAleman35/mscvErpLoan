package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Solicitude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Solicitude entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SolicitudeRepository extends JpaRepository<Solicitude, Long> {
    // Métodos para filtrar solicitudes

    // Método para buscar solicitudes recientes con paginación
    @Query("SELECT s FROM Solicitude s WHERE s.status = :status AND s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<Solicitude> findRecentByStatus(@Param("status") String status, @Param("startDate") LocalDateTime startDate, Pageable pageable);

    // Método para buscar solicitudes recientes sin paginación
    @Query("SELECT s FROM Solicitude s WHERE s.status = :status AND s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<Solicitude> findRecentByStatus(@Param("status") String status, @Param("startDate") LocalDateTime startDate);

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
