package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.OfferAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.miempresa.erp.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.repository.OfferRepository;
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
 * Integration tests for the {@link OfferResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OfferResourceIT {

    private static final Integer DEFAULT_PARTNER_ID = 1;
    private static final Integer UPDATED_PARTNER_ID = 2;

    private static final BigDecimal DEFAULT_INTEREST = new BigDecimal(1);
    private static final BigDecimal UPDATED_INTEREST = new BigDecimal(2);

    private static final Integer DEFAULT_LOAN_TERM = 1;
    private static final Integer UPDATED_LOAN_TERM = 2;

    private static final BigDecimal DEFAULT_MONTHLY_PAYMENT = new BigDecimal(1);
    private static final BigDecimal UPDATED_MONTHLY_PAYMENT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TOTAL_REPAYMENT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_REPAYMENT_AMOUNT = new BigDecimal(2);

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/offers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOfferMockMvc;

    private Offer offer;

    private Offer insertedOffer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Offer createEntity() {
        return new Offer()
            .partnerId(DEFAULT_PARTNER_ID)
            .interest(DEFAULT_INTEREST)
            .loanTerm(DEFAULT_LOAN_TERM)
            .monthlyPayment(DEFAULT_MONTHLY_PAYMENT)
            .totalRepaymentAmount(DEFAULT_TOTAL_REPAYMENT_AMOUNT)
            .status(DEFAULT_STATUS)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Offer createUpdatedEntity() {
        return new Offer()
            .partnerId(UPDATED_PARTNER_ID)
            .interest(UPDATED_INTEREST)
            .loanTerm(UPDATED_LOAN_TERM)
            .monthlyPayment(UPDATED_MONTHLY_PAYMENT)
            .totalRepaymentAmount(UPDATED_TOTAL_REPAYMENT_AMOUNT)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        offer = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedOffer != null) {
            offerRepository.delete(insertedOffer);
            insertedOffer = null;
        }
    }

    @Test
    @Transactional
    void createOffer() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Offer
        var returnedOffer = om.readValue(
            restOfferMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Offer.class
        );

        // Validate the Offer in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertOfferUpdatableFieldsEquals(returnedOffer, getPersistedOffer(returnedOffer));

        insertedOffer = returnedOffer;
    }

    @Test
    @Transactional
    void createOfferWithExistingId() throws Exception {
        // Create the Offer with an existing ID
        offer.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPartnerIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setPartnerId(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInterestIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setInterest(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLoanTermIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setLoanTerm(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMonthlyPaymentIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setMonthlyPayment(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalRepaymentAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setTotalRepaymentAmount(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setStatus(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        offer.setCreatedAt(null);

        // Create the Offer, which fails.

        restOfferMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOffers() throws Exception {
        // Initialize the database
        insertedOffer = offerRepository.saveAndFlush(offer);

        // Get all the offerList
        restOfferMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(offer.getId().intValue())))
            .andExpect(jsonPath("$.[*].partnerId").value(hasItem(DEFAULT_PARTNER_ID)))
            .andExpect(jsonPath("$.[*].interest").value(hasItem(sameNumber(DEFAULT_INTEREST))))
            .andExpect(jsonPath("$.[*].loanTerm").value(hasItem(DEFAULT_LOAN_TERM)))
            .andExpect(jsonPath("$.[*].monthlyPayment").value(hasItem(sameNumber(DEFAULT_MONTHLY_PAYMENT))))
            .andExpect(jsonPath("$.[*].totalRepaymentAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_REPAYMENT_AMOUNT))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getOffer() throws Exception {
        // Initialize the database
        insertedOffer = offerRepository.saveAndFlush(offer);

        // Get the offer
        restOfferMockMvc
            .perform(get(ENTITY_API_URL_ID, offer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(offer.getId().intValue()))
            .andExpect(jsonPath("$.partnerId").value(DEFAULT_PARTNER_ID))
            .andExpect(jsonPath("$.interest").value(sameNumber(DEFAULT_INTEREST)))
            .andExpect(jsonPath("$.loanTerm").value(DEFAULT_LOAN_TERM))
            .andExpect(jsonPath("$.monthlyPayment").value(sameNumber(DEFAULT_MONTHLY_PAYMENT)))
            .andExpect(jsonPath("$.totalRepaymentAmount").value(sameNumber(DEFAULT_TOTAL_REPAYMENT_AMOUNT)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingOffer() throws Exception {
        // Get the offer
        restOfferMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOffer() throws Exception {
        // Initialize the database
        insertedOffer = offerRepository.saveAndFlush(offer);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the offer
        Offer updatedOffer = offerRepository.findById(offer.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOffer are not directly saved in db
        em.detach(updatedOffer);
        updatedOffer
            .partnerId(UPDATED_PARTNER_ID)
            .interest(UPDATED_INTEREST)
            .loanTerm(UPDATED_LOAN_TERM)
            .monthlyPayment(UPDATED_MONTHLY_PAYMENT)
            .totalRepaymentAmount(UPDATED_TOTAL_REPAYMENT_AMOUNT)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);

        restOfferMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedOffer.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedOffer))
            )
            .andExpect(status().isOk());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOfferToMatchAllProperties(updatedOffer);
    }

    @Test
    @Transactional
    void putNonExistingOffer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offer.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOfferMockMvc
            .perform(put(ENTITY_API_URL_ID, offer.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOffer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offer.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(offer))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOffer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offer.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(offer)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOfferWithPatch() throws Exception {
        // Initialize the database
        insertedOffer = offerRepository.saveAndFlush(offer);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the offer using partial update
        Offer partialUpdatedOffer = new Offer();
        partialUpdatedOffer.setId(offer.getId());

        partialUpdatedOffer.monthlyPayment(UPDATED_MONTHLY_PAYMENT).totalRepaymentAmount(UPDATED_TOTAL_REPAYMENT_AMOUNT);

        restOfferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOffer.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOffer))
            )
            .andExpect(status().isOk());

        // Validate the Offer in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOfferUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedOffer, offer), getPersistedOffer(offer));
    }

    @Test
    @Transactional
    void fullUpdateOfferWithPatch() throws Exception {
        // Initialize the database
        insertedOffer = offerRepository.saveAndFlush(offer);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the offer using partial update
        Offer partialUpdatedOffer = new Offer();
        partialUpdatedOffer.setId(offer.getId());

        partialUpdatedOffer
            .partnerId(UPDATED_PARTNER_ID)
            .interest(UPDATED_INTEREST)
            .loanTerm(UPDATED_LOAN_TERM)
            .monthlyPayment(UPDATED_MONTHLY_PAYMENT)
            .totalRepaymentAmount(UPDATED_TOTAL_REPAYMENT_AMOUNT)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);

        restOfferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOffer.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOffer))
            )
            .andExpect(status().isOk());

        // Validate the Offer in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOfferUpdatableFieldsEquals(partialUpdatedOffer, getPersistedOffer(partialUpdatedOffer));
    }

    @Test
    @Transactional
    void patchNonExistingOffer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offer.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOfferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, offer.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(offer))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOffer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offer.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(offer))
            )
            .andExpect(status().isBadRequest());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOffer() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        offer.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOfferMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(offer)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Offer in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOffer() throws Exception {
        // Initialize the database
        insertedOffer = offerRepository.saveAndFlush(offer);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the offer
        restOfferMockMvc
            .perform(delete(ENTITY_API_URL_ID, offer.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return offerRepository.count();
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

    protected Offer getPersistedOffer(Offer offer) {
        return offerRepository.findById(offer.getId()).orElseThrow();
    }

    protected void assertPersistedOfferToMatchAllProperties(Offer expectedOffer) {
        assertOfferAllPropertiesEquals(expectedOffer, getPersistedOffer(expectedOffer));
    }

    protected void assertPersistedOfferToMatchUpdatableProperties(Offer expectedOffer) {
        assertOfferAllUpdatablePropertiesEquals(expectedOffer, getPersistedOffer(expectedOffer));
    }
}
