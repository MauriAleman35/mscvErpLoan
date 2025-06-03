package com.miempresa.erp.web.rest;

import static com.miempresa.erp.domain.DocumentAsserts.*;
import static com.miempresa.erp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.IntegrationTest;
import com.miempresa.erp.domain.Document;
import com.miempresa.erp.repository.DocumentRepository;
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
 * Integration tests for the {@link DocumentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DocumentResourceIT {

    private static final String DEFAULT_URL_FILE = "AAAAAAAAAA";
    private static final String UPDATED_URL_FILE = "BBBBBBBBBB";

    private static final Instant DEFAULT_UPLOAD_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPLOAD_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_VERIFIED = false;
    private static final Boolean UPDATED_VERIFIED = true;

    private static final String ENTITY_API_URL = "/api/documents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDocumentMockMvc;

    private Document document;

    private Document insertedDocument;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Document createEntity() {
        return new Document().urlFile(DEFAULT_URL_FILE).uploadDate(DEFAULT_UPLOAD_DATE).verified(DEFAULT_VERIFIED);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Document createUpdatedEntity() {
        return new Document().urlFile(UPDATED_URL_FILE).uploadDate(UPDATED_UPLOAD_DATE).verified(UPDATED_VERIFIED);
    }

    @BeforeEach
    void initTest() {
        document = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDocument != null) {
            documentRepository.delete(insertedDocument);
            insertedDocument = null;
        }
    }

    @Test
    @Transactional
    void createDocument() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Document
        var returnedDocument = om.readValue(
            restDocumentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Document.class
        );

        // Validate the Document in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertDocumentUpdatableFieldsEquals(returnedDocument, getPersistedDocument(returnedDocument));

        insertedDocument = returnedDocument;
    }

    @Test
    @Transactional
    void createDocumentWithExistingId() throws Exception {
        // Create the Document with an existing ID
        document.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document)))
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUrlFileIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        document.setUrlFile(null);

        // Create the Document, which fails.

        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUploadDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        document.setUploadDate(null);

        // Create the Document, which fails.

        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVerifiedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        document.setVerified(null);

        // Create the Document, which fails.

        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDocuments() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(document.getId().intValue())))
            .andExpect(jsonPath("$.[*].urlFile").value(hasItem(DEFAULT_URL_FILE)))
            .andExpect(jsonPath("$.[*].uploadDate").value(hasItem(DEFAULT_UPLOAD_DATE.toString())))
            .andExpect(jsonPath("$.[*].verified").value(hasItem(DEFAULT_VERIFIED)));
    }

    @Test
    @Transactional
    void getDocument() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get the document
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL_ID, document.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(document.getId().intValue()))
            .andExpect(jsonPath("$.urlFile").value(DEFAULT_URL_FILE))
            .andExpect(jsonPath("$.uploadDate").value(DEFAULT_UPLOAD_DATE.toString()))
            .andExpect(jsonPath("$.verified").value(DEFAULT_VERIFIED));
    }

    @Test
    @Transactional
    void getNonExistingDocument() throws Exception {
        // Get the document
        restDocumentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDocument() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the document
        Document updatedDocument = documentRepository.findById(document.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDocument are not directly saved in db
        em.detach(updatedDocument);
        updatedDocument.urlFile(UPDATED_URL_FILE).uploadDate(UPDATED_UPLOAD_DATE).verified(UPDATED_VERIFIED);

        restDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedDocument.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedDocument))
            )
            .andExpect(status().isOk());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDocumentToMatchAllProperties(updatedDocument);
    }

    @Test
    @Transactional
    void putNonExistingDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, document.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(document))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(document)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDocumentWithPatch() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the document using partial update
        Document partialUpdatedDocument = new Document();
        partialUpdatedDocument.setId(document.getId());

        partialUpdatedDocument.urlFile(UPDATED_URL_FILE);

        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocument))
            )
            .andExpect(status().isOk());

        // Validate the Document in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDocument, document), getPersistedDocument(document));
    }

    @Test
    @Transactional
    void fullUpdateDocumentWithPatch() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the document using partial update
        Document partialUpdatedDocument = new Document();
        partialUpdatedDocument.setId(document.getId());

        partialUpdatedDocument.urlFile(UPDATED_URL_FILE).uploadDate(UPDATED_UPLOAD_DATE).verified(UPDATED_VERIFIED);

        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocument))
            )
            .andExpect(status().isOk());

        // Validate the Document in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentUpdatableFieldsEquals(partialUpdatedDocument, getPersistedDocument(partialUpdatedDocument));
    }

    @Test
    @Transactional
    void patchNonExistingDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, document.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(document))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(document))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(document)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDocument() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the document
        restDocumentMockMvc
            .perform(delete(ENTITY_API_URL_ID, document.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return documentRepository.count();
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

    protected Document getPersistedDocument(Document document) {
        return documentRepository.findById(document.getId()).orElseThrow();
    }

    protected void assertPersistedDocumentToMatchAllProperties(Document expectedDocument) {
        assertDocumentAllPropertiesEquals(expectedDocument, getPersistedDocument(expectedDocument));
    }

    protected void assertPersistedDocumentToMatchUpdatableProperties(Document expectedDocument) {
        assertDocumentAllUpdatablePropertiesEquals(expectedDocument, getPersistedDocument(expectedDocument));
    }
}
