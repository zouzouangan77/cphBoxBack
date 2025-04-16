package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.repository.LanguageRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.dto.LanguageDTO;
import com.psu.rouen.cphbox.service.dto.WarehouseDTO;
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
public class LanguageResource {

    private final LanguageRepository languageRepository;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public LanguageResource(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @GetMapping("/languages")
    public List<LanguageDTO> getAllLanguages() {
        log.debug("REST request to get all warehouse");
        return languageRepository.findAll().stream().map(LanguageDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/languages/_search/{name}")
    public List<LanguageDTO> getAllLanguagesByName(@PathVariable String name) {
        log.debug("REST request to get all warehouse");
        return languageRepository.findByNameContainingIgnoreCase(name).stream().map(LanguageDTO::new).collect(Collectors.toList());
    }
}
