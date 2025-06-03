package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.UserSubscriptionAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.UserSubscription;
import com.miempresa.erp.repository.UserSubscriptionRepository;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link UserSubscriptionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UserSubscriptionResourceIT {

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/user-subscriptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserSubscriptionMockMvc;

    private UserSubscription userSubscription;

    private UserSubscription insertedUserSubscription;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserSubscription createEntity() {
        return new UserSubscription().startDate(DEFAULT_START_DATE).endDate(DEFAULT_END_DATE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserSubscription createUpdatedEntity() {
        return new UserSubscription().startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        userSubscription = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserSubscription != null) {
            userSubscriptionRepository.delete(insertedUserSubscription);
            insertedUserSubscription = null;
        }
    }

    @Test
    @Transactional
    void createUserSubscription() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the UserSubscription
        var returnedUserSubscription = om.readValue(
            restUserSubscriptionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userSubscription)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UserSubscription.class
        );

        // Validate the UserSubscription in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertUserSubscriptionUpdatableFieldsEquals(returnedUserSubscription, getPersistedUserSubscription(returnedUserSubscription));

        insertedUserSubscription = returnedUserSubscription;
    }

    @Test
    @Transactional
    void createUserSubscriptionWithExistingId() throws Exception {
        // Create the UserSubscription with an existing ID
        userSubscription.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userSubscription)))
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userSubscription.setStartDate(null);

        // Create the UserSubscription, which fails.

        restUserSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userSubscription)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userSubscription.setEndDate(null);

        // Create the UserSubscription, which fails.

        restUserSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userSubscription)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userSubscription.setStatus(null);

        // Create the UserSubscription, which fails.

        restUserSubscriptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userSubscription)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllUserSubscriptions() throws Exception {
        // Initialize the database
        insertedUserSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get all the userSubscriptionList
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userSubscription.getId().intValue())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    @Transactional
    void getUserSubscription() throws Exception {
        // Initialize the database
        insertedUserSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        // Get the userSubscription
        restUserSubscriptionMockMvc
            .perform(get(ENTITY_API_URL_ID, userSubscription.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(userSubscription.getId().intValue()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    @Transactional
    void getNonExistingUserSubscription() throws Exception {
        // Get the userSubscription
        restUserSubscriptionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingUserSubscription() throws Exception {
        // Initialize the database
        insertedUserSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userSubscription
        UserSubscription updatedUserSubscription = userSubscriptionRepository.findById(userSubscription.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedUserSubscription are not directly saved in db
        em.detach(updatedUserSubscription);
        updatedUserSubscription.startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);

        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedUserSubscription.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedUserSubscription))
            )
            .andExpect(status().isOk());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUserSubscriptionToMatchAllProperties(updatedUserSubscription);
    }

    @Test
    @Transactional
    void putNonExistingUserSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userSubscription.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, userSubscription.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchUserSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userSubscription.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUserSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userSubscription.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userSubscription)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateUserSubscriptionWithPatch() throws Exception {
        // Initialize the database
        insertedUserSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userSubscription using partial update
        UserSubscription partialUpdatedUserSubscription = new UserSubscription();
        partialUpdatedUserSubscription.setId(userSubscription.getId());

        partialUpdatedUserSubscription.endDate(UPDATED_END_DATE);

        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUserSubscription))
            )
            .andExpect(status().isOk());

        // Validate the UserSubscription in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserSubscriptionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedUserSubscription, userSubscription),
            getPersistedUserSubscription(userSubscription)
        );
    }

    @Test
    @Transactional
    void fullUpdateUserSubscriptionWithPatch() throws Exception {
        // Initialize the database
        insertedUserSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userSubscription using partial update
        UserSubscription partialUpdatedUserSubscription = new UserSubscription();
        partialUpdatedUserSubscription.setId(userSubscription.getId());

        partialUpdatedUserSubscription.startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);

        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUserSubscription))
            )
            .andExpect(status().isOk());

        // Validate the UserSubscription in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserSubscriptionUpdatableFieldsEquals(
            partialUpdatedUserSubscription,
            getPersistedUserSubscription(partialUpdatedUserSubscription)
        );
    }

    @Test
    @Transactional
    void patchNonExistingUserSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userSubscription.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, userSubscription.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUserSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userSubscription.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(userSubscription))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUserSubscription() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userSubscription.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserSubscriptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(userSubscription)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserSubscription in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteUserSubscription() throws Exception {
        // Initialize the database
        insertedUserSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the userSubscription
        restUserSubscriptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, userSubscription.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return userSubscriptionRepository.count();
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

    protected UserSubscription getPersistedUserSubscription(UserSubscription userSubscription) {
        return userSubscriptionRepository.findById(userSubscription.getId()).orElseThrow();
    }

    protected void assertPersistedUserSubscriptionToMatchAllProperties(UserSubscription expectedUserSubscription) {
        assertUserSubscriptionAllPropertiesEquals(expectedUserSubscription, getPersistedUserSubscription(expectedUserSubscription));
    }

    protected void assertPersistedUserSubscriptionToMatchUpdatableProperties(UserSubscription expectedUserSubscription) {
        assertUserSubscriptionAllUpdatablePropertiesEquals(
            expectedUserSubscription,
            getPersistedUserSubscription(expectedUserSubscription)
        );
    }
}
