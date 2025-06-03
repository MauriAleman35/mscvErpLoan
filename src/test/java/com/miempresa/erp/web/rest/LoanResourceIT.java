package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.LoanAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.miempresa.erp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.repository.LoanRepository;
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
 * Integration tests for the {@link LoanResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LoanResourceIT {

    private static final BigDecimal DEFAULT_LOAN_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_LOAN_AMOUNT = new BigDecimal(2);

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_HASH_BLOCKCHAIN = "AAAAAAAAAA";
    private static final String UPDATED_HASH_BLOCKCHAIN = "BBBBBBBBBB";

    private static final String DEFAULT_CURRENT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_CURRENT_STATUS = "BBBBBBBBBB";

    private static final Integer DEFAULT_LATE_PAYMENT_COUNT = 1;
    private static final Integer UPDATED_LATE_PAYMENT_COUNT = 2;

    private static final Instant DEFAULT_LAST_STATUS_UPDATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_STATUS_UPDATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/loans";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLoanMockMvc;

    private Loan loan;

    private Loan insertedLoan;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Loan createEntity() {
        return new Loan()
            .loanAmount(DEFAULT_LOAN_AMOUNT)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .hashBlockchain(DEFAULT_HASH_BLOCKCHAIN)
            .currentStatus(DEFAULT_CURRENT_STATUS)
            .latePaymentCount(DEFAULT_LATE_PAYMENT_COUNT)
            .lastStatusUpdate(DEFAULT_LAST_STATUS_UPDATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Loan createUpdatedEntity() {
        return new Loan()
            .loanAmount(UPDATED_LOAN_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .hashBlockchain(UPDATED_HASH_BLOCKCHAIN)
            .currentStatus(UPDATED_CURRENT_STATUS)
            .latePaymentCount(UPDATED_LATE_PAYMENT_COUNT)
            .lastStatusUpdate(UPDATED_LAST_STATUS_UPDATE);
    }

    @BeforeEach
    void initTest() {
        loan = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLoan != null) {
            loanRepository.delete(insertedLoan);
            insertedLoan = null;
        }
    }

    @Test
    @Transactional
    void createLoan() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Loan
        var returnedLoan = om.readValue(
            restLoanMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Loan.class
        );

        // Validate the Loan in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertLoanUpdatableFieldsEquals(returnedLoan, getPersistedLoan(returnedLoan));

        insertedLoan = returnedLoan;
    }

    @Test
    @Transactional
    void createLoanWithExistingId() throws Exception {
        // Create the Loan with an existing ID
        loan.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLoanAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setLoanAmount(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setStartDate(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setEndDate(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkHashBlockchainIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setHashBlockchain(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrentStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setCurrentStatus(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLatePaymentCountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setLatePaymentCount(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastStatusUpdateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setLastStatusUpdate(null);

        // Create the Loan, which fails.

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLoans() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList
        restLoanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(loan.getId().intValue())))
            .andExpect(jsonPath("$.[*].loanAmount").value(hasItem(sameNumber(DEFAULT_LOAN_AMOUNT))))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].hashBlockchain").value(hasItem(DEFAULT_HASH_BLOCKCHAIN)))
            .andExpect(jsonPath("$.[*].currentStatus").value(hasItem(DEFAULT_CURRENT_STATUS)))
            .andExpect(jsonPath("$.[*].latePaymentCount").value(hasItem(DEFAULT_LATE_PAYMENT_COUNT)))
            .andExpect(jsonPath("$.[*].lastStatusUpdate").value(hasItem(DEFAULT_LAST_STATUS_UPDATE.toString())));
    }

    @Test
    @Transactional
    void getLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get the loan
        restLoanMockMvc
            .perform(get(ENTITY_API_URL_ID, loan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(loan.getId().intValue()))
            .andExpect(jsonPath("$.loanAmount").value(sameNumber(DEFAULT_LOAN_AMOUNT)))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.hashBlockchain").value(DEFAULT_HASH_BLOCKCHAIN))
            .andExpect(jsonPath("$.currentStatus").value(DEFAULT_CURRENT_STATUS))
            .andExpect(jsonPath("$.latePaymentCount").value(DEFAULT_LATE_PAYMENT_COUNT))
            .andExpect(jsonPath("$.lastStatusUpdate").value(DEFAULT_LAST_STATUS_UPDATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingLoan() throws Exception {
        // Get the loan
        restLoanMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan
        Loan updatedLoan = loanRepository.findById(loan.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLoan are not directly saved in db
        em.detach(updatedLoan);
        updatedLoan
            .loanAmount(UPDATED_LOAN_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .hashBlockchain(UPDATED_HASH_BLOCKCHAIN)
            .currentStatus(UPDATED_CURRENT_STATUS)
            .latePaymentCount(UPDATED_LATE_PAYMENT_COUNT)
            .lastStatusUpdate(UPDATED_LAST_STATUS_UPDATE);

        restLoanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedLoan.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedLoan))
            )
            .andExpect(status().isOk());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLoanToMatchAllProperties(updatedLoan);
    }

    @Test
    @Transactional
    void putNonExistingLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(put(ENTITY_API_URL_ID, loan.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(loan))
            )
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loan)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLoanWithPatch() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan using partial update
        Loan partialUpdatedLoan = new Loan();
        partialUpdatedLoan.setId(loan.getId());

        partialUpdatedLoan
            .loanAmount(UPDATED_LOAN_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .hashBlockchain(UPDATED_HASH_BLOCKCHAIN)
            .currentStatus(UPDATED_CURRENT_STATUS);

        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLoan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLoan))
            )
            .andExpect(status().isOk());

        // Validate the Loan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLoanUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLoan, loan), getPersistedLoan(loan));
    }

    @Test
    @Transactional
    void fullUpdateLoanWithPatch() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan using partial update
        Loan partialUpdatedLoan = new Loan();
        partialUpdatedLoan.setId(loan.getId());

        partialUpdatedLoan
            .loanAmount(UPDATED_LOAN_AMOUNT)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .hashBlockchain(UPDATED_HASH_BLOCKCHAIN)
            .currentStatus(UPDATED_CURRENT_STATUS)
            .latePaymentCount(UPDATED_LATE_PAYMENT_COUNT)
            .lastStatusUpdate(UPDATED_LAST_STATUS_UPDATE);

        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLoan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLoan))
            )
            .andExpect(status().isOk());

        // Validate the Loan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLoanUpdatableFieldsEquals(partialUpdatedLoan, getPersistedLoan(partialUpdatedLoan));
    }

    @Test
    @Transactional
    void patchNonExistingLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(patch(ENTITY_API_URL_ID, loan.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(loan)))
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(loan))
            )
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(loan)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the loan
        restLoanMockMvc
            .perform(delete(ENTITY_API_URL_ID, loan.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return loanRepository.count();
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

    protected Loan getPersistedLoan(Loan loan) {
        return loanRepository.findById(loan.getId()).orElseThrow();
    }

    protected void assertPersistedLoanToMatchAllProperties(Loan expectedLoan) {
        assertLoanAllPropertiesEquals(expectedLoan, getPersistedLoan(expectedLoan));
    }

    protected void assertPersistedLoanToMatchUpdatableProperties(Loan expectedLoan) {
        assertLoanAllUpdatablePropertiesEquals(expectedLoan, getPersistedLoan(expectedLoan));
    }
}
