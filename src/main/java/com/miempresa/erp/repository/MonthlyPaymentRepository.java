package com.miempresa.erp.repository;

import com.miempresa.erp.domain.MonthlyPayment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MonthlyPayment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {}
