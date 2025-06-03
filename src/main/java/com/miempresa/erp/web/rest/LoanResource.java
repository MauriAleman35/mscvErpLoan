package com.miempresa.erp.web.rest;

import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.repository.LoanRepository;
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
 * REST controller for managing {@link com.miempresa.erp.domain.Loan}.
 */
@RestController
@RequestMapping("/api/loans")
@Transactional
public class LoanResource {

    private static final Logger LOG = LoggerFactory.getLogger(LoanResource.class);

    private static final String ENTITY_NAME = "erpLoan";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LoanRepository loanRepository;

    public LoanResource(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    /**
     * {@code POST  /loans} : Create a new loan.
     *
     * @param loan the loan to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new loan, or with status {@code 400 (Bad Request)} if the loan has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody Loan loan) throws URISyntaxException {
        LOG.debug("REST request to save Loan : {}", loan);
        if (loan.getId() != null) {
            throw new BadRequestAlertException("A new loan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        loan = loanRepository.save(loan);
        return ResponseEntity.created(new URI("/api/loans/" + loan.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, loan.getId().toString()))
            .body(loan);
    }

    /**
     * {@code PUT  /loans/:id} : Updates an existing loan.
     *
     * @param id the id of the loan to save.
     * @param loan the loan to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loan,
     * or with status {@code 400 (Bad Request)} if the loan is not valid,
     * or with status {@code 500 (Internal Server Error)} if the loan couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Loan loan)
        throws URISyntaxException {
        LOG.debug("REST request to update Loan : {}, {}", id, loan);
        if (loan.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, loan.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!loanRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        loan = loanRepository.save(loan);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, loan.getId().toString()))
            .body(loan);
    }

    /**
     * {@code PATCH  /loans/:id} : Partial updates given fields of an existing loan, field will ignore if it is null
     *
     * @param id the id of the loan to save.
     * @param loan the loan to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loan,
     * or with status {@code 400 (Bad Request)} if the loan is not valid,
     * or with status {@code 404 (Not Found)} if the loan is not found,
     * or with status {@code 500 (Internal Server Error)} if the loan couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Loan> partialUpdateLoan(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Loan loan
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Loan partially : {}, {}", id, loan);
        if (loan.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, loan.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!loanRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Loan> result = loanRepository
            .findById(loan.getId())
            .map(existingLoan -> {
                if (loan.getLoanAmount() != null) {
                    existingLoan.setLoanAmount(loan.getLoanAmount());
                }
                if (loan.getStartDate() != null) {
                    existingLoan.setStartDate(loan.getStartDate());
                }
                if (loan.getEndDate() != null) {
                    existingLoan.setEndDate(loan.getEndDate());
                }
                if (loan.getHashBlockchain() != null) {
                    existingLoan.setHashBlockchain(loan.getHashBlockchain());
                }
                if (loan.getCurrentStatus() != null) {
                    existingLoan.setCurrentStatus(loan.getCurrentStatus());
                }
                if (loan.getLatePaymentCount() != null) {
                    existingLoan.setLatePaymentCount(loan.getLatePaymentCount());
                }
                if (loan.getLastStatusUpdate() != null) {
                    existingLoan.setLastStatusUpdate(loan.getLastStatusUpdate());
                }

                return existingLoan;
            })
            .map(loanRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, loan.getId().toString())
        );
    }

    /**
     * {@code GET  /loans} : get all the loans.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of loans in body.
     */
    @GetMapping("")
    public List<Loan> getAllLoans() {
        LOG.debug("REST request to get all Loans");
        return loanRepository.findAll();
    }

    /**
     * {@code GET  /loans/:id} : get the "id" loan.
     *
     * @param id the id of the loan to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the loan, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Loan : {}", id);
        Optional<Loan> loan = loanRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(loan);
    }

    /**
     * {@code DELETE  /loans/:id} : delete the "id" loan.
     *
     * @param id the id of the loan to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Loan : {}", id);
        loanRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
