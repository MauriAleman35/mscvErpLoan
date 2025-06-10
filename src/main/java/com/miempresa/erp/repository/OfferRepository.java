package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.domain.Solicitude;
import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Offer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Override
    List<Offer> findAll();

    @Query(
        "SELECT o FROM Offer o " +
        "LEFT JOIN FETCH o.solicitude s " +
        "LEFT JOIN FETCH s.borrower " +
        "WHERE o.partnerId = :partnerId " +
        "ORDER BY o.createdAt DESC"
    )
    List<Offer> findByPartnerIdOrderByCreatedAtDesc(@Param("partnerId") String partnerId);

    List<Offer> findBySolicitudeIdAndStatus(Long solicitudeId, String status);
    List<Offer> findByStatus(String status);

    List<Offer> findByPartnerId(Integer partnerId);

    List<Offer> findBySolicitudeId(Long solicitudeId);

    Integer countByStatus(String status);

    List<Offer> findByInterestLessThan(BigDecimal maxInterest);

    List<Offer> findBySolicitudeAndStatusNot(Solicitude solicitude, String status);
}
