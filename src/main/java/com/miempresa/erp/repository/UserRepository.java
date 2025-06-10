package com.miempresa.erp.repository;

import com.miempresa.erp.domain.User;
import com.miempresa.erp.dto.BorrowerStats;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 *
 * When extending this class, extend UserRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    // Método usado en userByEmail query
    Optional<User> findOneByEmail(String email);
    Optional<User> findByEmail(String email);
    // Métodos para filtrar usuarios
    List<User> findByEmailContaining(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByUserType(String userType);

    List<User> findByStatus(String status);
}
