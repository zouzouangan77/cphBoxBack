package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.repository.BoxCatalogRepository;
import com.psu.rouen.cphbox.service.dto.BookAvailableByWarehouseDTO;
import com.psu.rouen.cphbox.service.dto.BookAvailableDTO;
import com.psu.rouen.cphbox.service.dto.BoxCatalogDTO;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@Slf4j
public class BoxCatalogService {

    private final BoxCatalogRepository boxCatalogRepository;

    private final MongoTemplate mongoTemplate;

    public BoxCatalogService(BoxCatalogRepository boxCatalogRepository, MongoTemplate mongoTemplate) {
        this.boxCatalogRepository = boxCatalogRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<BookAvailableDTO> getAllBoxCatalog(String search, Pageable pageable) {
        LookupOperation lookupOperationCatalog = LookupOperation
            .newLookup()
            .from("catalog")
            .localField("catalog")
            .foreignField("_id")
            .as("cat");
        LookupOperation lookupOperationBox = LookupOperation.newLookup().from("box").localField("box").foreignField("_id").as("bx");
        LookupOperation lookupOperationStockBox = LookupOperation
            .newLookup()
            .from("box_stock")
            .localField("box_stocks")
            .foreignField("_id")
            .as("boxStocks");
        LookupOperation lookupOperationWarehouse = LookupOperation
            .newLookup()
            .from("ware_house")
            .localField("bx.warehouse")
            .foreignField("_id")
            .as("wh");
        GroupOperation groupByTitleAndWarehouse = Aggregation
            .group("cat.book", "wh.name")
            .sum("boxStocks.input_quantity")
            .as("inputQuantity")
            .sum("boxStocks.output_quantity")
            .as("outputQuantity")
            .sum("boxStocks.input_return_quantity")
            .as("inputReturnQuantity")
            .addToSet("cat.book")
            .as("book")
            .addToSet("wh.name")
            .as("warehouse_name");

        UnwindOperation unwindBoxStock = Aggregation.unwind("boxStocks");

        UnwindOperation unwindIdCatalog = Aggregation.unwind("book");
        UnwindOperation unwindIdWarehouse = Aggregation.unwind("warehouse_name");

        ProjectionOperation nbBookAvailableOperation = Aggregation
            .project()
            .andExpression("subtract(add(inputQuantity,inputReturnQuantity), outputQuantity)")
            .as("nbBookAvailable")
            .and("warehouse_name")
            .as("warehouse")
            .and("book")
            .as("book");

        MatchOperation filterNbBookAvailableOperation = Aggregation.match(Criteria.where("nbBookAvailable").gt(0));

        GroupOperation groupByTitleOperation = Aggregation
            .group("book")
            .sum("nbBookAvailable")
            .as("nbBookAvailable")
            .addToSet("book")
            .as("book")
            .addToSet("warehouse")
            .as("warehouse");
        MatchOperation matchOperationWithSearch = Aggregation.match(
            new Criteria().orOperator(Criteria.where("book").regex(search, "i"), Criteria.where("warehouse").regex(search, "i"))
        );
        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationCatalog,
            lookupOperationStockBox,
            lookupOperationBox,
            lookupOperationWarehouse,
            unwindBoxStock,
            groupByTitleAndWarehouse,
            unwindIdCatalog,
            unwindIdCatalog,
            unwindIdWarehouse,
            unwindIdWarehouse,
            nbBookAvailableOperation,
            filterNbBookAvailableOperation,
            groupByTitleOperation,
            unwindIdCatalog,
            matchOperationWithSearch,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );
        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationCatalog,
            lookupOperationStockBox,
            lookupOperationBox,
            lookupOperationWarehouse,
            unwindBoxStock,
            groupByTitleAndWarehouse,
            unwindIdCatalog,
            unwindIdCatalog,
            unwindIdWarehouse,
            unwindIdWarehouse,
            nbBookAvailableOperation,
            filterNbBookAvailableOperation,
            groupByTitleOperation,
            unwindIdCatalog,
            matchOperationWithSearch,
            Aggregation.count().as("count")
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "box_catalog", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        List<BookAvailableDTO> boxList = mongoTemplate.aggregate(aggregation, "box_catalog", BookAvailableDTO.class).getMappedResults();
        return PageableExecutionUtils.getPage(boxList, pageable, () -> totalCount);
    }

