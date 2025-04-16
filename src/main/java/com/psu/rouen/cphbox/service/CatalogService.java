package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.Box;
import com.psu.rouen.cphbox.domain.BoxCatalog;
import com.psu.rouen.cphbox.domain.Catalog;
import com.psu.rouen.cphbox.domain.Warehouse;
import com.psu.rouen.cphbox.repository.*;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.service.dto.CatalogDTO;
import com.psu.rouen.cphbox.service.dto.WarehouseDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final MongoTemplate mongoTemplate;
    private final LanguageRepository languageRepository;

    private final BoxCatalogRepository boxCatalogRepository;

    private final AuthorRepository authorRepository;
    private final BoxRepository boxRepository;

    public CatalogService(
        CatalogRepository catalogRepository,
        LanguageRepository languageRepository,
        AuthorRepository authorRepository,
        BoxCatalogRepository boxCatalogRepository,
        BoxRepository boxRepository,
        MongoTemplate mongoTemplate
    ) {
        this.catalogRepository = catalogRepository;
        this.languageRepository = languageRepository;
        this.authorRepository = authorRepository;
        this.boxCatalogRepository = boxCatalogRepository;
        this.boxRepository = boxRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public CatalogDTO createCatalog(CatalogDTO catalogDTO) {
        Catalog catalog = createUpdateCatalog(catalogDTO, false);
        return new CatalogDTO(catalog);
    }

    public Optional<CatalogDTO> updateCatalog(CatalogDTO catalogDTO) {
        createUpdateCatalog(catalogDTO, true);
        return Optional.of(createUpdateCatalog(catalogDTO, true)).map(CatalogDTO::new);
    }

    private Catalog createUpdateCatalog(CatalogDTO catalogDTO, Boolean isUpdate) {
        Catalog catalog = catalogDTO.dtoToEntity();
        final String id = catalog.getId();
        if (isUpdate) {
            if (StringUtils.isBlank(id) || catalogRepository.findById(id).isEmpty()) {
                throw new BadRequestAlertException("Catalog '" + catalog.getId() + "' not found", "Catalog Updating", "NotFound Catalog ");
            }

            catalogRepository
                .findOneByBookIgnoreCase(catalog.getBook())
                .ifPresent(catalog1 -> {
                    if (catalog1.getId().equals(id)) {
                        throw new BadRequestAlertException(
                            "Catalog '" + catalog1.getBook() + "' Already Exists",
                            "Catalog Saving",
                            "Already Exists"
                        );
                    }
                });
        } else {
            if (catalogRepository.findOneByBookIgnoreCase(catalog.getBook()).isPresent()) {
                throw new BadRequestAlertException(
                    "Catalog '" + catalog.getBook() + "' Already Exists",
                    "Catalog Saving",
                    "Already Exists"
                );
            }
        }

        if (StringUtils.isBlank(catalog.getId())) {
            catalog.setId(null);
        }

        catalog = catalogRepository.save(catalog);

        return catalog;
    }

    public Page<CatalogDTO> getAllBoxes(Pageable pageable) {
        return catalogRepository.findAll(pageable).map(CatalogDTO::new);
    }

    public Page<CatalogDTO> searchAllCatalog(String search, Pageable pageable) {
        LookupOperation lookupOperationLA = LookupOperation
            .newLookup()
            .from("language")
            .localField("language")
            .foreignField("_id")
            .as("languages");

        LookupOperation lookupOperationAU = LookupOperation.newLookup().from("author").localField("author").foreignField("_id").as("zdth");

        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("book").regex(search, "i"),
                    Criteria.where("languages.name").regex(search, "i"),
                    Criteria.where("zdth.name").regex(search, "i")
                )
        );

        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationLA,
            lookupOperationAU,
            matchOperation,
            Aggregation.count().as("count")
        );

        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "catalog", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationLA,
            lookupOperationAU,
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );

        List<CatalogDTO> catalogList = mongoTemplate
            .aggregate(aggregation, "catalog", Catalog.class)
            .getMappedResults()
            .stream()
            .map(CatalogDTO::new)
            .collect(Collectors.toList());
        log.debug("catalogList", catalogList);
        return PageableExecutionUtils.getPage(catalogList, pageable, () -> totalCount);
    }
}
