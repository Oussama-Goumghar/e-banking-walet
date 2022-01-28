package com.ensa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ensa.IntegrationTest;
import com.ensa.domain.Walet;
import com.ensa.repository.WaletRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link WaletResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WaletResourceIT {

    private static final Long DEFAULT_ID_C_LIENT = 1L;
    private static final Long UPDATED_ID_C_LIENT = 2L;

    private static final String DEFAULT_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_LOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/walets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private WaletRepository waletRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWaletMockMvc;

    private Walet walet;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Walet createEntity(EntityManager em) {
        Walet walet = new Walet().idCLient(DEFAULT_ID_C_LIENT).login(DEFAULT_LOGIN).password(DEFAULT_PASSWORD);
        return walet;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Walet createUpdatedEntity(EntityManager em) {
        Walet walet = new Walet().idCLient(UPDATED_ID_C_LIENT).login(UPDATED_LOGIN).password(UPDATED_PASSWORD);
        return walet;
    }

    @BeforeEach
    public void initTest() {
        walet = createEntity(em);
    }

    @Test
    @Transactional
    void createWalet() throws Exception {
        int databaseSizeBeforeCreate = waletRepository.findAll().size();
        // Create the Walet
        restWaletMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(walet)))
            .andExpect(status().isCreated());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeCreate + 1);
        Walet testWalet = waletList.get(waletList.size() - 1);
        assertThat(testWalet.getIdCLient()).isEqualTo(DEFAULT_ID_C_LIENT);
        assertThat(testWalet.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testWalet.getPassword()).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test
    @Transactional
    void createWaletWithExistingId() throws Exception {
        // Create the Walet with an existing ID
        walet.setId(1L);

        int databaseSizeBeforeCreate = waletRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWaletMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(walet)))
            .andExpect(status().isBadRequest());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllWalets() throws Exception {
        // Initialize the database
        waletRepository.saveAndFlush(walet);

        // Get all the waletList
        restWaletMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(walet.getId().intValue())))
            .andExpect(jsonPath("$.[*].idCLient").value(hasItem(DEFAULT_ID_C_LIENT.intValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)));
    }

    @Test
    @Transactional
    void getWalet() throws Exception {
        // Initialize the database
        waletRepository.saveAndFlush(walet);

        // Get the walet
        restWaletMockMvc
            .perform(get(ENTITY_API_URL_ID, walet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(walet.getId().intValue()))
            .andExpect(jsonPath("$.idCLient").value(DEFAULT_ID_C_LIENT.intValue()))
            .andExpect(jsonPath("$.login").value(DEFAULT_LOGIN))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD));
    }

    @Test
    @Transactional
    void getNonExistingWalet() throws Exception {
        // Get the walet
        restWaletMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewWalet() throws Exception {
        // Initialize the database
        waletRepository.saveAndFlush(walet);

        int databaseSizeBeforeUpdate = waletRepository.findAll().size();

        // Update the walet
        Walet updatedWalet = waletRepository.findById(walet.getId()).get();
        // Disconnect from session so that the updates on updatedWalet are not directly saved in db
        em.detach(updatedWalet);
        updatedWalet.idCLient(UPDATED_ID_C_LIENT).login(UPDATED_LOGIN).password(UPDATED_PASSWORD);

        restWaletMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedWalet.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedWalet))
            )
            .andExpect(status().isOk());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
        Walet testWalet = waletList.get(waletList.size() - 1);
        assertThat(testWalet.getIdCLient()).isEqualTo(UPDATED_ID_C_LIENT);
        assertThat(testWalet.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testWalet.getPassword()).isEqualTo(UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void putNonExistingWalet() throws Exception {
        int databaseSizeBeforeUpdate = waletRepository.findAll().size();
        walet.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWaletMockMvc
            .perform(
                put(ENTITY_API_URL_ID, walet.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(walet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWalet() throws Exception {
        int databaseSizeBeforeUpdate = waletRepository.findAll().size();
        walet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWaletMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(walet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWalet() throws Exception {
        int databaseSizeBeforeUpdate = waletRepository.findAll().size();
        walet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWaletMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(walet)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWaletWithPatch() throws Exception {
        // Initialize the database
        waletRepository.saveAndFlush(walet);

        int databaseSizeBeforeUpdate = waletRepository.findAll().size();

        // Update the walet using partial update
        Walet partialUpdatedWalet = new Walet();
        partialUpdatedWalet.setId(walet.getId());

        partialUpdatedWalet.idCLient(UPDATED_ID_C_LIENT).login(UPDATED_LOGIN).password(UPDATED_PASSWORD);

        restWaletMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWalet.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWalet))
            )
            .andExpect(status().isOk());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
        Walet testWalet = waletList.get(waletList.size() - 1);
        assertThat(testWalet.getIdCLient()).isEqualTo(UPDATED_ID_C_LIENT);
        assertThat(testWalet.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testWalet.getPassword()).isEqualTo(UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void fullUpdateWaletWithPatch() throws Exception {
        // Initialize the database
        waletRepository.saveAndFlush(walet);

        int databaseSizeBeforeUpdate = waletRepository.findAll().size();

        // Update the walet using partial update
        Walet partialUpdatedWalet = new Walet();
        partialUpdatedWalet.setId(walet.getId());

        partialUpdatedWalet.idCLient(UPDATED_ID_C_LIENT).login(UPDATED_LOGIN).password(UPDATED_PASSWORD);

        restWaletMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWalet.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWalet))
            )
            .andExpect(status().isOk());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
        Walet testWalet = waletList.get(waletList.size() - 1);
        assertThat(testWalet.getIdCLient()).isEqualTo(UPDATED_ID_C_LIENT);
        assertThat(testWalet.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testWalet.getPassword()).isEqualTo(UPDATED_PASSWORD);
    }

    @Test
    @Transactional
    void patchNonExistingWalet() throws Exception {
        int databaseSizeBeforeUpdate = waletRepository.findAll().size();
        walet.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWaletMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, walet.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(walet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWalet() throws Exception {
        int databaseSizeBeforeUpdate = waletRepository.findAll().size();
        walet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWaletMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(walet))
            )
            .andExpect(status().isBadRequest());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWalet() throws Exception {
        int databaseSizeBeforeUpdate = waletRepository.findAll().size();
        walet.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWaletMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(walet)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Walet in the database
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWalet() throws Exception {
        // Initialize the database
        waletRepository.saveAndFlush(walet);

        int databaseSizeBeforeDelete = waletRepository.findAll().size();

        // Delete the walet
        restWaletMockMvc
            .perform(delete(ENTITY_API_URL_ID, walet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Walet> waletList = waletRepository.findAll();
        assertThat(waletList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