    public Page<BookAvailableByWarehouseDTO> getAllTitleByWarehouse(String search, Pageable pageable) {
        LookupOperation lookupOperationCatalog = LookupOperation
            .newLookup()
            .from("catalog")
            .localField("catalog")
            .foreignField("_id")
            .as("cat");
        LookupOperation lookupOperationBox = LookupOperation.newLookup().from("box").localField("box").foreignField("_id").as("bx");
        LookupOperation lookupOperationStockBox = LookupOperation
            .newLookup()
            .from("box_stock")
            .localField("box_stocks")
            .foreignField("_id")
            .as("boxStocks");
        LookupOperation lookupOperationWarehouse = LookupOperation
            .newLookup()
            .from("ware_house")
            .localField("bx.warehouse")
            .foreignField("_id")
            .as("wh");
        LookupOperation lookupOperationPosition = LookupOperation
            .newLookup()
            .from("position")
            .localField("bx.position")
            .foreignField("_id")
            .as("ptn");

        GroupOperation groupByTitleAndWarehouse = Aggregation
            .group("cat.book", "wh.name")
            .sum("boxStocks.input_quantity")
            .as("inputQuantity")
            .sum("boxStocks.output_quantity")
            .as("outputQuantity")
            .sum("boxStocks.input_return_quantity")
            .as("inputReturnQuantity")
            .addToSet("wh.name")
            .as("warehouse")
            .addToSet("cat.book")
            .as("book");

        ProjectionOperation nbBookAvailableOperation = Aggregation
            .project()
            .andExpression("subtract(add(inputQuantity,inputReturnQuantity), outputQuantity)")
            .as("nbBookAvailable")
            .and("warehouse")
            .as("warehouse")
            .and("book")
            .as("book");

        MatchOperation filterNbBookAvailableOperation = Aggregation.match(Criteria.where("nbBookAvailable").gt(0));

        UnwindOperation unwindBoxStock = Aggregation.unwind("boxStocks");
        UnwindOperation unwindIdCatalog = Aggregation.unwind("book");
        UnwindOperation unwindIdWarehouse = Aggregation.unwind("warehouse");

        GroupOperation groupByWarehouseOperation = Aggregation
            .group("book")
            .sum("nbBookAvailable")
            .as("nbBookAvailable")
            .addToSet("book")
            .as("book")
            .addToSet("warehouse")
            .as("warehouse");
        MatchOperation matchOperationWithSearch = Aggregation.match(
            new Criteria().orOperator(Criteria.where("book").regex(search, "i"), Criteria.where("warehouse").regex(search, "i"))
        );

        Aggregation aggregation = Aggregation.newAggregation(
            lookupOperationCatalog,
            lookupOperationStockBox,
            lookupOperationBox,
            lookupOperationWarehouse,
            lookupOperationPosition,
            unwindBoxStock,
            groupByTitleAndWarehouse,
            unwindIdCatalog,
            unwindIdCatalog,
            unwindIdWarehouse,
            unwindIdWarehouse,
            nbBookAvailableOperation,
            filterNbBookAvailableOperation,
            //groupByWarehouseOperation,
            matchOperationWithSearch,
            Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );
        Aggregation aggregationCount = Aggregation.newAggregation(
            lookupOperationCatalog,
            lookupOperationStockBox,
            lookupOperationBox,
            lookupOperationWarehouse,
            lookupOperationPosition,
            unwindBoxStock,
            groupByTitleAndWarehouse,
            unwindIdCatalog,
            unwindIdCatalog,
            unwindIdWarehouse,
            unwindIdWarehouse,
            nbBookAvailableOperation,
            filterNbBookAvailableOperation,
            //groupByWarehouseOperation,
            matchOperationWithSearch,
            Aggregation.count().as("count")
        );
        final Long totalCount = Optional
            .ofNullable(mongoTemplate.aggregate(aggregationCount, "box_catalog", Document.class).getUniqueMappedResult())
            .map(doc -> ((Integer) doc.get("count")).longValue())
            .orElse(0L);

        List<BookAvailableByWarehouseDTO> bookList = mongoTemplate
            .aggregate(aggregation, "box_catalog", BookAvailableByWarehouseDTO.class)
            .getMappedResults();
        return PageableExecutionUtils.getPage(bookList, pageable, () -> totalCount);
    }
}
