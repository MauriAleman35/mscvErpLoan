package com.miempresa.erp.repository;

import com.miempresa.erp.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface UserRepositoryWithBagRelationships {
    Optional<User> fetchBagRelationships(Optional<User> user);

    List<User> fetchBagRelationships(List<User> users);

    Page<User> fetchBagRelationships(Page<User> users);
}
