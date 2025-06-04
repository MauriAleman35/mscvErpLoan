package com.miempresa.erp.repository;

import com.miempresa.erp.domain.Document;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.domain.UserSubscription;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the UserSubscription entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {}
