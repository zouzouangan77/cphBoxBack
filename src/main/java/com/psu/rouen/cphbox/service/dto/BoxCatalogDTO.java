package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.BoxCatalog;
import com.psu.rouen.cphbox.domain.OrderItem;
import com.psu.rouen.cphbox.service.ServiceUtils;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BoxCatalogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private BoxDTO box;
    private CatalogDTO catalog;

    @Builder.Default
    @ToString.Exclude
    private Set<BoxStockDTO> boxStocks = new HashSet<>();

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    public static BoxCatalogDTO createSimple(BoxCatalog boxCatalog) {
        if (boxCatalog == null) {
            return null;
        }

        return BoxCatalogDTO
            .builder()
            .id(boxCatalog.getId())
            .box(new BoxDTO(boxCatalog.getBox()))
            .catalog(new CatalogDTO(boxCatalog.getCatalog()))
            .createdDate(boxCatalog.getCreatedDate())
            .createdBy(boxCatalog.getCreatedBy())
            .lastModifiedDate(boxCatalog.getLastModifiedDate())
            .lastModifiedBy(boxCatalog.getLastModifiedBy())
            .build();
    }

    public BoxCatalogDTO(BoxCatalog boxCatalog) {
        if (boxCatalog == null) {
            return;
        }
        this.id = boxCatalog.getId();
        this.catalog = boxCatalog.getCatalog() != null ? new CatalogDTO(boxCatalog.getCatalog()) : null;
        this.box = boxCatalog.getBox() != null ? BoxDTO.createSimple(boxCatalog.getBox()) : null;
        this.boxStocks =
            boxCatalog.getBoxStocks() != null
                ? boxCatalog.getBoxStocks().stream().map(BoxStockDTO::new).collect(Collectors.toSet())
                : new HashSet<>();
    }

    public BoxCatalog dtoToEntity() {
        return BoxCatalog
            .builder()
            .id(this.id)
            .catalog(this.catalog != null ? this.catalog.dtoToEntity() : null)
            .box(this.box != null ? this.box.dtoToEntity() : null)
            .boxStocks(
                CollectionUtils.isEmpty(this.boxStocks)
                    ? new HashSet<>()
                    : this.boxStocks.stream().map(BoxStockDTO::dtoToEntity).collect(Collectors.toSet())
            )
            .build();
    }

    public int getNbBookAvailable() {
        if (CollectionUtils.isEmpty(this.boxStocks)) {
            return 0;
        } else {
            return this.boxStocks.stream()
                .map(bs ->
                    Arrays.asList(
                        ServiceUtils.toInt(bs.getInputQuantity()),
                        ServiceUtils.toInt(bs.getInputReturnQuantity()),
                        -ServiceUtils.toInt(bs.getOutputQuantity())
                    )
                )
                .flatMap(Collection::stream)
                .reduce(0, (total, element) -> total + element);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdDate, createdBy, lastModifiedBy, lastModifiedDate);
    }
}
