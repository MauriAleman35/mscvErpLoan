package com.miempresa.erp.web.rest;

import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.repository.OfferRepository;
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
 * REST controller for managing {@link com.miempresa.erp.domain.Offer}.
 */
@RestController
@RequestMapping("/api/offers")
@Transactional
public class OfferResource {

    private static final Logger LOG = LoggerFactory.getLogger(OfferResource.class);

    private static final String ENTITY_NAME = "erpOffer";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OfferRepository offerRepository;

    public OfferResource(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    /**
     * {@code POST  /offers} : Create a new offer.
     *
     * @param offer the offer to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new offer, or with status {@code 400 (Bad Request)} if the offer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Offer> createOffer(@Valid @RequestBody Offer offer) throws URISyntaxException {
        LOG.debug("REST request to save Offer : {}", offer);
        if (offer.getId() != null) {
            throw new BadRequestAlertException("A new offer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        offer = offerRepository.save(offer);
        return ResponseEntity.created(new URI("/api/offers/" + offer.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, offer.getId().toString()))
            .body(offer);
    }

    /**
     * {@code PUT  /offers/:id} : Updates an existing offer.
     *
     * @param id the id of the offer to save.
     * @param offer the offer to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offer,
     * or with status {@code 400 (Bad Request)} if the offer is not valid,
     * or with status {@code 500 (Internal Server Error)} if the offer couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Offer offer)
        throws URISyntaxException {
        LOG.debug("REST request to update Offer : {}, {}", id, offer);
        if (offer.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, offer.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!offerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        offer = offerRepository.save(offer);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, offer.getId().toString()))
            .body(offer);
    }

    /**
     * {@code PATCH  /offers/:id} : Partial updates given fields of an existing offer, field will ignore if it is null
     *
     * @param id the id of the offer to save.
     * @param offer the offer to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offer,
     * or with status {@code 400 (Bad Request)} if the offer is not valid,
     * or with status {@code 404 (Not Found)} if the offer is not found,
     * or with status {@code 500 (Internal Server Error)} if the offer couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Offer> partialUpdateOffer(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Offer offer
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Offer partially : {}, {}", id, offer);
        if (offer.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, offer.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!offerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Offer> result = offerRepository
            .findById(offer.getId())
            .map(existingOffer -> {
                if (offer.getPartnerId() != null) {
                    existingOffer.setPartnerId(offer.getPartnerId());
                }
                if (offer.getInterest() != null) {
                    existingOffer.setInterest(offer.getInterest());
                }
                if (offer.getLoanTerm() != null) {
                    existingOffer.setLoanTerm(offer.getLoanTerm());
                }
                if (offer.getMonthlyPayment() != null) {
                    existingOffer.setMonthlyPayment(offer.getMonthlyPayment());
                }
                if (offer.getTotalRepaymentAmount() != null) {
                    existingOffer.setTotalRepaymentAmount(offer.getTotalRepaymentAmount());
                }
                if (offer.getStatus() != null) {
                    existingOffer.setStatus(offer.getStatus());
                }
                if (offer.getCreatedAt() != null) {
                    existingOffer.setCreatedAt(offer.getCreatedAt());
                }

                return existingOffer;
            })
            .map(offerRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, offer.getId().toString())
        );
    }

    /**
     * {@code GET  /offers} : get all the offers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of offers in body.
     */
    @GetMapping("")
    public List<Offer> getAllOffers() {
        LOG.debug("REST request to get all Offers");
        return offerRepository.findAll();
    }

    /**
     * {@code GET  /offers/:id} : get the "id" offer.
     *
     * @param id the id of the offer to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the offer, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOffer(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Offer : {}", id);
        Optional<Offer> offer = offerRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(offer);
    }

    /**
     * {@code DELETE  /offers/:id} : delete the "id" offer.
     *
     * @param id the id of the offer to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Offer : {}", id);
        offerRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
