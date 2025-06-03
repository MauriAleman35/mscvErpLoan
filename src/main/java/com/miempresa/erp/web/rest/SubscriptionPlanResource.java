package com.miempresa.erp.web.rest;

import com.miempresa.erp.domain.SubscriptionPlan;
import com.miempresa.erp.repository.SubscriptionPlanRepository;
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
 * REST controller for managing {@link com.miempresa.erp.domain.SubscriptionPlan}.
 */
@RestController
@RequestMapping("/api/subscription-plans")
@Transactional
public class SubscriptionPlanResource {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionPlanResource.class);

    private static final String ENTITY_NAME = "erpSubscriptionPlan";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanResource(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    /**
     * {@code POST  /subscription-plans} : Create a new subscriptionPlan.
     *
     * @param subscriptionPlan the subscriptionPlan to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new subscriptionPlan, or with status {@code 400 (Bad Request)} if the subscriptionPlan has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SubscriptionPlan> createSubscriptionPlan(@Valid @RequestBody SubscriptionPlan subscriptionPlan)
        throws URISyntaxException {
        LOG.debug("REST request to save SubscriptionPlan : {}", subscriptionPlan);
        if (subscriptionPlan.getId() != null) {
            throw new BadRequestAlertException("A new subscriptionPlan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        subscriptionPlan = subscriptionPlanRepository.save(subscriptionPlan);
        return ResponseEntity.created(new URI("/api/subscription-plans/" + subscriptionPlan.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, subscriptionPlan.getId().toString()))
            .body(subscriptionPlan);
    }

    /**
     * {@code PUT  /subscription-plans/:id} : Updates an existing subscriptionPlan.
     *
     * @param id the id of the subscriptionPlan to save.
     * @param subscriptionPlan the subscriptionPlan to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated subscriptionPlan,
     * or with status {@code 400 (Bad Request)} if the subscriptionPlan is not valid,
     * or with status {@code 500 (Internal Server Error)} if the subscriptionPlan couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> updateSubscriptionPlan(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SubscriptionPlan subscriptionPlan
    ) throws URISyntaxException {
        LOG.debug("REST request to update SubscriptionPlan : {}, {}", id, subscriptionPlan);
        if (subscriptionPlan.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, subscriptionPlan.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!subscriptionPlanRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        subscriptionPlan = subscriptionPlanRepository.save(subscriptionPlan);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, subscriptionPlan.getId().toString()))
            .body(subscriptionPlan);
    }

    /**
     * {@code PATCH  /subscription-plans/:id} : Partial updates given fields of an existing subscriptionPlan, field will ignore if it is null
     *
     * @param id the id of the subscriptionPlan to save.
     * @param subscriptionPlan the subscriptionPlan to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated subscriptionPlan,
     * or with status {@code 400 (Bad Request)} if the subscriptionPlan is not valid,
     * or with status {@code 404 (Not Found)} if the subscriptionPlan is not found,
     * or with status {@code 500 (Internal Server Error)} if the subscriptionPlan couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SubscriptionPlan> partialUpdateSubscriptionPlan(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SubscriptionPlan subscriptionPlan
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SubscriptionPlan partially : {}, {}", id, subscriptionPlan);
        if (subscriptionPlan.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, subscriptionPlan.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!subscriptionPlanRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SubscriptionPlan> result = subscriptionPlanRepository
            .findById(subscriptionPlan.getId())
            .map(existingSubscriptionPlan -> {
                if (subscriptionPlan.getName() != null) {
                    existingSubscriptionPlan.setName(subscriptionPlan.getName());
                }
                if (subscriptionPlan.getDescription() != null) {
                    existingSubscriptionPlan.setDescription(subscriptionPlan.getDescription());
                }
                if (subscriptionPlan.getMonthlyPrice() != null) {
                    existingSubscriptionPlan.setMonthlyPrice(subscriptionPlan.getMonthlyPrice());
                }
                if (subscriptionPlan.getAnnualPrice() != null) {
                    existingSubscriptionPlan.setAnnualPrice(subscriptionPlan.getAnnualPrice());
                }
                if (subscriptionPlan.getStatus() != null) {
                    existingSubscriptionPlan.setStatus(subscriptionPlan.getStatus());
                }

                return existingSubscriptionPlan;
            })
            .map(subscriptionPlanRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, subscriptionPlan.getId().toString())
        );
    }

    /**
     * {@code GET  /subscription-plans} : get all the subscriptionPlans.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of subscriptionPlans in body.
     */
    @GetMapping("")
    public List<SubscriptionPlan> getAllSubscriptionPlans() {
        LOG.debug("REST request to get all SubscriptionPlans");
        return subscriptionPlanRepository.findAll();
    }

    /**
     * {@code GET  /subscription-plans/:id} : get the "id" subscriptionPlan.
     *
     * @param id the id of the subscriptionPlan to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the subscriptionPlan, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> getSubscriptionPlan(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SubscriptionPlan : {}", id);
        Optional<SubscriptionPlan> subscriptionPlan = subscriptionPlanRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(subscriptionPlan);
    }

    /**
     * {@code DELETE  /subscription-plans/:id} : delete the "id" subscriptionPlan.
     *
     * @param id the id of the subscriptionPlan to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscriptionPlan(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SubscriptionPlan : {}", id);
        subscriptionPlanRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
