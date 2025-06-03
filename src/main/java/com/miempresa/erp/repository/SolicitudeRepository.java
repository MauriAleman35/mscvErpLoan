package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Solicitude;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Solicitude entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SolicitudeRepository extends JpaRepository<Solicitude, Long> {
    @Query("select solicitude from Solicitude solicitude where solicitude.borrower.login = ?#{authentication.name}")
    List<Solicitude> findByBorrowerIsCurrentUser();
}
