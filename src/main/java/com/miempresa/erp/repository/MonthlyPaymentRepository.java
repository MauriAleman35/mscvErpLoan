package com.miempresa.erp.repository;

import com.miempresa.erp.domain.MonthlyPayment;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MonthlyPayment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {
    List<MonthlyPayment> findByLoanId(Long loanId);

    List<MonthlyPayment> findByDueDateBefore(Instant date);

    List<MonthlyPayment> findByBorrowVerified(Boolean verified);

    List<MonthlyPayment> findByPartnerVerified(Boolean verified);

    List<MonthlyPayment> findByPaymentStatus(String status);

    List<MonthlyPayment> findByDaysLateGreaterThan(Integer days);
}
