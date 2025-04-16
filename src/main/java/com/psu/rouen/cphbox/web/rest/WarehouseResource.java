package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.repository.WarehouseRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.WarehouseService;
import com.psu.rouen.cphbox.service.dto.WarehouseDTO;
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
public class WarehouseResource {

    private final WarehouseRepository warehouseRepository;
    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "name","address", "positions",  "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
    );

    private final WarehouseService warehouseService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public WarehouseResource(WarehouseRepository warehouseRepository, WarehouseService warehouseService) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<WarehouseDTO> page = warehouseService.getAllWarehouse(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/warehouses/_search/{name}")
    public List<WarehouseDTO> getAllWarehousesByName(@PathVariable String name) {
        log.debug("REST request to get all warehouse");
        return warehouseRepository.findByNameContainingIgnoreCase(name).stream().map(WarehouseDTO::new).collect(Collectors.toList());
    }

    @PostMapping("/warehouses")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.WAREHOUSE_CRUD + "\")")
    public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody WarehouseDTO warehouseDTO) throws URISyntaxException {
        log.debug("REST request to create Warehouse : {}", warehouseDTO);

        if (StringUtils.isNoneBlank(warehouseDTO.getId())) {
            throw new BadRequestAlertException("A new warehouse cannot already have an ID", "warehouseManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else {
            WarehouseDTO createWarehouse = warehouseService.createWarehouse(warehouseDTO);

            return ResponseEntity
                .created(new URI("/api/warehouses/" + createWarehouse.getId()))
                .headers(
                    HeaderUtil.createAlert(
                        applicationName,
                        "A warehouse is created with identifier " + createWarehouse.getId(),
                        createWarehouse.getId()
                    )
                )
                .body(createWarehouse);
        }
    }

    @PutMapping("/warehouses")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.WAREHOUSE_CRUD + "\")")
    public ResponseEntity<WarehouseDTO> updateWarehouse(@Valid @RequestBody WarehouseDTO warehouseDTO) {
        log.debug("REST request to update Warehouse : {}", warehouseDTO);

        Optional<WarehouseDTO> updateWarehouse = warehouseService.updateWarehouse(warehouseDTO);

        return ResponseUtil.wrapOrNotFound(
            updateWarehouse,
            HeaderUtil.createAlert(applicationName, "A warehouse is updated with identifier " + warehouseDTO.getId(), warehouseDTO.getId())
        );
    }

    @DeleteMapping("/warehouses/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.WAREHOUSE_CRUD+ "\")")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable String id) {
        log.debug("REST request to delete Warehouse: {}", id);
        try {
            warehouseService.deleteWarehouse(id);

            return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createAlert(applicationName, "A warehouse is deleted with identifier " + id, id))
                .build();

        }catch (BadRequestAlertException e){
            return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createAlert(applicationName, "Impossible to delete Warehouse is already used by a box " + id, id))
                .build();

        }


    }

    @GetMapping("/warehouses/{id}")
    public ResponseEntity<WarehouseDTO> getWarehousesById(@PathVariable String id) {

        return ResponseUtil.wrapOrNotFound(warehouseRepository.findById(id).map(WarehouseDTO::new));
    }

    @GetMapping("/warehouses/search/{search}")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehousebySearch(@PathVariable String search, @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<WarehouseDTO> page = warehouseService.searchAllWarehouse(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }



}
