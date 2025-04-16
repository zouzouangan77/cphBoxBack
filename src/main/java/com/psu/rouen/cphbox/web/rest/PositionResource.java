package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.domain.Warehouse;
import com.psu.rouen.cphbox.repository.PositionRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.PositionService;
import com.psu.rouen.cphbox.service.dto.PositionDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
@Slf4j
public class PositionResource {

    private final PositionRepository positionRepository;

    private final PositionService positionService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public PositionResource(PositionRepository positionRepository, PositionService positionService) {
        this.positionRepository = positionRepository;
        this.positionService = positionService;
    }

    @GetMapping("/positions/warehouse/{idWarehouse}")
    public List<PositionDTO> getAllPositionByWarehouse(@PathVariable String idWarehouse) {
        log.debug("REST request to get all position");
        return positionRepository
            .findAllByWarehouse(Warehouse.builder().id(idWarehouse).build())
            .stream()
            .map(PositionDTO::new)
            .collect(Collectors.toList());
    }

    @GetMapping("/positions/warehouse/{idWarehouse}/_search/{name}")
    public List<PositionDTO> getAllPositionByWarehouseAndName(@PathVariable String idWarehouse, @PathVariable String name) {
        log.debug("REST request to get all position");
        return positionRepository
            .findAllByWarehouseAndNameContainingIgnoreCase(Warehouse.builder().id(idWarehouse).build(), name)
            .stream()
            .map(PositionDTO::new)
            .collect(Collectors.toList());
    }

    @PostMapping("/positions")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.BOX_CRUD + "\")")
    public ResponseEntity<PositionDTO> createPosition(@Valid @RequestBody PositionDTO positionDTO) throws URISyntaxException {
        log.debug("REST request to create Position : {}", positionDTO);

        if (StringUtils.isNoneBlank(positionDTO.getId())) {
            throw new BadRequestAlertException("A new position cannot already have an ID", "positionManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else {
            PositionDTO createPosition = positionService.createPosition(positionDTO);

            return ResponseEntity
                .created(new URI("/api/positions/" + createPosition.getId()))
                .headers(
                    HeaderUtil.createAlert(
                        applicationName,
                        "A position is created with identifier " + createPosition.getId(),
                        createPosition.getId()
                    )
                )
                .body(createPosition);
        }
    }

    @PutMapping("/positions")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.BOX_CRUD + "\")")
    public ResponseEntity<PositionDTO> updatePosition(@Valid @RequestBody PositionDTO positionDTO) {
        log.debug("REST request to update Position : {}", positionDTO);

        Optional<PositionDTO> updatePosition = positionService.updatePosition(positionDTO);

        return ResponseUtil.wrapOrNotFound(
            updatePosition,
            HeaderUtil.createAlert(applicationName, "A position is updated with identifier " + positionDTO.getId(), positionDTO.getId())
        );
    }

    @DeleteMapping("/positions/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.BOX_CRUD + "\")")
    public ResponseEntity<Void> deletePosition(@PathVariable String id) {
        log.debug("REST request to delete Position: {}", id);
        positionService.deletePosition(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createAlert(applicationName, "A position is deleted with identifier " + id, id))
            .build();
    }
}
