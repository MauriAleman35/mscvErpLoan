package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.RoleAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.Role;
import com.miempresa.erp.repository.RoleRepository;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link RoleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RoleResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/roles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRoleMockMvc;

    private Role role;

    private Role insertedRole;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Role createEntity() {
        return new Role().name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Role createUpdatedEntity() {
        return new Role().name(UPDATED_NAME);
    }

    @BeforeEach
    void initTest() {
        role = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedRole != null) {
            roleRepository.delete(insertedRole);
            insertedRole = null;
        }
    }

    @Test
    @Transactional
    void createRole() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Role
        var returnedRole = om.readValue(
            restRoleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(role)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Role.class
        );

        // Validate the Role in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertRoleUpdatableFieldsEquals(returnedRole, getPersistedRole(returnedRole));

        insertedRole = returnedRole;
    }

    @Test
    @Transactional
    void createRoleWithExistingId() throws Exception {
        // Create the Role with an existing ID
        role.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRoleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(role)))
            .andExpect(status().isBadRequest());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        role.setName(null);

        // Create the Role, which fails.

        restRoleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(role)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRoles() throws Exception {
        // Initialize the database
        insertedRole = roleRepository.saveAndFlush(role);

        // Get all the roleList
        restRoleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(role.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getRole() throws Exception {
        // Initialize the database
        insertedRole = roleRepository.saveAndFlush(role);

        // Get the role
        restRoleMockMvc
            .perform(get(ENTITY_API_URL_ID, role.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(role.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingRole() throws Exception {
        // Get the role
        restRoleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRole() throws Exception {
        // Initialize the database
        insertedRole = roleRepository.saveAndFlush(role);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the role
        Role updatedRole = roleRepository.findById(role.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRole are not directly saved in db
        em.detach(updatedRole);
        updatedRole.name(UPDATED_NAME);

        restRoleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedRole.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedRole))
            )
            .andExpect(status().isOk());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRoleToMatchAllProperties(updatedRole);
    }

    @Test
    @Transactional
    void putNonExistingRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        role.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoleMockMvc
            .perform(put(ENTITY_API_URL_ID, role.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(role)))
            .andExpect(status().isBadRequest());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        role.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(role))
            )
            .andExpect(status().isBadRequest());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        role.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(role)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRoleWithPatch() throws Exception {
        // Initialize the database
        insertedRole = roleRepository.saveAndFlush(role);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the role using partial update
        Role partialUpdatedRole = new Role();
        partialUpdatedRole.setId(role.getId());

        partialUpdatedRole.name(UPDATED_NAME);

        restRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRole.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRole))
            )
            .andExpect(status().isOk());

        // Validate the Role in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRoleUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedRole, role), getPersistedRole(role));
    }

    @Test
    @Transactional
    void fullUpdateRoleWithPatch() throws Exception {
        // Initialize the database
        insertedRole = roleRepository.saveAndFlush(role);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the role using partial update
        Role partialUpdatedRole = new Role();
        partialUpdatedRole.setId(role.getId());

        partialUpdatedRole.name(UPDATED_NAME);

        restRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRole.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRole))
            )
            .andExpect(status().isOk());

        // Validate the Role in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRoleUpdatableFieldsEquals(partialUpdatedRole, getPersistedRole(partialUpdatedRole));
    }

    @Test
    @Transactional
    void patchNonExistingRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        role.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoleMockMvc
            .perform(patch(ENTITY_API_URL_ID, role.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(role)))
            .andExpect(status().isBadRequest());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        role.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(role))
            )
            .andExpect(status().isBadRequest());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        role.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(role)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Role in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRole() throws Exception {
        // Initialize the database
        insertedRole = roleRepository.saveAndFlush(role);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the role
        restRoleMockMvc
            .perform(delete(ENTITY_API_URL_ID, role.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return roleRepository.count();
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

    protected Role getPersistedRole(Role role) {
        return roleRepository.findById(role.getId()).orElseThrow();
    }

    protected void assertPersistedRoleToMatchAllProperties(Role expectedRole) {
        assertRoleAllPropertiesEquals(expectedRole, getPersistedRole(expectedRole));
    }

    protected void assertPersistedRoleToMatchUpdatableProperties(Role expectedRole) {
        assertRoleAllUpdatablePropertiesEquals(expectedRole, getPersistedRole(expectedRole));
    }
}
