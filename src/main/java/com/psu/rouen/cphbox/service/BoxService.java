package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.*;
import com.psu.rouen.cphbox.repository.*;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class BoxService {

    private final BoxRepository boxRepository;
    private final PositionRepository positionRepository;

    private final WarehouseRepository warehouseRepository;

    private final BoxCatalogRepository boxCatalogRepository;

    private final BoxStockRepository boxStockRepository;

    private final CatalogRepository catalogRepository;

    private final MongoTemplate mongoTemplate;

    public BoxService(
        BoxRepository boxRepository,
        PositionRepository positionRepository,
        WarehouseRepository warehouseRepository,
        BoxCatalogRepository boxCatalogRepository,
        BoxStockRepository boxStockRepository,
        CatalogRepository catalogRepository,
        MongoTemplate mongoTemplate
    ) {
        this.boxRepository = boxRepository;
        this.positionRepository = positionRepository;
        this.warehouseRepository = warehouseRepository;
        this.boxCatalogRepository = boxCatalogRepository;
        this.boxStockRepository = boxStockRepository;
        this.catalogRepository = catalogRepository;
        this.mongoTemplate = mongoTemplate;
    }

    //    @Transactional(rollbackFor = BadRequestAlertException.class)
    public BoxDTO createBox(BoxDTO boxDTO) {
        if (StringUtils.isBlank(boxDTO.getReference())) {
            boxDTO.setReference(ServiceUtils.generateReference());
        }
        Box box = createUpdateBox(boxDTO, false);
        return new BoxDTO(box);
    }

    //   @Transactional(rollbackFor = BadRequestAlertException.class)
    public Optional<BoxDTO> updateBox(BoxDTO boxDTO) {
        return Optional.of(createUpdateBox(boxDTO, true)).map(BoxDTO::new);
    }

    private Box createUpdateBox(BoxDTO boxDTO, boolean isUpdate) {
        Box box = boxDTO.dtoToEntity();
        Optional<Box> oldBox = Optional.empty();

        if (isUpdate) {
            oldBox = boxRepository.findById(box.getId());

            if (oldBox.isEmpty()) {
                throw new BadRequestAlertException("Box '" + box.getId() + "' not found", "Box Updating", "NotFound Box ");
            }
        }

        if (StringUtils.isBlank(box.getId())) {
            box.setId(null);
        }

        //Ici on vérifie si l'entrepot existe, si la position existe et si la postion est bien celle de l'entrepot
        Warehouse warehouse = box.getWarehouse();
        Position position = box.getPosition();

        Optional<Warehouse> opWH = warehouseRepository.findById(warehouse.getId());
        Optional<Position> opP = positionRepository.findById(position.getId());

        if (!opP.isPresent() || !opWH.isPresent() || !opWH.get().getId().equals(opP.get().getWarehouse().getId())) {
            throw new BadRequestAlertException("Warehouse or Position is empty or not compatible", "Box Saving", "Ambigous Value");
        }

        //Si on est dans le cas d'un update on supprime d'abord les boxCatolg et les stocks Catalog de l'ancien
        if (isUpdate && oldBox.isPresent()) {
            if (!CollectionUtils.isEmpty(oldBox.get().getBoxCatalogs())) {
                oldBox
                    .get()
                    .getBoxCatalogs()
                    .stream()
                    .forEach(boxCatalog -> {
                        if (!CollectionUtils.isEmpty(boxCatalog.getBoxStocks())) {
                            boxStockRepository.deleteAll(boxCatalog.getBoxStocks());
                        }
                    });
                boxCatalogRepository.deleteAll(oldBox.get().getBoxCatalogs());
            }
        }

        Set<BoxCatalog> boxCatalogs = box.getBoxCatalogs();
        Set<BoxCatalog> newBoxCatalogs = new HashSet<>();
        if (!CollectionUtils.isEmpty(boxCatalogs)) {
            // Box finalBox = box;
            String idBox = box.getId();
            String reference = box.getReference();
            boxCatalogs
                .stream()
                .forEach(bc -> {
                    Catalog catalog = bc.getCatalog();
                    if (catalog != null) {
                        Optional<Catalog> catalogOptional = catalogRepository.findById(catalog.getId());
                        if (!catalogOptional.isPresent()) {
                            throw new BadRequestAlertException(
                                "Catalog '" + catalog.getId() + "' not found",
                                "Box Saving",
                                "NotFound Catalog "
                            );
                        }
                    } else {
                        throw new BadRequestAlertException("Empty Catalog", "Box Saving", "Empty Catalog");
                    }
                    Set<BoxStock> boxStocks = bc.getBoxStocks();
                    Set<BoxStock> newBoxStocks = new HashSet<>();

                    if (!CollectionUtils.isEmpty(boxStocks)) {
                        boxStocks
                            .stream()
                            .forEach(bs -> {
                                if (StringUtils.isBlank(bs.getId())) {
                                    bs.setId(null);
                                }
                                BoxStock bsSave = boxStockRepository.save(bs);
                                newBoxStocks.add(bsSave);
                            });
                        bc.setBoxStocks(newBoxStocks);
                    }
                    if (StringUtils.isBlank(bc.getId())) {
                        bc.setId(null);
                    }
                    bc.setBox(Box.builder().id(idBox).reference(reference).build());
                    BoxCatalog bcSave = boxCatalogRepository.save(bc);

                    newBoxCatalogs.add(bcSave);
                });
            box.setBoxCatalogs(newBoxCatalogs);
        }

        box = boxRepository.save(box);
        Box finalBox = box;
        newBoxCatalogs
            .stream()
            .forEach(newBC -> {
                newBC.setBox(finalBox);
                boxCatalogRepository.save(newBC);
            });
        //Mise à jours des boxCatalog avec le box nouvellement créé

        return box;
    }

    public void deleteBox(String id) {
        boxRepository
            .findById(id)
            .ifPresent(box -> {
                if (!CollectionUtils.isEmpty(box.getBoxCatalogs())) {
                    box
                        .getBoxCatalogs()
                        .stream()
                        .forEach(boxCatalog -> {
                            if (!CollectionUtils.isEmpty(boxCatalog.getBoxStocks())) {
                                boxCatalog
                                    .getBoxStocks()
                                    .stream()
                                    .forEach(boxStock -> {
                                        boxStockRepository.delete(boxStock);
                                    });
                            }
                            boxCatalogRepository.delete(boxCatalog);
                        });
                }
                boxRepository.delete(box);
                log.debug("Deleted Box: {}", box);
            });
    }

    public Page<BoxDTO> getAllBoxes(Pageable pageable) {
        return boxRepository.findAll(pageable).map(BoxDTO::new);
    }

    public Page<BoxDTO> searchAllBox(String search, Pageable pageable) {
        LookupOperation lookupOperationWH = LookupOperation
            .newLookup()
            .from("ware_house")
            .localField("warehouse")
            .foreignField("_id")
            .as("wh");

        LookupOperation lookupOperationBC = LookupOperation
            .newLookup()
            .from("box_catalog")
            .localField("box_catalogs")
            .foreignField("_id")
            .as("boxcatalogs");

        LookupOperation lookupOperationCat = LookupOperation
            .newLookup()
            .from("catalog")
            .localField("boxcatalogs.catalog")
            .foreignField("_id")
            .as("catalog");
        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("reference").regex(search, "i"),
                    Criteria.where("wh.name").regex(search, "i"),
                    Criteria.where("catalog.book").regex(search, "i")
                )
        );

        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationWH,
            lookupOperationBC,
            lookupOperationCat,
            matchOperation,
            Aggregation.count().as("count")
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "box", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationWH,
            lookupOperationBC,
            lookupOperationCat,
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );

        List<BoxDTO> boxList = mongoTemplate
            .aggregate(aggregation, "box", Box.class)
            .getMappedResults()
            .stream()
            .map(BoxDTO::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(boxList, pageable, () -> totalCount);
    }

    public Page<BoxDTO> searchAllBoxForViewByBox(String search, Pageable pageable) {
        LookupOperation lookupOperationWH = LookupOperation
            .newLookup()
            .from("ware_house")
            .localField("warehouse")
            .foreignField("_id")
            .as("wh");

        LookupOperation lookupOperationBC = LookupOperation
            .newLookup()
            .from("box_catalog")
            .localField("box_catalogs")
            .foreignField("_id")
            .as("boxcatalogs");

        LookupOperation lookupOperationCat = LookupOperation
            .newLookup()
            .from("catalog")
            .localField("boxcatalogs.catalog")
            .foreignField("_id")
            .as("catalog");
        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("reference").regex(search, "i"),
                    Criteria.where("wh.name").regex(search, "i"),
                    Criteria.where("catalog.book").regex(search, "i")
                )
        );

        UnwindOperation unwindCatalogOperation = Aggregation.unwind("box_catalogs");
        //MatchOperation filterNbBookAvailableOperation = Aggregation.match(Criteria.where("nbTotalBook").gt(0));

        UnwindOperation unwindBoxCatalogsOperation = Aggregation.unwind("boxCatalogs");
        Aggregation aggregationCount = Aggregation.newAggregation(
            unwindCatalogOperation,
            lookupOperationWH,
            lookupOperationBC,
            lookupOperationCat,
            //filterNbBookAvailableOperation,
            matchOperation,
            Aggregation.count().as("count")
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "box", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        Aggregation aggregation = Aggregation.newAggregation(
            unwindCatalogOperation,
            lookupOperationWH,
            lookupOperationBC,
            lookupOperationCat,
            //filterNbBookAvailableOperation,
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );

        List<BoxDTO> boxList = mongoTemplate
            .aggregate(aggregation, "box", Box.class)
            .getMappedResults()
            .stream()
            .map(BoxDTO::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(boxList, pageable, () -> totalCount);
    }

    public Page<BoxDTO> searchAllBoxByCatalogAndWarehouse(String searchCatalog,String searchWarehouse, Pageable pageable) {
        LookupOperation lookupOperationWH = LookupOperation
            .newLookup()
            .from("ware_house")
            .localField("warehouse")
            .foreignField("_id")
            .as("wh");

        LookupOperation lookupOperationBC = LookupOperation
            .newLookup()
            .from("box_catalog")
            .localField("box_catalogs")
            .foreignField("_id")
            .as("boxcatalogs");

        LookupOperation lookupOperationCat = LookupOperation
            .newLookup()
            .from("catalog")
            .localField("boxcatalogs.catalog")
            .foreignField("_id")
            .as("catalog");
        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .andOperator(
                    Criteria.where("catalog.book").regex(searchCatalog, "i"),
                    Criteria.where("wh.name").regex(searchWarehouse, "i")
                )
        );

        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationWH,
            lookupOperationBC,
            lookupOperationCat,
            matchOperation,
            Aggregation.count().as("count")
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "box", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationWH,
            lookupOperationBC,
            lookupOperationCat,
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );

        List<BoxDTO> boxList = mongoTemplate
            .aggregate(aggregation, "box", Box.class)
            .getMappedResults()
            .stream()
            .map(BoxDTO::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(boxList, pageable, () -> totalCount);
    }
}
