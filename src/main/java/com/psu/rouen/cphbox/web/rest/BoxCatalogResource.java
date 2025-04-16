package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.config.ApplicationProperties;

import com.psu.rouen.cphbox.repository.BoxCatalogRepository;
import com.psu.rouen.cphbox.repository.BoxRepository;
import com.psu.rouen.cphbox.repository.CatalogRepository;
import com.psu.rouen.cphbox.service.BoxCatalogService;

import com.psu.rouen.cphbox.service.dto.BookAvailableByWarehouseDTO;
import com.psu.rouen.cphbox.service.dto.BookAvailableDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.web.util.PaginationUtil;


@RestController
@RequestMapping("/api")
@Slf4j
public class BoxCatalogResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "reference", "position", "warehouse", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
    );
    private final BoxCatalogService boxCatalogService;
    private final BoxCatalogRepository boxCatalogRepository;

    private final SpringTemplateEngine templateEngine;

    private final ApplicationProperties applicationProperties;


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public BoxCatalogResource(
        BoxCatalogService boxCatalogService,
        BoxCatalogRepository boxCatalogRepository,
        SpringTemplateEngine templateEngine,
        ApplicationProperties applicationProperties,
        BoxRepository boxRepository,
        CatalogRepository catalogRepository
    ) {
        this.boxCatalogService = boxCatalogService;
        this.boxCatalogRepository = boxCatalogRepository;
        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;

    }

    @GetMapping("/boxCatalog/viewByTitle")
    public ResponseEntity<List<BookAvailableDTO>> getAllTitles(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BookAvailableDTO> page = boxCatalogService.getAllBoxCatalog("", pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/boxCatalog/viewByTitle/{search}")
    public ResponseEntity<List<BookAvailableDTO>> getAllTitlesWithSearch(
        @PathVariable String search,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BookAvailableDTO> page = boxCatalogService.getAllBoxCatalog(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/boxCatalog/viewByWarehouse")
    public ResponseEntity<List<BookAvailableByWarehouseDTO>> getAllTitlesByWarehouse(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BookAvailableByWarehouseDTO> page = boxCatalogService.getAllTitleByWarehouse("", pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/boxCatalog/viewByWarehouse/{search}")
    public ResponseEntity<List<BookAvailableByWarehouseDTO>> getAllTitlesByWarehouseWithSearch(
        @PathVariable String search,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BookAvailableByWarehouseDTO> page = boxCatalogService.getAllTitleByWarehouse(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
}
