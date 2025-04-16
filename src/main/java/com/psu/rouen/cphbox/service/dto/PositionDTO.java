package com.psu.rouen.cphbox.service.dto;

import com.psu.rouen.cphbox.domain.Position;
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
public class PositionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public PositionDTO(Position position) {
        if (position == null) {
            return;
        }
        this.id = position.getId();
        this.name = position.getName();
    }

    public Position dtoToEntity() {
        return Position.builder().id(this.id).name(this.name).build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,name );
    }
}
