package com.miempresa.erp.web.rest;

import com.miempresa.erp.domain.UserSubscription;
import com.miempresa.erp.repository.UserSubscriptionRepository;
import com.miempresa.erp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.miempresa.erp.domain.UserSubscription}.
 */
@RestController
@RequestMapping("/api/user-subscriptions")
@Transactional
public class UserSubscriptionResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserSubscriptionResource.class);

    private static final String ENTITY_NAME = "erpUserSubscription";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionResource(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    /**
     * {@code POST  /user-subscriptions} : Create a new userSubscription.
     *
     * @param userSubscription the userSubscription to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userSubscription, or with status {@code 400 (Bad Request)} if the userSubscription has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<UserSubscription> createUserSubscription(@Valid @RequestBody UserSubscription userSubscription)
        throws URISyntaxException {
        LOG.debug("REST request to save UserSubscription : {}", userSubscription);
        if (userSubscription.getId() != null) {
            throw new BadRequestAlertException("A new userSubscription cannot already have an ID", ENTITY_NAME, "idexists");
        }
        userSubscription = userSubscriptionRepository.save(userSubscription);
        return ResponseEntity.created(new URI("/api/user-subscriptions/" + userSubscription.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, userSubscription.getId().toString()))
            .body(userSubscription);
    }

    /**
     * {@code PUT  /user-subscriptions/:id} : Updates an existing userSubscription.
     *
     * @param id the id of the userSubscription to save.
     * @param userSubscription the userSubscription to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userSubscription,
     * or with status {@code 400 (Bad Request)} if the userSubscription is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userSubscription couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserSubscription> updateUserSubscription(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody UserSubscription userSubscription
    ) throws URISyntaxException {
        LOG.debug("REST request to update UserSubscription : {}, {}", id, userSubscription);
        if (userSubscription.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userSubscription.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!userSubscriptionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        userSubscription = userSubscriptionRepository.save(userSubscription);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userSubscription.getId().toString()))
            .body(userSubscription);
    }

    /**
     * {@code PATCH  /user-subscriptions/:id} : Partial updates given fields of an existing userSubscription, field will ignore if it is null
     *
     * @param id the id of the userSubscription to save.
     * @param userSubscription the userSubscription to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userSubscription,
     * or with status {@code 400 (Bad Request)} if the userSubscription is not valid,
     * or with status {@code 404 (Not Found)} if the userSubscription is not found,
     * or with status {@code 500 (Internal Server Error)} if the userSubscription couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<UserSubscription> partialUpdateUserSubscription(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody UserSubscription userSubscription
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update UserSubscription partially : {}, {}", id, userSubscription);
        if (userSubscription.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userSubscription.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!userSubscriptionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<UserSubscription> result = userSubscriptionRepository
            .findById(userSubscription.getId())
            .map(existingUserSubscription -> {
                if (userSubscription.getStartDate() != null) {
                    existingUserSubscription.setStartDate(userSubscription.getStartDate());
                }
                if (userSubscription.getEndDate() != null) {
                    existingUserSubscription.setEndDate(userSubscription.getEndDate());
                }
                if (userSubscription.getStatus() != null) {
                    existingUserSubscription.setStatus(userSubscription.getStatus());
                }

                return existingUserSubscription;
            })
            .map(userSubscriptionRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userSubscription.getId().toString())
        );
    }

    /**
     * {@code GET  /user-subscriptions} : get all the userSubscriptions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userSubscriptions in body.
     */
    @GetMapping("")
    public List<UserSubscription> getAllUserSubscriptions() {
        LOG.debug("REST request to get all UserSubscriptions");
        return userSubscriptionRepository.findAll();
    }

    /**
     * {@code GET  /user-subscriptions/:id} : get the "id" userSubscription.
     *
     * @param id the id of the userSubscription to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userSubscription, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserSubscription> getUserSubscription(@PathVariable("id") Long id) {
        LOG.debug("REST request to get UserSubscription : {}", id);
        Optional<UserSubscription> userSubscription = userSubscriptionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(userSubscription);
    }

    /**
     * {@code DELETE  /user-subscriptions/:id} : delete the "id" userSubscription.
     *
     * @param id the id of the userSubscription to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserSubscription(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete UserSubscription : {}", id);
        userSubscriptionRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
