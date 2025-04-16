package com.psu.rouen.cphbox.config.dbmigrations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psu.rouen.cphbox.domain.Position;
import com.psu.rouen.cphbox.domain.Warehouse;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.CollectionUtils;

@ChangeUnit(id = "warehouses-initialization", order = "004")
@Slf4j
public class LoadWarehousePosition20230524 {

    private final MongoTemplate template;

    private ObjectMapper objectMapper = new ObjectMapper();

    public LoadWarehousePosition20230524(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Warehouse> warehouses = loadWarehouse();

        if (!CollectionUtils.isEmpty(warehouses)) {
            warehouses
                .stream()
                .forEach(warehouse -> {
                    Set<Position> newPositions = new HashSet<>();
                    Warehouse newWarehouse = template.save(warehouse);
                    Set<Position> positions = newWarehouse.getPositions();

                    if (!CollectionUtils.isEmpty(positions)) {
                        positions
                            .stream()
                            .forEach(position -> {
                                position.setWarehouse(Warehouse.builder().id(newWarehouse.getId()).build());
                                newPositions.add(template.insert(position));
                            });
                        newWarehouse.setPositions(newPositions);
                        template.save(newWarehouse);
                    }
                });
        }
    }

    @RollbackExecution
    public void rollback() {}

    private List<Warehouse> loadWarehouse() {
        try {
            return objectMapper.readValue(
                new File("src/main/resources/loadData/20230524_warehouse.json"),
                new TypeReference<List<Warehouse>>() {}
            );
        } catch (IOException e) {
            log.error("error load loadData/20230524_warehouse.json", e);
        }
        return null;
    }
}
