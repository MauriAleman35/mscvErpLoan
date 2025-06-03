package com.miempresa.erp.web.rest;

import com.miempresa.erp.domain.Solicitude;
import com.miempresa.erp.repository.SolicitudeRepository;
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
 * REST controller for managing {@link com.miempresa.erp.domain.Solicitude}.
 */
@RestController
@RequestMapping("/api/solicitudes")
@Transactional
public class SolicitudeResource {

    private static final Logger LOG = LoggerFactory.getLogger(SolicitudeResource.class);

    private static final String ENTITY_NAME = "erpSolicitude";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SolicitudeRepository solicitudeRepository;

    public SolicitudeResource(SolicitudeRepository solicitudeRepository) {
        this.solicitudeRepository = solicitudeRepository;
    }

    /**
     * {@code POST  /solicitudes} : Create a new solicitude.
     *
     * @param solicitude the solicitude to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new solicitude, or with status {@code 400 (Bad Request)} if the solicitude has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Solicitude> createSolicitude(@Valid @RequestBody Solicitude solicitude) throws URISyntaxException {
        LOG.debug("REST request to save Solicitude : {}", solicitude);
        if (solicitude.getId() != null) {
            throw new BadRequestAlertException("A new solicitude cannot already have an ID", ENTITY_NAME, "idexists");
        }
        solicitude = solicitudeRepository.save(solicitude);
        return ResponseEntity.created(new URI("/api/solicitudes/" + solicitude.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, solicitude.getId().toString()))
            .body(solicitude);
    }

    /**
     * {@code PUT  /solicitudes/:id} : Updates an existing solicitude.
     *
     * @param id the id of the solicitude to save.
     * @param solicitude the solicitude to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated solicitude,
     * or with status {@code 400 (Bad Request)} if the solicitude is not valid,
     * or with status {@code 500 (Internal Server Error)} if the solicitude couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Solicitude> updateSolicitude(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Solicitude solicitude
    ) throws URISyntaxException {
        LOG.debug("REST request to update Solicitude : {}, {}", id, solicitude);
        if (solicitude.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, solicitude.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!solicitudeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        solicitude = solicitudeRepository.save(solicitude);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, solicitude.getId().toString()))
            .body(solicitude);
    }

    /**
     * {@code PATCH  /solicitudes/:id} : Partial updates given fields of an existing solicitude, field will ignore if it is null
     *
     * @param id the id of the solicitude to save.
     * @param solicitude the solicitude to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated solicitude,
     * or with status {@code 400 (Bad Request)} if the solicitude is not valid,
     * or with status {@code 404 (Not Found)} if the solicitude is not found,
     * or with status {@code 500 (Internal Server Error)} if the solicitude couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Solicitude> partialUpdateSolicitude(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Solicitude solicitude
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Solicitude partially : {}, {}", id, solicitude);
        if (solicitude.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, solicitude.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!solicitudeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Solicitude> result = solicitudeRepository
            .findById(solicitude.getId())
            .map(existingSolicitude -> {
                if (solicitude.getLoanAmount() != null) {
                    existingSolicitude.setLoanAmount(solicitude.getLoanAmount());
                }
                if (solicitude.getStatus() != null) {
                    existingSolicitude.setStatus(solicitude.getStatus());
                }
                if (solicitude.getCreatedAt() != null) {
                    existingSolicitude.setCreatedAt(solicitude.getCreatedAt());
                }

                return existingSolicitude;
            })
            .map(solicitudeRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, solicitude.getId().toString())
        );
    }

    /**
     * {@code GET  /solicitudes} : get all the solicitudes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of solicitudes in body.
     */
    @GetMapping("")
    public List<Solicitude> getAllSolicitudes() {
        LOG.debug("REST request to get all Solicitudes");
        return solicitudeRepository.findAll();
    }

    /**
     * {@code GET  /solicitudes/:id} : get the "id" solicitude.
     *
     * @param id the id of the solicitude to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the solicitude, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Solicitude> getSolicitude(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Solicitude : {}", id);
        Optional<Solicitude> solicitude = solicitudeRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(solicitude);
    }

    /**
     * {@code DELETE  /solicitudes/:id} : delete the "id" solicitude.
     *
     * @param id the id of the solicitude to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolicitude(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Solicitude : {}", id);
        solicitudeRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
