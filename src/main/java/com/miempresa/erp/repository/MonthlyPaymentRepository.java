package com.miempresa.erp.repository;

import com.miempresa.erp.domain.MonthlyPayment;
import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {
    /**
     * Spring Data JPA repository for the MonthlyPayment entity.
     */
    @SuppressWarnings("unused")
    List<MonthlyPayment> findByLoanId(Long loanId);

    List<MonthlyPayment> findByDueDateBefore(Instant date);

    List<MonthlyPayment> findByBorrowVerified(Boolean verified);

    List<MonthlyPayment> findByPartnerVerified(Boolean verified);

    List<MonthlyPayment> findByPaymentStatus(String status);

    List<MonthlyPayment> findByDaysLateGreaterThan(Integer days);

    List<MonthlyPayment> findByLoanIdOrderByDueDateAsc(Long loanId);

    @Query(
        value = "SELECT mp.id, mp.due_date, mp.payment_date, mp.payment_status, mp.comprobant_file, " +
        "mp.penalty_amount, o.monthly_payment as expected_payment, " +
        "RANK() OVER (PARTITION BY l.id ORDER BY mp.due_date) as cuota_number, " +
        "o.loan_term as total_cuotas " +
        "FROM monthly_payment mp " +
        "JOIN loan l ON mp.id_loan = l.id " +
        "JOIN offer o ON l.id_offer = o.id " +
        "JOIN solicitude s ON o.id_solicitude = s.id " +
        "WHERE s.borrower_id = :userId " +
        "AND mp.payment_status = :status " +
        "ORDER BY l.id, mp.due_date ASC",
        nativeQuery = true
    )
    List<Object[]> findPaidPaymentsByUserId(@Param("userId") Long userId, @Param("status") String status);

    Long countByLoanIdAndPaymentStatusNot(Long loanId, String verificar);
    Long countByLoanIdAndDueDateBeforeAndPaymentStatusNot(Long loanId, Instant instant, String verificar);
    List<MonthlyPayment> findByLoanOfferPartnerIdAndPaymentStatusAndPartnerVerifiedOrderByPaymentDateAsc(
        Long partnerId,
        String status,
        Boolean partnerVerified
    );
    Boolean existsByLoanIdAndPaymentStatusAndDueDateBefore(Long payment_id, String status, Instant due_date);

    List<MonthlyPayment> findByLoanIdAndPaymentStatusOrderByDueDateAsc(Long loanId, String status);

    @Query(
        "SELECT mp FROM MonthlyPayment mp " +
        "JOIN mp.loan l JOIN l.offer o " +
        "WHERE o.partnerId = :partnerId AND mp.partnerVerified = true " +
        "ORDER BY mp.dueDate DESC"
    )
    List<MonthlyPayment> findVerifiedPaymentsByPartnerIdOrderByDueDateDesc(@Param("partnerId") String partnerId);

    @Query(
        "SELECT mp FROM MonthlyPayment mp " +
        "JOIN mp.loan l " +
        "JOIN l.offer o " +
        "WHERE o.partner.id = :partnerId AND mp.paymentStatus = :status AND mp.partnerVerified = false " +
        "ORDER BY mp.paymentDate ASC"
    )
    List<MonthlyPayment> findPaymentsToVerifyByPartnerId(@Param("partnerId") Long partnerId, @Param("status") String status);

    // Pagos por préstamo y estado
    @Query("SELECT mp FROM MonthlyPayment mp " + "WHERE mp.loan.id = :loanId AND mp.paymentStatus = :status " + "ORDER BY mp.dueDate ASC")
    List<MonthlyPayment> findByLoanIdAndPaymentStatus(@Param("loanId") Long loanId, @Param("status") String status);
}
