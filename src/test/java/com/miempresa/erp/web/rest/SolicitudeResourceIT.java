package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.SolicitudeAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.miempresa.erp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.Solicitude;
import com.miempresa.erp.repository.SolicitudeRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SolicitudeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SolicitudeResourceIT {

    private static final BigDecimal DEFAULT_LOAN_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_LOAN_AMOUNT = new BigDecimal(2);

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/solicitudes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SolicitudeRepository solicitudeRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSolicitudeMockMvc;

    private Solicitude solicitude;

    private Solicitude insertedSolicitude;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Solicitude createEntity() {
        return new Solicitude().loanAmount(DEFAULT_LOAN_AMOUNT).status(DEFAULT_STATUS).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Solicitude createUpdatedEntity() {
        return new Solicitude().loanAmount(UPDATED_LOAN_AMOUNT).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        solicitude = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSolicitude != null) {
            solicitudeRepository.delete(insertedSolicitude);
            insertedSolicitude = null;
        }
    }

    @Test
    @Transactional
    void createSolicitude() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Solicitude
        var returnedSolicitude = om.readValue(
            restSolicitudeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Solicitude.class
        );

        // Validate the Solicitude in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSolicitudeUpdatableFieldsEquals(returnedSolicitude, getPersistedSolicitude(returnedSolicitude));

        insertedSolicitude = returnedSolicitude;
    }

    @Test
    @Transactional
    void createSolicitudeWithExistingId() throws Exception {
        // Create the Solicitude with an existing ID
        solicitude.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSolicitudeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude)))
            .andExpect(status().isBadRequest());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLoanAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        solicitude.setLoanAmount(null);

        // Create the Solicitude, which fails.

        restSolicitudeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        solicitude.setStatus(null);

        // Create the Solicitude, which fails.

        restSolicitudeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        solicitude.setCreatedAt(null);

        // Create the Solicitude, which fails.

        restSolicitudeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSolicitudes() throws Exception {
        // Initialize the database
        insertedSolicitude = solicitudeRepository.saveAndFlush(solicitude);

        // Get all the solicitudeList
        restSolicitudeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(solicitude.getId().intValue())))
            .andExpect(jsonPath("$.[*].loanAmount").value(hasItem(sameNumber(DEFAULT_LOAN_AMOUNT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getSolicitude() throws Exception {
        // Initialize the database
        insertedSolicitude = solicitudeRepository.saveAndFlush(solicitude);

        // Get the solicitude
        restSolicitudeMockMvc
            .perform(get(ENTITY_API_URL_ID, solicitude.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(solicitude.getId().intValue()))
            .andExpect(jsonPath("$.loanAmount").value(sameNumber(DEFAULT_LOAN_AMOUNT)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingSolicitude() throws Exception {
        // Get the solicitude
        restSolicitudeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSolicitude() throws Exception {
        // Initialize the database
        insertedSolicitude = solicitudeRepository.saveAndFlush(solicitude);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the solicitude
        Solicitude updatedSolicitude = solicitudeRepository.findById(solicitude.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSolicitude are not directly saved in db
        em.detach(updatedSolicitude);
        updatedSolicitude.loanAmount(UPDATED_LOAN_AMOUNT).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);

        restSolicitudeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSolicitude.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSolicitude))
            )
            .andExpect(status().isOk());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSolicitudeToMatchAllProperties(updatedSolicitude);
    }

    @Test
    @Transactional
    void putNonExistingSolicitude() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        solicitude.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSolicitudeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, solicitude.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSolicitude() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        solicitude.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSolicitudeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(solicitude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSolicitude() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        solicitude.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSolicitudeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(solicitude)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSolicitudeWithPatch() throws Exception {
        // Initialize the database
        insertedSolicitude = solicitudeRepository.saveAndFlush(solicitude);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the solicitude using partial update
        Solicitude partialUpdatedSolicitude = new Solicitude();
        partialUpdatedSolicitude.setId(solicitude.getId());

        partialUpdatedSolicitude.loanAmount(UPDATED_LOAN_AMOUNT).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);

        restSolicitudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSolicitude.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSolicitude))
            )
            .andExpect(status().isOk());

        // Validate the Solicitude in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSolicitudeUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSolicitude, solicitude),
            getPersistedSolicitude(solicitude)
        );
    }

    @Test
    @Transactional
    void fullUpdateSolicitudeWithPatch() throws Exception {
        // Initialize the database
        insertedSolicitude = solicitudeRepository.saveAndFlush(solicitude);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the solicitude using partial update
        Solicitude partialUpdatedSolicitude = new Solicitude();
        partialUpdatedSolicitude.setId(solicitude.getId());

        partialUpdatedSolicitude.loanAmount(UPDATED_LOAN_AMOUNT).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);

        restSolicitudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSolicitude.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSolicitude))
            )
            .andExpect(status().isOk());

        // Validate the Solicitude in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSolicitudeUpdatableFieldsEquals(partialUpdatedSolicitude, getPersistedSolicitude(partialUpdatedSolicitude));
    }

    @Test
    @Transactional
    void patchNonExistingSolicitude() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        solicitude.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSolicitudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, solicitude.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(solicitude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSolicitude() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        solicitude.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSolicitudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(solicitude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSolicitude() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        solicitude.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSolicitudeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(solicitude)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Solicitude in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSolicitude() throws Exception {
        // Initialize the database
        insertedSolicitude = solicitudeRepository.saveAndFlush(solicitude);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the solicitude
        restSolicitudeMockMvc
            .perform(delete(ENTITY_API_URL_ID, solicitude.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return solicitudeRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Solicitude getPersistedSolicitude(Solicitude solicitude) {
        return solicitudeRepository.findById(solicitude.getId()).orElseThrow();
    }

    protected void assertPersistedSolicitudeToMatchAllProperties(Solicitude expectedSolicitude) {
        assertSolicitudeAllPropertiesEquals(expectedSolicitude, getPersistedSolicitude(expectedSolicitude));
    }

    protected void assertPersistedSolicitudeToMatchUpdatableProperties(Solicitude expectedSolicitude) {
        assertSolicitudeAllUpdatablePropertiesEquals(expectedSolicitude, getPersistedSolicitude(expectedSolicitude));
    }
}
