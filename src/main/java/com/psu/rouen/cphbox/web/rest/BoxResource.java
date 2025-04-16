package com.psu.rouen.cphbox.web.rest;

import com.google.zxing.WriterException;
import com.psu.rouen.cphbox.config.ApplicationProperties;
import com.psu.rouen.cphbox.domain.Box;
import com.psu.rouen.cphbox.domain.Warehouse;
import com.psu.rouen.cphbox.repository.BoxRepository;
import com.psu.rouen.cphbox.security.AuthoritiesConstants;
import com.psu.rouen.cphbox.service.BoxService;
import com.psu.rouen.cphbox.service.ServiceUtils;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.service.dto.PositionDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
@Slf4j
public class BoxResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "reference", "position", "warehouse", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
    );
    private final BoxService boxService;
    private final BoxRepository boxRepository;

    private final SpringTemplateEngine templateEngine;

    private final ApplicationProperties applicationProperties;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public BoxResource(
        BoxService boxService,
        BoxRepository boxRepository,
        SpringTemplateEngine templateEngine,
        ApplicationProperties applicationProperties
    ) {
        this.boxService = boxService;
        this.boxRepository = boxRepository;
        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;
    }

    @PostMapping("/boxes")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.BOX_CRUD + "\")")
    public ResponseEntity<BoxDTO> createBox(@Valid @RequestBody BoxDTO boxDTO) throws URISyntaxException {
        log.debug("REST request to create Box : {}", boxDTO);

        if (StringUtils.isNoneBlank(boxDTO.getId())) {
            throw new BadRequestAlertException("A new box cannot already have an ID", "boxManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else {
            BoxDTO createBox = boxService.createBox(boxDTO);

            return ResponseEntity
                .created(new URI("/api/boxes/" + createBox.getId()))
                .headers(
                    HeaderUtil.createAlert(applicationName, "A box is created with identifier " + createBox.getId(), createBox.getId())
                )
                .body(createBox);
        }
    }

    @PutMapping("/boxes")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.BOX_CRUD + "\")")
    public ResponseEntity<BoxDTO> updateBox(@Valid @RequestBody BoxDTO boxDTO) {
        log.debug("REST request to update Box : {}", boxDTO);
        if (StringUtils.isBlank(boxDTO.getId()) || StringUtils.isBlank(boxDTO.getReference())) {
            throw new BadRequestAlertException("A update box must have a ID and REFERENCE", "boxManagement", "idEmptyOrReferenceEmpty");
        }
        Optional<Box> existingBox = boxRepository.findOneByReference(boxDTO.getReference());
        if (existingBox.isPresent() && (!existingBox.get().getId().equals(boxDTO.getId()))) {
            throw new BadRequestAlertException("A update has already use, please change reference", "boxManagement", "referenceAlreadyUse");
        }

        Optional<BoxDTO> updateBox = boxService.updateBox(boxDTO);

        return ResponseUtil.wrapOrNotFound(
            updateBox,
            HeaderUtil.createAlert(applicationName, "A box is updated with identifier " + boxDTO.getId(), boxDTO.getId())
        );
    }

    @DeleteMapping("/boxes/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\",\"" + AuthoritiesConstants.BOX_CRUD + "\")")
    public ResponseEntity<Void> deleteBox(@PathVariable String id) {
        log.debug("REST request to delete Box: {}", id);
        boxService.deleteBox(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createAlert(applicationName, "A box is deleted with identifier " + id, id))
            .build();
    }

    @GetMapping("/boxes")
    public ResponseEntity<List<BoxDTO>> getAllBoxes(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BoxDTO> page = boxService.getAllBoxes(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/boxes/_search/{search}")
    public ResponseEntity<List<BoxDTO>> getAllBoxesbySearch(
        @PathVariable String search,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BoxDTO> page = boxService.searchAllBox(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/boxes/catalog/{searchCatalog}/warehouse/{searchWarehouse}")
    public ResponseEntity<List<BoxDTO>> getAllBoxByCatalogAndWarehouse(
        @PathVariable String searchCatalog,
        @PathVariable String searchWarehouse,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BoxDTO> page = boxService.searchAllBoxByCatalogAndWarehouse(searchCatalog,searchWarehouse, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/boxes/search/")
    public ResponseEntity<List<BoxDTO>> getAllBoxesForViewByBox(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BoxDTO> page = boxService.searchAllBoxForViewByBox("", pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/boxes/search/{search}")
    public ResponseEntity<List<BoxDTO>> getAllBoxesbySearchForViewByBox(
        @PathVariable String search,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<BoxDTO> page = boxService.searchAllBoxForViewByBox(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @GetMapping("/boxes/{id}")
    public ResponseEntity<BoxDTO> getBoxById(@PathVariable String id) {
        log.debug("REST request to get Box By id : {}", id);
        return ResponseUtil.wrapOrNotFound(boxRepository.findById(id).map(BoxDTO::new));
    }

    @GetMapping("/boxes/reference/{reference}")
    public ResponseEntity<BoxDTO> getBoxByReference(@PathVariable String reference) {
        log.debug("REST request to get Box By reference : {}", reference);
        return ResponseUtil.wrapOrNotFound(boxRepository.findOneByReference(reference).map(BoxDTO::new));
    }

    @GetMapping("/boxes/warehouse/{idwarehouse}")
    public List<BoxDTO> getBoxesByWarehouse(@PathVariable String idwarehouse) {
        log.debug("REST request to get all position");
        return boxRepository
            .findAllByWarehouse(Warehouse.builder().id(idwarehouse).build())
            .stream()
            .map(BoxDTO::new)
            .collect(Collectors.toList());
    }

    @GetMapping(value = "/boxes/pdf/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getPdfBox(@PathVariable String id) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Optional<BoxDTO> opBox = boxRepository.findById(id).map(BoxDTO::new);

        if (opBox.isPresent()) {
            //Locale locale = new Locale();
            Context context = new Context();
            BoxDTO box = opBox.get();

            context.setVariable("box", box);
            try {
                String qrCodeImage = ServiceUtils.getQRCodeImageBase64(
                    applicationProperties.getBaseUrlInfoBox() + box.getReference() + "/view",
                    250,
                    250
                );
                qrCodeImage = "data:image/png;base64," + qrCodeImage;
                context.setVariable("qrCodeImage", qrCodeImage);
            } catch (IOException | WriterException e) {
                log.error("Erreur generation QrCode", e);
            }
            String content = templateEngine.process("box/boxTagInfoPdf", context);

            // Create an ITextRenderer instance
            ITextRenderer renderer = new ITextRenderer();

            // Generate the PDF from an XHTML file or HTML content
            renderer.setDocumentFromString(content);

            renderer.layout();
            renderer.createPDF(outputStream);
            // Create an InputStreamResource from the output stream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            // Set the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=box-" + box.getReference() + "_" + Instant.now().getEpochSecond() + ".pdf"
            );
            headers.add(HttpHeaders.CONTENT_TYPE, "multipart / form-data");

            // Return the PDF as a ResponseEntity with the appropriate headers
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(inputStream));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
