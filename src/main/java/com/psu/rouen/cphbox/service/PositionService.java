package com.psu.rouen.cphbox.service;

import com.psu.rouen.cphbox.domain.Position;
import com.psu.rouen.cphbox.domain.Warehouse;
import com.psu.rouen.cphbox.repository.BoxRepository;
import com.psu.rouen.cphbox.repository.PositionRepository;
import com.psu.rouen.cphbox.repository.WarehouseRepository;
import com.psu.rouen.cphbox.service.dto.PositionDTO;
import com.psu.rouen.cphbox.web.rest.errors.BadRequestAlertException;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PositionService {

    private final WarehouseRepository warehouseRepository;
    private final PositionRepository positionRepository;

    private final BoxRepository boxRepository;

    public PositionService(WarehouseRepository warehouseRepository, PositionRepository positionRepository, BoxRepository boxRepository) {
        this.warehouseRepository = warehouseRepository;
        this.positionRepository = positionRepository;
        this.boxRepository = boxRepository;
    }

    public PositionDTO createPosition(PositionDTO positionDTO) {
        Position position = createUpdatePosition(positionDTO, false);
        return new PositionDTO(position);
    }

    public Optional<PositionDTO> updatePosition(PositionDTO positionDTO) {
        return Optional.of(createUpdatePosition(positionDTO, true)).map(PositionDTO::new);
    }

    private Position createUpdatePosition(PositionDTO positionDTO, Boolean isUpdate) {
        Position position = positionDTO.dtoToEntity();

        Warehouse warehouse = position.getWarehouse();
        final String id = position.getId();

        if (warehouse == null || warehouseRepository.findById(warehouse.getId()).isEmpty()) {
            throw new BadRequestAlertException("Warehouse " + warehouse.getId() + " not found", "Position Saving", "NotFound warehouse ");
        }

        if (isUpdate) {
            if (StringUtils.isBlank(id) || positionRepository.findById(id).isEmpty()) {
                throw new BadRequestAlertException("Position '" + id + "' not found", "Position Updating", "NotFound Position ");
            }
            positionRepository
                .findOneByWarehouseAndNameIgnoreCase(warehouse, position.getName())
                .ifPresent(position1 -> {
                    if (!position1.getId().equals(id)) {
                        throw new BadRequestAlertException(
                            "Position '" + position1.getName() + "' Already Exists",
                            "Position Saving",
                            "Already Exists"
                        );
                    }
                });
        } else {
            if (positionRepository.findOneByWarehouseAndNameIgnoreCase(warehouse, position.getName()).isPresent()) {
                throw new BadRequestAlertException(
                    "Position '" + position.getName() + "' Already Exists",
                    "Position Saving",
                    "Already Exists"
                );
            }
        }

        if (StringUtils.isBlank(id)) {
            position.setId(null);
        }

        position = positionRepository.save(position);

        return position;
    }

    public void deletePosition(String id) {
        positionRepository
            .findById(id)
            .ifPresent(position -> {
                boxRepository
                    .findFirstByPosition(position)
                    .ifPresentOrElse(
                        box -> {
                            throw new BadRequestAlertException(
                                "Impossible to delete position is already used by a box",
                                "Position deleting",
                                "Already used somewhere"
                            );
                        },
                        () -> {
                            warehouseRepository
                                .findById(position.getWarehouse().getId())
                                .ifPresent(warehouse -> {
                                    Set<Position> positions = warehouse.getPositions();
                                    positions.removeIf(position1 -> position1.getId().equalsIgnoreCase(position.getId()));
                                    warehouseRepository.save(warehouse);
                                    positionRepository.delete(position);
                                });
                        }
                    );
            });
    }
}
