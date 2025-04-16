package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.repository.ApplicationLogRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.ApplicationLogService;
import com.psu.rouen.cphbox.web.rest.vm.ApplicationLogVM;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApplicationLogResource {

    private final ApplicationLogRepository applicationLogRepository;
    private final ApplicationLogService applicationLogService;
    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("login", "operation", "endPoint", "method", "params", "createDate")
    );

    public ApplicationLogResource(ApplicationLogRepository applicationLogRepository, ApplicationLogService applicationLogService) {
        this.applicationLogRepository = applicationLogRepository;
        this.applicationLogService = applicationLogService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<ApplicationLogVM>> getAllLogs(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all logs for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<ApplicationLogVM> page = applicationLogRepository.findAll(pageable).map(ApplicationLogVM::new);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/logs/_search/{search}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<ApplicationLogVM>> getAllLogesbySearch(@PathVariable String search, @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all logs for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<ApplicationLogVM> page = applicationLogService.searchAllBox(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
}
