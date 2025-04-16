package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.BoxStock;
import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BoxStockDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private Integer inputQuantity;

    private Integer outputQuantity;

    private Integer inputReturnQuantity;

    private boolean returnOrder;

    public BoxStockDTO(BoxStock boxStock) {
        if (boxStock == null) {
            return;
        }

        this.id = boxStock.getId();
        this.inputQuantity = boxStock.getInputQuantity();
        this.outputQuantity = boxStock.getOutputQuantity();
        this.returnOrder = boxStock.isReturnOrder();
        this.inputReturnQuantity = boxStock.getInputReturnQuantity();
    }

    public BoxStock dtoToEntity() {
        return BoxStock
            .builder()
            .id(this.id)
            .inputQuantity(this.inputQuantity)
            .outputQuantity(this.outputQuantity)
            .inputReturnQuantity(this.inputReturnQuantity)
            .returnOrder(this.returnOrder)
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
