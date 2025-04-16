package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.Box;
import com.psu.rouen.cphbox.service.ServiceUtils;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.CollectionUtils;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BoxDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String reference;

    @NotNull
    private PositionDTO position;

    @NotNull
    private WarehouseDTO warehouse;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    @ToString.Exclude
    private Set<BoxCatalogDTO> boxCatalogs;

    public static BoxDTO createSimple(Box box) {
        if (box == null) {
            return null;
        }

        return BoxDTO
            .builder()
            .id(box.getId())
            .reference(box.getReference())
            .position(new PositionDTO(box.getPosition()))
            .warehouse(new WarehouseDTO(box.getWarehouse()))
            .createdDate(box.getCreatedDate())
            .createdBy(box.getCreatedBy())
            .lastModifiedDate(box.getLastModifiedDate())
            .lastModifiedBy(box.getLastModifiedBy())
            .build();
    }

    public BoxDTO(Box box) {
        if (box == null) {
            return;
        }
        this.id = box.getId();
        this.reference = box.getReference();
        this.position = new PositionDTO(box.getPosition());
        this.warehouse = new WarehouseDTO(box.getWarehouse());
        this.boxCatalogs =
            CollectionUtils.isEmpty(box.getBoxCatalogs())
                ? new HashSet<>()
                : box.getBoxCatalogs().stream().map(BoxCatalogDTO::new).collect(Collectors.toSet());
        this.createdDate = box.getCreatedDate();
        this.createdBy = box.getCreatedBy();
        this.lastModifiedDate = box.getLastModifiedDate();
        this.lastModifiedBy = box.getLastModifiedBy();
    }

    public Box dtoToEntity() {
        return Box
            .builder()
            .id(this.id)
            .reference(this.reference)
            .position(this.position != null ? this.position.dtoToEntity() : null)
            .warehouse(this.warehouse != null ? this.warehouse.dtoToEntity() : null)
            .boxCatalogs(
                CollectionUtils.isEmpty(this.boxCatalogs)
                    ? new HashSet<>()
                    : this.boxCatalogs.stream().map(boxCatalogDTO -> boxCatalogDTO.dtoToEntity()).collect(Collectors.toSet())
            )
            .build();
    }

    public int getNbTotalBook() {
        if (CollectionUtils.isEmpty(this.boxCatalogs)) {
            return 0;
        } else {
            return this.boxCatalogs.stream().map(BoxCatalogDTO::getNbBookAvailable).reduce(0, (total, element) -> total + element);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,reference, createdDate, createdBy, lastModifiedBy, lastModifiedDate);
    }
}
