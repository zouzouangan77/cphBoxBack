package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.domain.Language;
import com.psu.rouen.cphbox.repository.CatalogRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.CatalogService;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.service.dto.CatalogDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
@Slf4j
public class CatalogResource {

    private CatalogRepository catalogRepository;
    private final CatalogService catalogService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList(
            "id",
            "book",
            "comment",
            "price",
            "language",
            "author",
            "createdBy",
            "createdDate",
            "lastModifiedBy",
            "lastModifiedDate"
        )
    );

    public CatalogResource(CatalogRepository catalogRepository, CatalogService catalogService) {
        this.catalogRepository = catalogRepository;
        this.catalogService = catalogService;
    }

    /*@GetMapping("/_search/catalogs/{book}")
    public List<CatalogDTO> search(@PathVariable String book) {
        return catalogSearchRepository.findByBook("*" + book + "*").stream().map(CatalogDTO::new).collect(Collectors.toList());
    }*/

    /*@GetMapping("/catalogs/_search/{query}")
    public List<CatalogDTO> search2(@PathVariable String query) {
        return StreamSupport
            .stream(catalogSearchRepository.search(query).spliterator(), false)
            .map(CatalogDTO::new)
            .collect(Collectors.toList());
    }*/
    @GetMapping("/catalogs/language/{idlanguage}")
    public List<CatalogDTO> getcatalogsByLanguage(@PathVariable String idlanguage) {
        log.debug("REST request to get all position");
        return catalogRepository
            .findAllByLanguage(Language.builder().id(idlanguage).build())
            .stream()
            .map(CatalogDTO::new)
            .collect(Collectors.toList());
    }

    @GetMapping("/catalogs/newcatalog")
    public List<CatalogDTO> getcatalogs() {
        log.debug("REST request to get all warehouse");
        return catalogRepository.findAll().stream().map(CatalogDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/catalogs/_searchElem/{search}")
    public ResponseEntity<List<CatalogDTO>> getAllCatalogbySearch(
        @PathVariable String search,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<CatalogDTO> page = catalogService.searchAllCatalog(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/catalogs")
    public ResponseEntity<List<CatalogDTO>> getAllBoxes(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<CatalogDTO> page = catalogService.getAllBoxes(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @PostMapping("/catalogs")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.CATALOG_CRUD + "\")")
    public ResponseEntity<CatalogDTO> createCatalog(@Valid @RequestBody CatalogDTO catalogDTO) throws URISyntaxException {
        if (StringUtils.isNoneBlank(catalogDTO.getId())) {
            throw new BadRequestAlertException("A new catalog cannot already have an ID", "catalogManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else {
            CatalogDTO createCatalog = catalogService.createCatalog(catalogDTO);

            return ResponseEntity
                .created(new URI("/api/catalogs/" + createCatalog.getId()))
                .headers(
                    HeaderUtil.createAlert(
                        applicationName,
                        "A warehouse is created with identifier " + createCatalog.getId(),
                        createCatalog.getId()
                    )
                )
                .body(createCatalog);
        }
    }

    @PutMapping("/catalogs")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.CATALOG_CRUD + "\")")
    public ResponseEntity<CatalogDTO> updateCatalog(@Valid @RequestBody CatalogDTO catalogDTO) {
        Optional<CatalogDTO> updateCatalog = catalogService.updateCatalog(catalogDTO);

        return ResponseUtil.wrapOrNotFound(
            updateCatalog,
            HeaderUtil.createAlert(applicationName, "A catalog is updated with identifier " + catalogDTO.getId(), catalogDTO.getId())
        );
    }

    @GetMapping("/catalogs/{id}")
    public ResponseEntity<CatalogDTO> getCatalogById(@PathVariable String id) {
        log.debug("REST request to get Box By id : {}", id);
        return ResponseUtil.wrapOrNotFound(catalogRepository.findById(id).map(CatalogDTO::new));
    }
}
