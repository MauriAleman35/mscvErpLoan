package com.miempresa.erp.web.rest;

import com.miempresa.erp.domain.MonthlyPayment;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
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
 * REST controller for managing {@link com.miempresa.erp.domain.MonthlyPayment}.
 */
@RestController
@RequestMapping("/api/monthly-payments")
@Transactional
public class MonthlyPaymentResource {

    private static final Logger LOG = LoggerFactory.getLogger(MonthlyPaymentResource.class);

    private static final String ENTITY_NAME = "erpMonthlyPayment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MonthlyPaymentRepository monthlyPaymentRepository;

    public MonthlyPaymentResource(MonthlyPaymentRepository monthlyPaymentRepository) {
        this.monthlyPaymentRepository = monthlyPaymentRepository;
    }

    /**
     * {@code POST  /monthly-payments} : Create a new monthlyPayment.
     *
     * @param monthlyPayment the monthlyPayment to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new monthlyPayment, or with status {@code 400 (Bad Request)} if the monthlyPayment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MonthlyPayment> createMonthlyPayment(@Valid @RequestBody MonthlyPayment monthlyPayment)
        throws URISyntaxException {
        LOG.debug("REST request to save MonthlyPayment : {}", monthlyPayment);
        if (monthlyPayment.getId() != null) {
            throw new BadRequestAlertException("A new monthlyPayment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        monthlyPayment = monthlyPaymentRepository.save(monthlyPayment);
        return ResponseEntity.created(new URI("/api/monthly-payments/" + monthlyPayment.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, monthlyPayment.getId().toString()))
            .body(monthlyPayment);
    }

    /**
     * {@code PUT  /monthly-payments/:id} : Updates an existing monthlyPayment.
     *
     * @param id the id of the monthlyPayment to save.
     * @param monthlyPayment the monthlyPayment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated monthlyPayment,
     * or with status {@code 400 (Bad Request)} if the monthlyPayment is not valid,
     * or with status {@code 500 (Internal Server Error)} if the monthlyPayment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MonthlyPayment> updateMonthlyPayment(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MonthlyPayment monthlyPayment
    ) throws URISyntaxException {
        LOG.debug("REST request to update MonthlyPayment : {}, {}", id, monthlyPayment);
        if (monthlyPayment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, monthlyPayment.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!monthlyPaymentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        monthlyPayment = monthlyPaymentRepository.save(monthlyPayment);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, monthlyPayment.getId().toString()))
            .body(monthlyPayment);
    }

    /**
     * {@code PATCH  /monthly-payments/:id} : Partial updates given fields of an existing monthlyPayment, field will ignore if it is null
     *
     * @param id the id of the monthlyPayment to save.
     * @param monthlyPayment the monthlyPayment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated monthlyPayment,
     * or with status {@code 400 (Bad Request)} if the monthlyPayment is not valid,
     * or with status {@code 404 (Not Found)} if the monthlyPayment is not found,
     * or with status {@code 500 (Internal Server Error)} if the monthlyPayment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MonthlyPayment> partialUpdateMonthlyPayment(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MonthlyPayment monthlyPayment
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MonthlyPayment partially : {}, {}", id, monthlyPayment);
        if (monthlyPayment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, monthlyPayment.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!monthlyPaymentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MonthlyPayment> result = monthlyPaymentRepository
            .findById(monthlyPayment.getId())
            .map(existingMonthlyPayment -> {
                if (monthlyPayment.getDueDate() != null) {
                    existingMonthlyPayment.setDueDate(monthlyPayment.getDueDate());
                }
                if (monthlyPayment.getPaymentDate() != null) {
                    existingMonthlyPayment.setPaymentDate(monthlyPayment.getPaymentDate());
                }
                if (monthlyPayment.getBorrowVerified() != null) {
                    existingMonthlyPayment.setBorrowVerified(monthlyPayment.getBorrowVerified());
                }
                if (monthlyPayment.getPartnerVerified() != null) {
                    existingMonthlyPayment.setPartnerVerified(monthlyPayment.getPartnerVerified());
                }
                if (monthlyPayment.getComprobantFile() != null) {
                    existingMonthlyPayment.setComprobantFile(monthlyPayment.getComprobantFile());
                }
                if (monthlyPayment.getDaysLate() != null) {
                    existingMonthlyPayment.setDaysLate(monthlyPayment.getDaysLate());
                }
                if (monthlyPayment.getPenaltyAmount() != null) {
                    existingMonthlyPayment.setPenaltyAmount(monthlyPayment.getPenaltyAmount());
                }
                if (monthlyPayment.getPaymentStatus() != null) {
                    existingMonthlyPayment.setPaymentStatus(monthlyPayment.getPaymentStatus());
                }

                return existingMonthlyPayment;
            })
            .map(monthlyPaymentRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, monthlyPayment.getId().toString())
        );
    }

    /**
     * {@code GET  /monthly-payments} : get all the monthlyPayments.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of monthlyPayments in body.
     */
    @GetMapping("")
    public List<MonthlyPayment> getAllMonthlyPayments() {
        LOG.debug("REST request to get all MonthlyPayments");
        return monthlyPaymentRepository.findAll();
    }

    /**
     * {@code GET  /monthly-payments/:id} : get the "id" monthlyPayment.
     *
     * @param id the id of the monthlyPayment to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the monthlyPayment, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MonthlyPayment> getMonthlyPayment(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MonthlyPayment : {}", id);
        Optional<MonthlyPayment> monthlyPayment = monthlyPaymentRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(monthlyPayment);
    }

    /**
     * {@code DELETE  /monthly-payments/:id} : delete the "id" monthlyPayment.
     *
     * @param id the id of the monthlyPayment to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonthlyPayment(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MonthlyPayment : {}", id);
        monthlyPaymentRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
