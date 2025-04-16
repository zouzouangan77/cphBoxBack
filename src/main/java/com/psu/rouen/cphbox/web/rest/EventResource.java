package com.psu.rouen.cphbox.web.rest;

import com.psu.rouen.cphbox.config.ApplicationProperties;
import com.psu.rouen.cphbox.domain.Box;

import com.psu.rouen.cphbox.domain.Event;
import com.psu.rouen.cphbox.repository.EventRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;

import com.psu.rouen.cphbox.service.EventService;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.service.dto.EventDTO;
import com.psu.rouen.cphbox.service.dto.OrderDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
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
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@Slf4j
public class EventResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "name", "address", "comment","eventDate", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
    );
    private final EventService eventService;
    private final EventRepository eventRepository;

    private final SpringTemplateEngine templateEngine;

    private final ApplicationProperties applicationProperties;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public EventResource(
        EventService eventService, EventRepository eventRepository, SpringTemplateEngine templateEngine,
        ApplicationProperties applicationProperties
    ) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;

        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;
    }

    @PostMapping("/events")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.EVENT_CRUD + "\")")
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) throws URISyntaxException {
        log.debug("**********************************************************");
        log.debug("REST request to create Event : {}", eventDTO);

        if (StringUtils.isNoneBlank(eventDTO.getId())) {
            throw new BadRequestAlertException("A new event cannot already have an ID", "eventManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else {
            EventDTO createEvent = eventService.createEvent(eventDTO);

            return ResponseEntity
                .created(new URI("/api/events/" + createEvent.getId()))
                .headers(
                    HeaderUtil.createAlert(applicationName, "A event is created with identifier " + createEvent.getId(), createEvent.getId())
                )
                .body(createEvent);
        }
    }

    @PutMapping("/events")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.EVENT_CRUD + "\")")
    public ResponseEntity<EventDTO> updateBox(@Valid @RequestBody EventDTO eventDTO) {
            log.debug("REST request to update Event : {}", eventDTO);
        if (StringUtils.isBlank(eventDTO.getId()) || StringUtils.isBlank(eventDTO.getName())) {
            throw new BadRequestAlertException("A update box must have a ID and NAME", "eventManagement", "idEmptyOrReferenceEmpty");
        }
        Optional<Event> existingEvent = eventRepository.findOneByNameIgnoreCase(eventDTO.getName());
        if (existingEvent.isPresent() && (!existingEvent.get().getId().equals(eventDTO.getId()))) {
            throw new BadRequestAlertException("A update has already use, please change reference", "eventManagement", "referenceAlreadyUse");
        }

        Optional<EventDTO> updateEvent = eventService.updateEvent(eventDTO);

        return ResponseUtil.wrapOrNotFound(
            updateEvent,
            HeaderUtil.createAlert(applicationName, "A event is updated with identifier " + eventDTO.getId(), eventDTO.getId())
        );
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.EVENT_CRUD + "\")")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        log.debug("REST request to delete Box: {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createAlert(applicationName, "A event is deleted with identifier " + id, id))
            .build();
    }
    @GetMapping("/events")
    public ResponseEntity<List<EventDTO>> getAllEvent(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<EventDTO> page = eventService.getAllEvent(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
    @GetMapping("/events/{id}")
    public ResponseEntity<EventDTO> getBoxById(@PathVariable String id) {
        log.debug("REST request to get Box By id : {}", id);
        return ResponseUtil.wrapOrNotFound(eventRepository.findById(id).map(EventDTO::new));
    }


}
