package com.ensa.web.rest;

import com.ensa.domain.Walet;
import com.ensa.repository.WaletRepository;
import com.ensa.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.ensa.domain.Walet}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class WaletResource {

    private final Logger log = LoggerFactory.getLogger(WaletResource.class);

    private static final String ENTITY_NAME = "waletApiWalet";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WaletRepository waletRepository;

    public WaletResource(WaletRepository waletRepository) {
        this.waletRepository = waletRepository;
    }

    /**
     * {@code POST  /walets} : Create a new walet.
     *
     * @param walet the walet to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new walet, or with status {@code 400 (Bad Request)} if the walet has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/walets")
    public ResponseEntity<Walet> createWalet(@Valid @RequestBody Walet walet) throws URISyntaxException {
        log.debug("REST request to save Walet : {}", walet);
        if (walet.getId() != null) {
            throw new BadRequestAlertException("A new walet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Walet result = waletRepository.save(walet);
        return ResponseEntity
            .created(new URI("/api/walets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /walets/:id} : Updates an existing walet.
     *
     * @param id the id of the walet to save.
     * @param walet the walet to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated walet,
     * or with status {@code 400 (Bad Request)} if the walet is not valid,
     * or with status {@code 500 (Internal Server Error)} if the walet couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/walets/{id}")
    public ResponseEntity<Walet> updateWalet(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Walet walet)
        throws URISyntaxException {
        log.debug("REST request to update Walet : {}, {}", id, walet);
        if (walet.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, walet.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!waletRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Walet result = waletRepository.save(walet);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, walet.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /walets/:id} : Partial updates given fields of an existing walet, field will ignore if it is null
     *
     * @param id the id of the walet to save.
     * @param walet the walet to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated walet,
     * or with status {@code 400 (Bad Request)} if the walet is not valid,
     * or with status {@code 404 (Not Found)} if the walet is not found,
     * or with status {@code 500 (Internal Server Error)} if the walet couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/walets/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Walet> partialUpdateWalet(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Walet walet
    ) throws URISyntaxException {
        log.debug("REST request to partial update Walet partially : {}, {}", id, walet);
        if (walet.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, walet.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!waletRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Walet> result = waletRepository
            .findById(walet.getId())
            .map(existingWalet -> {
                if (walet.getIdCLient() != null) {
                    existingWalet.setIdCLient(walet.getIdCLient());
                }
                if (walet.getLogin() != null) {
                    existingWalet.setLogin(walet.getLogin());
                }
                if (walet.getPassword() != null) {
                    existingWalet.setPassword(walet.getPassword());
                }

                return existingWalet;
            })
            .map(waletRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, walet.getId().toString())
        );
    }

    /**
     * {@code GET  /walets} : get all the walets.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of walets in body.
     */
    @GetMapping("/walets")
    public List<Walet> getAllWalets() {
        log.debug("REST request to get all Walets");
        return waletRepository.findAll();
    }

    /**
     * {@code GET  /walets/:id} : get the "id" walet.
     *
     * @param id the id of the walet to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the walet, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/walets/{id}")
    public ResponseEntity<Walet> getWalet(@PathVariable Long id) {
        log.debug("REST request to get Walet : {}", id);
        Optional<Walet> walet = waletRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(walet);
    }

    /**
     * {@code DELETE  /walets/:id} : delete the "id" walet.
     *
     * @param id the id of the walet to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/walets/{id}")
    public ResponseEntity<Void> deleteWalet(@PathVariable Long id) {
        log.debug("REST request to delete Walet : {}", id);
        waletRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
