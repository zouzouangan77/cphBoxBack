package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.Warehouse;
import com.psu.rouen.cphbox.repository.BoxRepository;
import com.psu.rouen.cphbox.repository.PositionRepository;
import com.psu.rouen.cphbox.repository.WarehouseRepository;
import com.psu.rouen.cphbox.service.dto.BoxDTO;
import com.psu.rouen.cphbox.service.dto.WarehouseDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BoxRepository boxRepository;

    private final PositionRepository positionRepository;
    private final MongoTemplate mongoTemplate;

    public WarehouseService(WarehouseRepository warehouseRepository, BoxRepository boxRepository, PositionRepository positionRepository, MongoTemplate mongoTemplate) {
        this.warehouseRepository = warehouseRepository;
        this.boxRepository = boxRepository;
        this.positionRepository = positionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = createUpdateWarehouse(warehouseDTO, false);
        return new WarehouseDTO(warehouse);
    }

    public Page<WarehouseDTO> getAllWarehouse(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(WarehouseDTO::new);
    }


    public Optional<WarehouseDTO> updateWarehouse(WarehouseDTO warehouseDTO) {
        createUpdateWarehouse(warehouseDTO, true);
        return Optional.of(createUpdateWarehouse(warehouseDTO, true)).map(WarehouseDTO::new);
    }

    private Warehouse createUpdateWarehouse(WarehouseDTO warehouseDTO, Boolean isUpdate) {
        Warehouse warehouse = warehouseDTO.dtoToEntity();
        final String id = warehouse.getId();
        if (isUpdate) {
            if (StringUtils.isBlank(id) || warehouseRepository.findById(id).isEmpty()) {
                throw new BadRequestAlertException(
                    "Warehouse '" + warehouse.getId() + "' not found",
                    "Warehouse Updating",
                    "NotFound Warehouse "
                );
            }

            warehouseRepository
                .findOneByNameIgnoreCase(warehouse.getName())
                .ifPresent(warehouse1 -> {
                    if (!warehouse1.getId().equals(id)) {
                        throw new BadRequestAlertException(
                            "Warehouse '" + warehouse1.getName() + "' Already Exists",
                            "Warehouse Saving",
                            "Already Exists"
                        );
                    }
                });
        } else {
            if (warehouseRepository.findOneByNameIgnoreCase(warehouse.getName()).isPresent()) {
                throw new BadRequestAlertException(
                    "Warehouse '" + warehouse.getName() + "' Already Exists",
                    "Warehouse Saving",
                    "Already Exists"
                );
            }
        }

        if (StringUtils.isBlank(warehouse.getId())) {
            warehouse.setId(null);
        }

        warehouse = warehouseRepository.save(warehouse);

        return warehouse;
    }

    public void deleteWarehouse(String id) {
        warehouseRepository
            .findById(id)
            .ifPresent(warehouse -> {
                boxRepository
                    .findFirstByWarehouse(warehouse)
                    .ifPresentOrElse(
                        box -> {
                            throw new BadRequestAlertException(
                                "Impossible to delete Warehouse is already used by a box",
                                "Warehouse deleting",
                                "Already used somewhere"
                            );
                        },
                        () -> {
                            positionRepository.deleteAll(warehouse.getPositions());
                            warehouseRepository.delete(warehouse);
                        }
                    );
            });
    }

    public Page<WarehouseDTO> searchAllWarehouse(String search, Pageable pageable) {
        MatchOperation matchOperation = Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("address").regex(search, "i")

                )
        );
        Aggregation aggregationCount = Aggregation.newAggregation(
            matchOperation,
            Aggregation.count().as("count")
        );
        Aggregation aggregation = Aggregation.newAggregation(
            matchOperation,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "ware_house", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        List<WarehouseDTO> warehouseList = mongoTemplate
            .aggregate(aggregation, "ware_house", Warehouse.class)
            .getMappedResults()
            .stream()
            .map(WarehouseDTO::new)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(warehouseList, pageable, () -> totalCount);
    }

}
