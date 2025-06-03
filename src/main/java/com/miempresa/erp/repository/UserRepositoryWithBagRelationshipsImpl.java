package com.miempresa.erp.repository;

import com.miempresa.erp.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class UserRepositoryWithBagRelationshipsImpl implements UserRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String USERS_PARAMETER = "users";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> fetchBagRelationships(Optional<User> user) {
        return user.map(this::fetchRoles);
    }

    @Override
    public Page<User> fetchBagRelationships(Page<User> users) {
        return new PageImpl<>(fetchBagRelationships(users.getContent()), users.getPageable(), users.getTotalElements());
    }

    @Override
    public List<User> fetchBagRelationships(List<User> users) {
        return Optional.of(users).map(this::fetchRoles).orElse(Collections.emptyList());
    }

    User fetchRoles(User result) {
        return entityManager
            .createQuery("select user from User user left join fetch user.roles where user.id = :id", User.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<User> fetchRoles(List<User> users) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, users.size()).forEach(index -> order.put(users.get(index).getId(), index));
        List<User> result = entityManager
            .createQuery("select user from User user left join fetch user.roles where user in :users", User.class)
            .setParameter(USERS_PARAMETER, users)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
