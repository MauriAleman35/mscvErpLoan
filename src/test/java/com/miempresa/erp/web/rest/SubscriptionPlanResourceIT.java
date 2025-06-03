package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.SubscriptionPlanAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.miempresa.erp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.SubscriptionPlan;
import com.miempresa.erp.repository.SubscriptionPlanRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link SubscriptionPlanResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SubscriptionPlanResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_MONTHLY_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_MONTHLY_PRICE = new BigDecimal(2);

    private static final BigDecimal DEFAULT_ANNUAL_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_ANNUAL_PRICE = new BigDecimal(2);

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/subscription-plans";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSubscriptionPlanMockMvc;

    private SubscriptionPlan subscriptionPlan;

    private SubscriptionPlan insertedSubscriptionPlan;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SubscriptionPlan createEntity() {
        return new SubscriptionPlan()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .monthlyPrice(DEFAULT_MONTHLY_PRICE)
            .annualPrice(DEFAULT_ANNUAL_PRICE)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SubscriptionPlan createUpdatedEntity() {
        return new SubscriptionPlan()
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .monthlyPrice(UPDATED_MONTHLY_PRICE)
            .annualPrice(UPDATED_ANNUAL_PRICE)
            .status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        subscriptionPlan = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSubscriptionPlan != null) {
            subscriptionPlanRepository.delete(insertedSubscriptionPlan);
            insertedSubscriptionPlan = null;
        }
    }

    @Test
    @Transactional
    void createSubscriptionPlan() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SubscriptionPlan
        var returnedSubscriptionPlan = om.readValue(
            restSubscriptionPlanMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SubscriptionPlan.class
        );

        // Validate the SubscriptionPlan in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSubscriptionPlanUpdatableFieldsEquals(returnedSubscriptionPlan, getPersistedSubscriptionPlan(returnedSubscriptionPlan));

        insertedSubscriptionPlan = returnedSubscriptionPlan;
    }

    @Test
    @Transactional
    void createSubscriptionPlanWithExistingId() throws Exception {
        // Create the SubscriptionPlan with an existing ID
        subscriptionPlan.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSubscriptionPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isBadRequest());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionPlan.setName(null);

        // Create the SubscriptionPlan, which fails.

        restSubscriptionPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDescriptionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionPlan.setDescription(null);

        // Create the SubscriptionPlan, which fails.

        restSubscriptionPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMonthlyPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionPlan.setMonthlyPrice(null);

        // Create the SubscriptionPlan, which fails.

        restSubscriptionPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAnnualPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionPlan.setAnnualPrice(null);

        // Create the SubscriptionPlan, which fails.

        restSubscriptionPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionPlan.setStatus(null);

        // Create the SubscriptionPlan, which fails.

        restSubscriptionPlanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSubscriptionPlans() throws Exception {
        // Initialize the database
        insertedSubscriptionPlan = subscriptionPlanRepository.saveAndFlush(subscriptionPlan);

        // Get all the subscriptionPlanList
        restSubscriptionPlanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subscriptionPlan.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].monthlyPrice").value(hasItem(sameNumber(DEFAULT_MONTHLY_PRICE))))
            .andExpect(jsonPath("$.[*].annualPrice").value(hasItem(sameNumber(DEFAULT_ANNUAL_PRICE))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    @Transactional
    void getSubscriptionPlan() throws Exception {
        // Initialize the database
        insertedSubscriptionPlan = subscriptionPlanRepository.saveAndFlush(subscriptionPlan);

        // Get the subscriptionPlan
        restSubscriptionPlanMockMvc
            .perform(get(ENTITY_API_URL_ID, subscriptionPlan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(subscriptionPlan.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.monthlyPrice").value(sameNumber(DEFAULT_MONTHLY_PRICE)))
            .andExpect(jsonPath("$.annualPrice").value(sameNumber(DEFAULT_ANNUAL_PRICE)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    @Transactional
    void getNonExistingSubscriptionPlan() throws Exception {
        // Get the subscriptionPlan
        restSubscriptionPlanMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSubscriptionPlan() throws Exception {
        // Initialize the database
        insertedSubscriptionPlan = subscriptionPlanRepository.saveAndFlush(subscriptionPlan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscriptionPlan
        SubscriptionPlan updatedSubscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlan.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSubscriptionPlan are not directly saved in db
        em.detach(updatedSubscriptionPlan);
        updatedSubscriptionPlan
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .monthlyPrice(UPDATED_MONTHLY_PRICE)
            .annualPrice(UPDATED_ANNUAL_PRICE)
            .status(UPDATED_STATUS);

        restSubscriptionPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSubscriptionPlan.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSubscriptionPlan))
            )
            .andExpect(status().isOk());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSubscriptionPlanToMatchAllProperties(updatedSubscriptionPlan);
    }

    @Test
    @Transactional
    void putNonExistingSubscriptionPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionPlan.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSubscriptionPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, subscriptionPlan.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(subscriptionPlan))
            )
            .andExpect(status().isBadRequest());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSubscriptionPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionPlan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionPlanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(subscriptionPlan))
            )
            .andExpect(status().isBadRequest());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSubscriptionPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionPlan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionPlanMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSubscriptionPlanWithPatch() throws Exception {
        // Initialize the database
        insertedSubscriptionPlan = subscriptionPlanRepository.saveAndFlush(subscriptionPlan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscriptionPlan using partial update
        SubscriptionPlan partialUpdatedSubscriptionPlan = new SubscriptionPlan();
        partialUpdatedSubscriptionPlan.setId(subscriptionPlan.getId());

        partialUpdatedSubscriptionPlan.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restSubscriptionPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSubscriptionPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSubscriptionPlan))
            )
            .andExpect(status().isOk());

        // Validate the SubscriptionPlan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSubscriptionPlanUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSubscriptionPlan, subscriptionPlan),
            getPersistedSubscriptionPlan(subscriptionPlan)
        );
    }

    @Test
    @Transactional
    void fullUpdateSubscriptionPlanWithPatch() throws Exception {
        // Initialize the database
        insertedSubscriptionPlan = subscriptionPlanRepository.saveAndFlush(subscriptionPlan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscriptionPlan using partial update
        SubscriptionPlan partialUpdatedSubscriptionPlan = new SubscriptionPlan();
        partialUpdatedSubscriptionPlan.setId(subscriptionPlan.getId());

        partialUpdatedSubscriptionPlan
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .monthlyPrice(UPDATED_MONTHLY_PRICE)
            .annualPrice(UPDATED_ANNUAL_PRICE)
            .status(UPDATED_STATUS);

        restSubscriptionPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSubscriptionPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSubscriptionPlan))
            )
            .andExpect(status().isOk());

        // Validate the SubscriptionPlan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSubscriptionPlanUpdatableFieldsEquals(
            partialUpdatedSubscriptionPlan,
            getPersistedSubscriptionPlan(partialUpdatedSubscriptionPlan)
        );
    }

    @Test
    @Transactional
    void patchNonExistingSubscriptionPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionPlan.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSubscriptionPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, subscriptionPlan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(subscriptionPlan))
            )
            .andExpect(status().isBadRequest());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSubscriptionPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionPlan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionPlanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(subscriptionPlan))
            )
            .andExpect(status().isBadRequest());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSubscriptionPlan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionPlan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSubscriptionPlanMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(subscriptionPlan)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SubscriptionPlan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSubscriptionPlan() throws Exception {
        // Initialize the database
        insertedSubscriptionPlan = subscriptionPlanRepository.saveAndFlush(subscriptionPlan);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the subscriptionPlan
        restSubscriptionPlanMockMvc
            .perform(delete(ENTITY_API_URL_ID, subscriptionPlan.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return subscriptionPlanRepository.count();
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

    protected SubscriptionPlan getPersistedSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        return subscriptionPlanRepository.findById(subscriptionPlan.getId()).orElseThrow();
    }

    protected void assertPersistedSubscriptionPlanToMatchAllProperties(SubscriptionPlan expectedSubscriptionPlan) {
        assertSubscriptionPlanAllPropertiesEquals(expectedSubscriptionPlan, getPersistedSubscriptionPlan(expectedSubscriptionPlan));
    }

    protected void assertPersistedSubscriptionPlanToMatchUpdatableProperties(SubscriptionPlan expectedSubscriptionPlan) {
        assertSubscriptionPlanAllUpdatablePropertiesEquals(
            expectedSubscriptionPlan,
            getPersistedSubscriptionPlan(expectedSubscriptionPlan)
        );
    }
}
