package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.MonthlyPaymentAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.miempresa.erp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.MonthlyPayment;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
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
 * Integration tests for the {@link MonthlyPaymentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MonthlyPaymentResourceIT {

    private static final Instant DEFAULT_DUE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DUE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_PAYMENT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PAYMENT_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_BORROW_VERIFIED = false;
    private static final Boolean UPDATED_BORROW_VERIFIED = true;

    private static final Boolean DEFAULT_PARTNER_VERIFIED = false;
    private static final Boolean UPDATED_PARTNER_VERIFIED = true;

    private static final String DEFAULT_COMPROBANT_FILE = "AAAAAAAAAA";
    private static final String UPDATED_COMPROBANT_FILE = "BBBBBBBBBB";

    private static final Integer DEFAULT_DAYS_LATE = 1;
    private static final Integer UPDATED_DAYS_LATE = 2;

    private static final BigDecimal DEFAULT_PENALTY_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_PENALTY_AMOUNT = new BigDecimal(2);

    private static final String DEFAULT_PAYMENT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_PAYMENT_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/monthly-payments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MonthlyPaymentRepository monthlyPaymentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMonthlyPaymentMockMvc;

    private MonthlyPayment monthlyPayment;

    private MonthlyPayment insertedMonthlyPayment;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MonthlyPayment createEntity() {
        return new MonthlyPayment()
            .dueDate(DEFAULT_DUE_DATE)
            .paymentDate(DEFAULT_PAYMENT_DATE)
            .borrowVerified(DEFAULT_BORROW_VERIFIED)
            .partnerVerified(DEFAULT_PARTNER_VERIFIED)
            .comprobantFile(DEFAULT_COMPROBANT_FILE)
            .daysLate(DEFAULT_DAYS_LATE)
            .penaltyAmount(DEFAULT_PENALTY_AMOUNT)
            .paymentStatus(DEFAULT_PAYMENT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MonthlyPayment createUpdatedEntity() {
        return new MonthlyPayment()
            .dueDate(UPDATED_DUE_DATE)
            .paymentDate(UPDATED_PAYMENT_DATE)
            .borrowVerified(UPDATED_BORROW_VERIFIED)
            .partnerVerified(UPDATED_PARTNER_VERIFIED)
            .comprobantFile(UPDATED_COMPROBANT_FILE)
            .daysLate(UPDATED_DAYS_LATE)
            .penaltyAmount(UPDATED_PENALTY_AMOUNT)
            .paymentStatus(UPDATED_PAYMENT_STATUS);
    }

    @BeforeEach
    void initTest() {
        monthlyPayment = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMonthlyPayment != null) {
            monthlyPaymentRepository.delete(insertedMonthlyPayment);
            insertedMonthlyPayment = null;
        }
    }

    @Test
    @Transactional
    void createMonthlyPayment() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MonthlyPayment
        var returnedMonthlyPayment = om.readValue(
            restMonthlyPaymentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MonthlyPayment.class
        );

        // Validate the MonthlyPayment in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertMonthlyPaymentUpdatableFieldsEquals(returnedMonthlyPayment, getPersistedMonthlyPayment(returnedMonthlyPayment));

        insertedMonthlyPayment = returnedMonthlyPayment;
    }

    @Test
    @Transactional
    void createMonthlyPaymentWithExistingId() throws Exception {
        // Create the MonthlyPayment with an existing ID
        monthlyPayment.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDueDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setDueDate(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPaymentDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setPaymentDate(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBorrowVerifiedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setBorrowVerified(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPartnerVerifiedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setPartnerVerified(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkComprobantFileIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setComprobantFile(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDaysLateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setDaysLate(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPenaltyAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setPenaltyAmount(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPaymentStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        monthlyPayment.setPaymentStatus(null);

        // Create the MonthlyPayment, which fails.

        restMonthlyPaymentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMonthlyPayments() throws Exception {
        // Initialize the database
        insertedMonthlyPayment = monthlyPaymentRepository.saveAndFlush(monthlyPayment);

        // Get all the monthlyPaymentList
        restMonthlyPaymentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(monthlyPayment.getId().intValue())))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].paymentDate").value(hasItem(DEFAULT_PAYMENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].borrowVerified").value(hasItem(DEFAULT_BORROW_VERIFIED)))
            .andExpect(jsonPath("$.[*].partnerVerified").value(hasItem(DEFAULT_PARTNER_VERIFIED)))
            .andExpect(jsonPath("$.[*].comprobantFile").value(hasItem(DEFAULT_COMPROBANT_FILE)))
            .andExpect(jsonPath("$.[*].daysLate").value(hasItem(DEFAULT_DAYS_LATE)))
            .andExpect(jsonPath("$.[*].penaltyAmount").value(hasItem(sameNumber(DEFAULT_PENALTY_AMOUNT))))
            .andExpect(jsonPath("$.[*].paymentStatus").value(hasItem(DEFAULT_PAYMENT_STATUS)));
    }

    @Test
    @Transactional
    void getMonthlyPayment() throws Exception {
        // Initialize the database
        insertedMonthlyPayment = monthlyPaymentRepository.saveAndFlush(monthlyPayment);

        // Get the monthlyPayment
        restMonthlyPaymentMockMvc
            .perform(get(ENTITY_API_URL_ID, monthlyPayment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(monthlyPayment.getId().intValue()))
            .andExpect(jsonPath("$.dueDate").value(DEFAULT_DUE_DATE.toString()))
            .andExpect(jsonPath("$.paymentDate").value(DEFAULT_PAYMENT_DATE.toString()))
            .andExpect(jsonPath("$.borrowVerified").value(DEFAULT_BORROW_VERIFIED))
            .andExpect(jsonPath("$.partnerVerified").value(DEFAULT_PARTNER_VERIFIED))
            .andExpect(jsonPath("$.comprobantFile").value(DEFAULT_COMPROBANT_FILE))
            .andExpect(jsonPath("$.daysLate").value(DEFAULT_DAYS_LATE))
            .andExpect(jsonPath("$.penaltyAmount").value(sameNumber(DEFAULT_PENALTY_AMOUNT)))
            .andExpect(jsonPath("$.paymentStatus").value(DEFAULT_PAYMENT_STATUS));
    }

    @Test
    @Transactional
    void getNonExistingMonthlyPayment() throws Exception {
        // Get the monthlyPayment
        restMonthlyPaymentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMonthlyPayment() throws Exception {
        // Initialize the database
        insertedMonthlyPayment = monthlyPaymentRepository.saveAndFlush(monthlyPayment);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the monthlyPayment
        MonthlyPayment updatedMonthlyPayment = monthlyPaymentRepository.findById(monthlyPayment.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMonthlyPayment are not directly saved in db
        em.detach(updatedMonthlyPayment);
        updatedMonthlyPayment
            .dueDate(UPDATED_DUE_DATE)
            .paymentDate(UPDATED_PAYMENT_DATE)
            .borrowVerified(UPDATED_BORROW_VERIFIED)
            .partnerVerified(UPDATED_PARTNER_VERIFIED)
            .comprobantFile(UPDATED_COMPROBANT_FILE)
            .daysLate(UPDATED_DAYS_LATE)
            .penaltyAmount(UPDATED_PENALTY_AMOUNT)
            .paymentStatus(UPDATED_PAYMENT_STATUS);

        restMonthlyPaymentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedMonthlyPayment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedMonthlyPayment))
            )
            .andExpect(status().isOk());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMonthlyPaymentToMatchAllProperties(updatedMonthlyPayment);
    }

    @Test
    @Transactional
    void putNonExistingMonthlyPayment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        monthlyPayment.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMonthlyPaymentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, monthlyPayment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(monthlyPayment))
            )
            .andExpect(status().isBadRequest());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMonthlyPayment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        monthlyPayment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonthlyPaymentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(monthlyPayment))
            )
            .andExpect(status().isBadRequest());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMonthlyPayment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        monthlyPayment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonthlyPaymentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMonthlyPaymentWithPatch() throws Exception {
        // Initialize the database
        insertedMonthlyPayment = monthlyPaymentRepository.saveAndFlush(monthlyPayment);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the monthlyPayment using partial update
        MonthlyPayment partialUpdatedMonthlyPayment = new MonthlyPayment();
        partialUpdatedMonthlyPayment.setId(monthlyPayment.getId());

        partialUpdatedMonthlyPayment
            .paymentDate(UPDATED_PAYMENT_DATE)
            .borrowVerified(UPDATED_BORROW_VERIFIED)
            .daysLate(UPDATED_DAYS_LATE)
            .penaltyAmount(UPDATED_PENALTY_AMOUNT);

        restMonthlyPaymentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMonthlyPayment.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMonthlyPayment))
            )
            .andExpect(status().isOk());

        // Validate the MonthlyPayment in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMonthlyPaymentUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMonthlyPayment, monthlyPayment),
            getPersistedMonthlyPayment(monthlyPayment)
        );
    }

    @Test
    @Transactional
    void fullUpdateMonthlyPaymentWithPatch() throws Exception {
        // Initialize the database
        insertedMonthlyPayment = monthlyPaymentRepository.saveAndFlush(monthlyPayment);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the monthlyPayment using partial update
        MonthlyPayment partialUpdatedMonthlyPayment = new MonthlyPayment();
        partialUpdatedMonthlyPayment.setId(monthlyPayment.getId());

        partialUpdatedMonthlyPayment
            .dueDate(UPDATED_DUE_DATE)
            .paymentDate(UPDATED_PAYMENT_DATE)
            .borrowVerified(UPDATED_BORROW_VERIFIED)
            .partnerVerified(UPDATED_PARTNER_VERIFIED)
            .comprobantFile(UPDATED_COMPROBANT_FILE)
            .daysLate(UPDATED_DAYS_LATE)
            .penaltyAmount(UPDATED_PENALTY_AMOUNT)
            .paymentStatus(UPDATED_PAYMENT_STATUS);

        restMonthlyPaymentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMonthlyPayment.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMonthlyPayment))
            )
            .andExpect(status().isOk());

        // Validate the MonthlyPayment in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMonthlyPaymentUpdatableFieldsEquals(partialUpdatedMonthlyPayment, getPersistedMonthlyPayment(partialUpdatedMonthlyPayment));
    }

    @Test
    @Transactional
    void patchNonExistingMonthlyPayment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        monthlyPayment.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMonthlyPaymentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, monthlyPayment.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(monthlyPayment))
            )
            .andExpect(status().isBadRequest());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMonthlyPayment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        monthlyPayment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonthlyPaymentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(monthlyPayment))
            )
            .andExpect(status().isBadRequest());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMonthlyPayment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        monthlyPayment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonthlyPaymentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(monthlyPayment)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MonthlyPayment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMonthlyPayment() throws Exception {
        // Initialize the database
        insertedMonthlyPayment = monthlyPaymentRepository.saveAndFlush(monthlyPayment);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the monthlyPayment
        restMonthlyPaymentMockMvc
            .perform(delete(ENTITY_API_URL_ID, monthlyPayment.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return monthlyPaymentRepository.count();
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

    protected MonthlyPayment getPersistedMonthlyPayment(MonthlyPayment monthlyPayment) {
        return monthlyPaymentRepository.findById(monthlyPayment.getId()).orElseThrow();
    }

    protected void assertPersistedMonthlyPaymentToMatchAllProperties(MonthlyPayment expectedMonthlyPayment) {
        assertMonthlyPaymentAllPropertiesEquals(expectedMonthlyPayment, getPersistedMonthlyPayment(expectedMonthlyPayment));
    }

    protected void assertPersistedMonthlyPaymentToMatchUpdatableProperties(MonthlyPayment expectedMonthlyPayment) {
        assertMonthlyPaymentAllUpdatablePropertiesEquals(expectedMonthlyPayment, getPersistedMonthlyPayment(expectedMonthlyPayment));
    }
}
