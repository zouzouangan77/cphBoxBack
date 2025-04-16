package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.Event;
import com.psu.rouen.cphbox.domain.Warehouse;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WarehouseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String address;

    public WarehouseDTO(Warehouse warehouse) {
        if (warehouse == null) {
            return;
        }
        this.id = warehouse.getId();
        this.name = warehouse.getName();
        this.address = warehouse.getAddress();
    }


    public Warehouse dtoToEntity() {
        return Warehouse.builder().id(this.id).name(this.name).address(this.address).build();
    }
}
